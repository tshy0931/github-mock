package com.gitmining.mock.actors

import akka.actor.Actor
import akka.event.Logging
import java.util.concurrent.CountDownLatch

import com.gitmining.mock.redis.Redis
import java.util.concurrent.atomic.AtomicLong

import com.gitmining.mock.models.{RandomAttributes, Repo, User}
import com.gitmining.mock.models.RandomAttributes._

import scala.util.{Failure, Random, Success}
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.time.temporal.ChronoUnit.DAYS
import java.time.format.DateTimeFormatter

import com.gitmining.mock.dao.GitHubDatabase

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Worker {

  case class CreateUsers(users:Seq[Int], prefix:String, latch:CountDownLatch) extends Message
  case class CreateRepos(userIds:Seq[Int], maxCount:Int, latch:CountDownLatch) extends Message
  case class CreateLinks(userIds:Seq[Int], maxCount:Int, linkType:LinkType, latch:CountDownLatch) extends Message
  case class Persist(userIds:Seq[Int], latch:CountDownLatch) extends Message

  val uniqRepoId = new AtomicLong(1)
  val uniqIssueId = new AtomicLong(1)
  val dateFormat = DateTimeFormatter.ISO_INSTANT
  implicit val random:Random = Random

  val createUsers = (userIds:Seq[Int], prefix:String) => {
    Redis.bulkExec(
      userIds map {
        userId =>
          val hireable = if (Random.nextDouble() > 0.5) true else false
          val createTime = ZonedDateTime.of(LocalDateTime.of(2008,1,1,0,0,0), ZoneId.of("Europe/Dublin"))
          val updateTime = ZonedDateTime.of(LocalDateTime.of(2017,5,30,0,0,0), ZoneId.of("Europe/Dublin"))
          val createdAt = randomDate(createTime, updateTime)
          val updatedAt = createdAt.plusDays(Random.nextInt(365))
          val valMap = Map(
            "login" -> s"${prefix}_${Random.alphanumeric.take(8).mkString}",
            "type" -> "User",
            "company" -> RandomAttributes.random(Company),
            "location" -> RandomAttributes.random(Location),
            "hireable" -> hireable.toString,
            "createdAt" -> createdAt.format(dateFormat),
            "updatedAt" -> updatedAt.format(dateFormat)
          )
          () => Redis.hmset(s"user:$userId", valMap)
      }
    )
  }

  val createRepos = (repoIds:List[Long], userId:Int) => {
    repoIds flatMap {
      repoId =>
        val userName = Redis.hmget(s"user:$userId", "login")
        val languages = Redis.getRandomItems("languages", Random.nextInt(8)+1) map {
          lang => (lang, Random.nextInt(2048000))
        } toMap
        val size:Long = languages.foldLeft(0L){ case (acc:Long, (k,v)) => acc+v}
        val createTime = ZonedDateTime.of(LocalDateTime.of(2008,1,1,0,0,0), ZoneId.of("Europe/Dublin"))
        val updateTime = ZonedDateTime.of(LocalDateTime.of(2017,5,30,0,0,0), ZoneId.of("Europe/Dublin"))
        val createdAt = randomDate(createTime, updateTime)
        val updatedAt = createdAt.plusDays(Random.nextInt(365))
        val pushedAt = updatedAt.plusDays(Random.nextInt(30))
        val valMap = Map(
          "name" -> s"repo_${Random.alphanumeric.take(8).mkString}",
          "ownerId" -> userId,
          "ownerName" -> userName,
          "orgId" -> userId,
          "orgName" -> userName,
          "language" -> languages.head._1,
          "size" -> size,
          "networkCount" -> Random.nextInt(2048),
          "createdAt" -> createdAt.format(dateFormat),
          "updatedAt" -> updatedAt.format(dateFormat),
          "pushedAt" -> pushedAt.format(dateFormat)
        )
        (() => Redis.hmset(s"repo:$repoId:languages", languages)) ::
        (() => Redis.hmset(s"repo:$repoId", valMap)) ::
        Nil
    }
  }

  val createUserUserLinks = (userIds:Seq[Int], maxCount:Int, set1:String, set2:String) => {
    val commands = userIds flatMap {
      userId =>
        val neighbors:List[String] = Redis.getRandomItems("users", Random nextInt maxCount+1)
        neighbors match {
          case Nil => Nil
          case list @ x :: xs =>
            val cmd1 = () => Redis.sadd(s"user:$userId:$set1", neighbors)
            val cmd2 = list map {
              neighbor => () => Redis.sadd(s"user:$neighbor:$set2", userId)
            }
            cmd1 :: cmd2
          }
      }

    if(commands!=Nil)Redis.bulkExec(commands)
  }

  val createUserRepoLinks = (userIds:Seq[Int], maxCount:Int, set1:String, set2:Option[String]) => {
    val commands = userIds flatMap {
      userId => {
        val neighbors:List[String] = Redis.getRandomItems("users", maxCount)
        val repos:List[String] = (neighbors match {
          case Nil => Nil
          case neighbors @ x :: xs => neighbors flatMap { neighbor =>
            Redis.getRandomItems(s"user:$neighbor:repos", maxCount)
          }
        })
        repos match {
          case Nil => Nil
          case repos @ x :: xs =>
            val cmd1 = () => Redis.sadd(s"user:$userId:$set1", repos)
            val cmd2 = set2 match {
              case None => Nil
              case Some(set2) => repos map {
                repo => () => Redis.sadd(s"repo:$repo:$set2", userId)
              }

            }
            cmd1 :: cmd2
        }
      }
    }
    if(commands!=Nil)Redis.bulkExec(commands)
  }

  private def randomDate(from: ZonedDateTime, to:ZonedDateTime): ZonedDateTime = {
    val diff = DAYS.between(from, to)
    from.plusDays(Random.nextInt(diff.toInt))
  }
}

