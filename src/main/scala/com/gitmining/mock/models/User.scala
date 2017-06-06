package com.gitmining.mock.models

import java.time.ZonedDateTime

import com.gitmining.mock.redis.Redis
import org.joda.time.DateTime

import scala.util.Random

object User extends Item {

  val links = List("followers","following","starred","subscriptions","orgs","repos")

  def of(id:Int)(implicit random:Random) = {
    val profile = Redis.hgetall(s"user:$id")
    val linkMap:Map[String, Set[Int]] = (links map {
      link => (link, Redis.smembers(s"user:$id:$link") map {_.toInt})
    }).toMap
    User(
      id,profile("login"),profile("type"),Some(profile("company")),Some(profile("location")),
      profile("hireable").toBoolean,
      (profile("createdAt")),(profile("updatedAt")),
      //      toDateTime(profile("createdAt")),toDateTime(profile("updatedAt")),
      linkMap("followers"),linkMap("followers").size,
      linkMap("following"),linkMap("following").size,
      linkMap("starred"),linkMap("starred").size,
      linkMap("subscriptions"),linkMap("subscriptions").size,
      linkMap("orgs"),linkMap("orgs").size,
      linkMap("repos"),linkMap("repos").size)
  }
}

case class User(
  id:Int,
  login:String=Random.alphanumeric.take(8).mkString,
  `type`:String="User",
  company:Option[String]=None,
  location:Option[String]=None,
  hireable:Boolean=true,
  createdAt:String, //DateTime,
  updatedAt:String, //DateTime,
  followers:Set[Int]=Set(),
  followersCount:Int=0,
  following:Set[Int]=Set(),
  followingCount:Int=0,
  starred:Set[Int]=Set(),
  starredCount:Int=0,
  subscriptions:Set[Int]=Set(),
  subscriptionsCount:Int=0,
  orgs:Set[Int]=Set(),
  orgsCount:Int=0,
  repos:Set[Int]=Set(),
  reposCount:Int=0
) extends Item