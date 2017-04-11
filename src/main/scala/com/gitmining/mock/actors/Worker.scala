package com.gitmining.mock.actors

import akka.actor.Actor
import akka.event.Logging
import scala.util.Random
import java.util.concurrent.CountDownLatch
import com.gitmining.mock.redis.Redis

class Worker extends Actor{
  val log = Logging(context.system, this)
  val rand = Random.alphanumeric
  
  implicit val redisResult2StringList:Option[List[Option[String]]] => List[String] = (strList:Option[List[Option[String]]]) => {
    strList match {
      case Some(list) => list map {
        _ match {
          case Some(str:String) => str
          case _ => ""
        }
      }
      case _ => Nil
    }
  }
  
  def receive = {
    case msg:CreateUsers => {
      log.debug(s"Creating users with id range [${msg.users.head}, ${msg.users.last}]")
      Redis.addItems("users",msg.users)
      msg.latch.countDown()
    }
    case msg:CreateRepos => {
      log.debug(s"Creating ${msg.count} repos for users from ${msg.userIds.head} to${msg.userIds.last}")
      val repos = (0 to msg.count) map {
        i => s"${msg.prefix}${(rand take 10).mkString}"
      }
      Redis.bulkExec(msg.userIds map {
        id => () => Redis.sadd(s"user:${id}:repos", repos)
      })
      msg.latch.countDown()
    }
    case msg:CreateLinks => {
      msg.linkType match {
        case t: Follower => 
          val ids:List[String] = Redis.getRandomItems("users", msg.count)
          ids foreach {
            id => Redis.addLinks("user", id, "following", List(id))
          }
          Redis.addLinks("user", msg.id, "followers", ids)
          
        case t: Starred =>
          val ids:List[String] = Redis.getRandomItems("users", msg.count)
          val repos:List[String] = ids flatMap {
            //TODO: only pick 1 repo per user, extend to random repo counts
            id => { 
              val repos = getReposOfUser(id, 3)
              repos match {
                case rps:List[String] => rps foreach {
                  repo => Redis.addLinks("repo", repo, "stargazers", List(id))
                }
                case _ => log.error(s"SRANDMEMBER returned illegal result for user:$id:repos")
              }
              repos
            }
          }
          Redis.addLinks("user", msg.id, "starred", repos)

        case t: Subscription => 
          ???
      }
    }
    case _ => log.error("Unknown message")
  }
    
  val createUsers = (start:Int, end:Int, prefix:String) => {
    
  }
  
  val getReposOfUser:(String,Int) => List[String] = (userId:String, count:Int) => {
    Redis.getRandomItems(s"user:$userId:repos", count)
  }
}