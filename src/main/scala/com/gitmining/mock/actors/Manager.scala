package com.gitmining.mock.actors

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Terminated}
import akka.event.Logging

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.routing.RoundRobinPool
import akka.routing.Broadcast
import com.gitmining.mock.dao.GitHubDatabase


object Manager {
  case object Terminate extends Message
  case object Cleanup extends Message
  case object Done extends Message
  case class IdRange(start:Int, end:Int, groupCount:Int) extends Message
}

class Manager extends Actor{

  val log = Logging(context.system, this)
  val workers:ActorRef = context.actorOf(RoundRobinPool(10).props(Props[Worker]), "router1")
  context watch workers
  val LINK_TYPES:Seq[LinkType] = Seq(
    Follower,Starred,Subscription,Assignee,Collaborator,Contributor,Fork,Issue
  )

  import com.gitmining.mock.actors.Manager._
  import com.gitmining.mock.actors.Worker._
  
  def receive = {
    case IdRange(start, end, groupSize) =>
      val fullList = (start to end).toList
      val groups = fullList.grouped(groupSize).toList
      val groupCount = groups.size
      val futureCreateUsers = Future {
        val latch = Utils.getCountDownLatch(groupCount)
        groups foreach {
          subList => {
            workers ! CreateUsers(subList, "user", latch)
          }
        }
        latch.await()
        println(s"Created ${end - start + 1} users")
      }
      val futureCreateRepos = futureCreateUsers map {
        done => {
          val latch = Utils.getCountDownLatch(groupCount)
          groups foreach {
            subList => workers ! CreateRepos(subList, 8, latch)
          }
          latch.await()
          println(s"Created repos")
        }
      }
      val futureCreateLinks = futureCreateRepos map {
        done => {
          val latch = Utils.getCountDownLatch(groupCount)
          groups foreach {
            subList => LINK_TYPES foreach {
              linkType => workers ! CreateLinks(subList, 8, linkType, latch)
            }    
          }
          latch.await()
          println(s"Created links")
        }
      }
      val futurePersist = futureCreateLinks map {
        done => {
          val latch = Utils.getCountDownLatch(groupCount)
          groups foreach {
            subList => workers ! Persist(subList, latch)
          }
          latch.await()
          println("Data Persisted")
        }
      }
      futurePersist onComplete {
        case Success(x) =>
          self ! Terminate
        case Failure(e) =>
          self ! Terminate
      }

    case Cleanup => 
      com.gitmining.mock.redis.Redis.flushDB()
      sender ! "Clean up done."
    case Terminate => 
      println("Shutting down worker pool")
      workers ! Broadcast(PoisonPill)
      
    case Terminated(actor) =>
      if(actor == workers){
        log.info("worker pool terminated, shutting down actor system")
        Utils.actorSystem.terminate()
      }else{
        log.info(s"Actor ${actor.path} terminated")
      }
  }
}