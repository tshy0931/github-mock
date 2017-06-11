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
  object ownerId extends LongColumn
  object ownerName extends StringColumn
  object orgId extends LongColumn
  object orgName extends StringColumn
  object assignees extends SetColumn[Long]
  object language extends StringColumn
  object languages extends MapColumn[String,Long]
  object issues extends SetColumn[Long]
  object openIssuesCount extends IntColumn
  object createdAt extends StringColumn //DateTimeColumn
  object updatedAt extends StringColumn //DateTimeColumn
  object pushedAt extends StringColumn //DateTimeColumn
  object size extends LongColumn
  object networkCount extends IntColumn
  object forks extends SetColumn[Long]
  object forksCount extends IntColumn
  object collaborators extends SetColumn[Long]
  object collaboratorsCount extends IntColumn
  object stargazers extends SetColumn[Long]
  object starCount extends IntColumn
  object contributors extends SetColumn[Long]
  object contributorsCount extends IntColumn
  object subscribers extends SetColumn[Long]
  object subscribersCount extends IntColumn
}
