package com.gitmining.mock.actors

import akka.actor.{Actor,Props,ActorSystem,PoisonPill}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router, FromConfig}
import akka.event.Logging

import scala.concurrent.Future
import scala.concurrent.forkjoin._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.CountDownLatch

class Manager(start:Int, end:Int, workerCount:Int) extends Actor{
  val log = Logging(context.system, this)
  val workers = context.actorOf(FromConfig.props(Props[Worker]), "router1")
  val GROUP_SIZE = 1000
  val rand = scala.util.Random
  val LINK_TYPES:Seq[LinkType] = Seq(Follower(),Starred())
  
  def receive = {
    case msg: IdRange => 
      val fullList = (start to end).toList
      val groups = fullList.grouped(GROUP_SIZE).toList
      val groupCount = (end-start+1)/GROUP_SIZE+1
      val futureCreateUsers = Future {
        val latch = Utils.getCountDownLatch(groupCount)
         groups foreach {
          subList => {
            workers ! CreateUsers(subList, "user", latch)
          }
        }
        latch.await()
      }
      val futureCreateRepos = futureCreateUsers map {
        done => {
          val latch = Utils.getCountDownLatch(groupCount)
          groups foreach {
            subList => workers ! CreateRepos(subList, rand.nextInt(32), "repo_", latch)
          }
          latch.await()
        }
      }
      futureCreateRepos onSuccess {
        case _ => {
          val latch = Utils.getCountDownLatch(groupCount)
          groups foreach {
            subList => LINK_TYPES foreach {
              linkType => workers ! CreateLinks(subList, rand.nextInt(32), linkType, latch)
            }    
          }
          latch.await()
        }
      }
    case msg: Cleanup => com.gitmining.mock.redis.Redis.flushDB()
    case msg: Terminate => workers ! PoisonPill
  }
}