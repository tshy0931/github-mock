package com.gitmining.mock.actors

import com.redis._
import java.util.concurrent.CountDownLatch

trait Message {
  
}
case class Terminate() extends Message
case class IdRange(start:Int, end:Int) extends Message
case class CreateUsers(users:Seq[Int], prefix:String, latch:CountDownLatch) extends Message
case class CreateRepos(userIds:Seq[Int], count:Int, prefix:String, latch:CountDownLatch) extends Message
case class CreateLinks(id:String, count:Int, linkType:LinkType) extends Message

trait LinkType {
  val name:String
//  def add()
//  def remove()
}

case class Follower(name:String = "followers") extends LinkType
//case class Following(name:String = "following") extends LinkType
case class Starred(name:String = "starred") extends LinkType
case class Subscription(name:String = "sub") extends LinkType
case class Organization(name:String = "org") extends LinkType
case class Repo(name:String = "repo") extends LinkType

case class Fork(name:String = "fork") extends LinkType
case class Collaborator(name:String = "collaborator") extends LinkType
case class Assignee(name:String = "assignee") extends LinkType
//case class Stargazer(name:String = "stargazer") extends LinkType
case class Contributor(name:String = "contributor") extends LinkType
case class Subscriber(name:String = "subscriber") extends LinkType
case class Issue(name:String = "issue") extends LinkType