class Worker extends Actor {
  import com.gitmining.mock.actors.Worker._

  val log = Logging(context.system, this)
  implicit val random:Random = Random

  def receive = {
    case CreateUsers(users, prefix, latch) => {
      log.debug(s"Creating users with id from ${users.head}")
      Redis.addItems("users",users)
      createUsers(users, prefix)
      latch.countDown()
    }
    case CreateRepos(userIds, maxCount, latch) => {
      val commands = userIds flatMap {
        userId => {
          val count = Random nextInt (maxCount+1)
          val repos = List.tabulate(count)(i => uniqRepoId.getAndIncrement)
          val cmd1 = repos match {
            case Nil => None
            case a @ x :: xs => Some(() => Redis.sadd(s"user:$userId:repos", a))
          }
          cmd1 match {
            case None => List.empty[()=>Any]
            case Some(f) =>
              f :: createRepos(repos, userId) :::
              (repos map {
                repoId => () => Redis.sadd("repos", repoId)
              })
          }
        }
      }
      Redis.bulkExec(commands)
      latch.countDown()
    }
    case CreateLinks(userIds, maxCount, linkType, latch) =>
      
      linkType match {
        case Follower =>
          createUserUserLinks(userIds, maxCount, "followers", "following")
        case Starred =>
          createUserRepoLinks(userIds, maxCount, "starred", Some("stargazers"))
        case Subscription => 
          createUserRepoLinks(userIds, maxCount, "subscriptions", Some("subscribers"))
      }      
      latch.countDown()

    case Persist(userIds, latch) =>
      userIds foreach {
        userId => {
          val userFuture = GitHubDatabase.store(User.of(userId))
          val repoFutures = Redis.smembers(s"user:$userId:repos").toList map {
            repoId => GitHubDatabase.store(Repo.of(repoId.toLong))
          }
          Future.sequence(userFuture :: repoFutures).onComplete{
            case Success(x) => println(x);latch.countDown()
            case Failure(x) => println(x);latch.countDown()
          }
        }
      }

    case _ => log.error("Unknown message")
  }

}