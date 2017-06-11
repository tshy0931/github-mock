package com.gitmining.mock.main

import com.gitmining.mock.dao.GitHubDatabase

object App {
  def main( args:Array[String] ):Unit = {
    import com.gitmining.mock.actors.Utils._
    import com.gitmining.mock.actors.Manager._
    import com.gitmining.mock.models.RandomAttributes
    import com.outworkers.phantom.dsl.context
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.duration._
    import scala.util.{Success, Failure}

    val userCount = args(0).toInt
    val groupCount = args(1).toInt
    implicit val timeout = Timeout(100 seconds)
    val manager = getManager
    GitHubDatabase.create()
    val future = manager ? Cleanup
    future onComplete {
      case Success(msg) => 
        println(msg)
        RandomAttributes.load
        manager ! IdRange(1, userCount, groupCount)
      case Failure(e) => throw e
    }

  }
}