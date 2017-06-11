package com.gitmining.mock.models

import com.gitmining.mock.redis.Redis

import scala.util.Random

object User extends Item {

  val links = List("followers","following","starred","subscriptions","orgs","repos")

  def of(id:Long)(implicit random:Random):User = {
    val profile = Redis.hgetall(s"user:$id")
    val linkMap:Map[String, Set[Long]] = (links map {
      link => (link, Redis.smembers(s"user:$id:$link") map {_.toLong})
    }).toMap
    User(
      id,profile("login"),profile("type"),Some(profile("company")),Some(profile("location")),
      profile("hireable").toBoolean,
      profile("createdAt"),profile("updatedAt"),
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
  id:Long,
  login:String,
  `type`:String="User",
  company:Option[String]=None,
  location:Option[String]=None,
  hireable:Boolean=true,
  createdAt:String, //DateTime,
  updatedAt:String, //DateTime,
  followers:Set[Long]=Set(),
  followersCount:Int=0,
  following:Set[Long]=Set(),
  followingCount:Int=0,
  starred:Set[Long]=Set(),
  starredCount:Int=0,
  subscriptions:Set[Long]=Set(),
  subscriptionsCount:Int=0,
  orgs:Set[Long]=Set(),
  orgsCount:Int=0,
  repos:Set[Long]=Set(),
  reposCount:Int=0
) extends Item