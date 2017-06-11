package com.gitmining.mock.models

import com.gitmining.mock.redis.Redis

import scala.util.Random

object Repo extends Item {

  val links = List("assignees","forks","collaborators","subscribers","contributors","stargazers")

  def of(id:Long)(implicit random:Random):Repo = {

    val profile = Redis.hgetall(s"repo:$id")
    val linkMap:Map[String, Set[Long]] = (links map {
      link => (link, Redis.smembers(s"repo:$id:$link") map {_.toLong})
    }).toMap
    val languages = Redis.hgetall(s"repo:$id:languages").mapValues(_.toLong)
    val issues = Redis.smembers(s"repo:$id:issues") map (_.toLong)
    val openIssueCount = if(issues.isEmpty) 0 else random.nextInt(issues.size)
    Repo(
      id, profile("name"),
      profile("ownerId").toInt, profile("ownerName"),
      profile("orgId").toInt, profile("orgName"),
      linkMap("assignees"),
      profile("language"), languages, issues, openIssueCount,
      profile("createdAt"),profile("updatedAt"),profile("pushedAt"),
      //      toDateTime(profile("createdAt")),toDateTime(profile("updatedAt")),toDateTime(profile("pushedAt")),
      profile("size").toLong, profile("networkCount").toInt,
      linkMap("forks"), linkMap("forks").size,
      linkMap("collaborators"), linkMap("collaborators").size,
      linkMap("stargazers"), linkMap("stargazers").size,
      linkMap("contributors"), linkMap("contributors").size,
      linkMap("subscribers"), linkMap("subscribers").size
    )
  }
}

case class Repo(
  id:Long,
  name:String,
  ownerId:Long,
  ownerName:String,
  orgId:Long,
  orgName:String,
  assignees:Set[Long],
  language:String,
  languages:Map[String, Long],
  issues:Set[Long],
  openIssuesCount:Int,
  createdAt:String, //DateTime,
  updatedAt:String, //DateTime,
  pushedAt:String, //DateTime,
  size:Long,
  networkCount:Int,
  forks:Set[Long]=Set(),
  forksCount:Int=0,
  collaborators:Set[Long]=Set(),
  collaboratorsCount:Int=0,
  stargazers:Set[Long]=Set(),
  starCount:Int=0,
  contributors:Set[Long]=Set(),
  contributorsCount:Int=0,
  subscribers:Set[Long]=Set(),
  subscribersCount:Int=0
) extends Item