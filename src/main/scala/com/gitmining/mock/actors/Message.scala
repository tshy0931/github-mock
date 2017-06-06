package com.gitmining.mock.actors

import com.redis._
import java.util.concurrent.CountDownLatch

trait Message {
  
}

trait LinkType {

}

case object Follower extends LinkType
case object Starred extends LinkType
case object Subscription extends LinkType
case object Organization extends LinkType

case object Fork extends LinkType
case object Collaborator extends LinkType
case object Assignee extends LinkType
case object Contributor extends LinkType
case object Issue extends LinkType

