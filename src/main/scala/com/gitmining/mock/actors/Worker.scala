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
      val neighbors:List[String] = Redis.getRandomItems("users", msg.count)
      msg.linkType match {
        case t: Follower =>       
          Redis.bulkExec(
            msg.userIds flatMap {
              id => {
                (neighbors map {
                  id => () => Redis.sadd(s"user:$id:following", id)
                }).::(() => Redis.sadd(s"user:$id:followers", neighbors))
              }
            }
          )      
        case t: Starred =>
          Redis.bulkExec(
            msg.userIds flatMap {
              id => {
                val repos = getReposOfUser(id.toString, 3)
                val addStargazersCommands = repos map {
                  repo => () => Redis.sadd(s"repo:$repo:stargazers", id)
                }
                val addStarredCommand = () => Redis.sadd(s"user:$id:starred", repos)
                addStarredCommand :: addStargazersCommands
              }
            }
          )
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