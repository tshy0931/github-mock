package com.gitmining.mock.dao

import com.gitmining.mock.models.User
import com.outworkers.phantom.dsl._

/**
  * Created by weiwang on 03/06/17.
  */
abstract class Users extends Table[Users, User] {

  override lazy val tableName = "user"

  object id extends LongColumn with PartitionKey
  object login extends StringColumn
  object `type` extends StringColumn
  object company extends OptionalStringColumn
  object location extends OptionalStringColumn
  object hireable extends BooleanColumn
  object createdAt extends StringColumn //DateTimeColumn
  object updatedAt extends StringColumn //DateTimeColumn with ClusteringOrder
  object followers extends SetColumn[Long]
  object followersCount extends IntColumn
  object following extends SetColumn[Long]
  object followingCount extends IntColumn
  object starred extends SetColumn[Long]
  object starredCount extends IntColumn
  object subscriptions extends SetColumn[Long]
  object subscriptionsCount extends IntColumn
  object orgs extends SetColumn[Long]
  object orgsCount extends IntColumn
  object repos extends SetColumn[Long]
  object reposCount extends IntColumn
}
