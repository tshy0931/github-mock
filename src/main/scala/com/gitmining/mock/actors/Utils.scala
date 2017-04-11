package com.gitmining.mock.actors

import akka.actor.ActorSystem
import akka.actor.Props
import java.util.concurrent.CountDownLatch

object Utils {
  
  lazy val actorSystem = ActorSystem("github-mock_Actor_System")
  
  val getActorSystem = () => actorSystem
  
  val getManager = actorSystem.actorOf(Props[Manager], "github-mock-manager")
  
  val getCountDownLatch = (count:Int) => {
    new CountDownLatch(count)
  }
}