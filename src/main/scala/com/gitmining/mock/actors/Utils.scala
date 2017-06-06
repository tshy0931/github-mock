package com.gitmining.mock.actors

import akka.actor.ActorSystem
import akka.actor.Props
import java.util.concurrent.CountDownLatch

object Utils {
  
  val actorSystem = ActorSystem("github-mock")
    
  val getManager = actorSystem.actorOf(Props[Manager], "github-mock-manager")
  
  val getCountDownLatch = (count:Int) => {
    new CountDownLatch(count)
  }

  implicit lazy val random = scala.util.Random
}