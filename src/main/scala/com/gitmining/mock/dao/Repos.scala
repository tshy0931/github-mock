package com.gitmining.mock.dao

import com.gitmining.mock.models.Repo
import com.outworkers.phantom.dsl._

/**
  * Created by weiwang on 03/06/17.
  */
abstract class Repos extends Table[Repos, Repo] {

  override lazy val tableName = "repo"

  object id extends LongColumn with PartitionKey
  object name extends StringColumn
  object ownerId extends IntColumn
  object ownerName extends StringColumn
  object orgId extends IntColumn
  object orgName extends StringColumn
  object assignees extends SetColumn[Int]
  object language extends StringColumn
  object languages extends MapColumn[String,Long]
  object issues extends SetColumn[Long]
  object openIssuesCount extends IntColumn
  object createdAt extends StringColumn //DateTimeColumn
  object updatedAt extends StringColumn //DateTimeColumn
  object pushedAt extends StringColumn //DateTimeColumn
  object size extends LongColumn
  object networkCount extends IntColumn
  object forks extends SetColumn[Int]
  object forksCount extends IntColumn
  object collaborators extends SetColumn[Int]
  object collaboratorsCount extends IntColumn
  object stargazers extends SetColumn[Int]
  object starCount extends IntColumn
  object contributors extends SetColumn[Int]
  object contributorsCount extends IntColumn
  object subscribers extends SetColumn[Int]
  object subscribersCount extends IntColumn
}
