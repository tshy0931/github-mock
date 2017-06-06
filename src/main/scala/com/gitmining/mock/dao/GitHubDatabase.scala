package com.gitmining.mock.dao

import com.datastax.driver.core.ConsistencyLevel
import com.gitmining.mock.models.{Item, Repo, User}
import com.outworkers.phantom.builder.query.CreateQuery.Default
import com.outworkers.phantom.connectors
import com.outworkers.phantom.connectors.{CassandraConnection, KeySpace}
import com.outworkers.phantom.database.DatabaseProvider
import com.outworkers.phantom.dsl.{Database, ResultSet}
import com.outworkers.phantom.dsl.context

import scala.concurrent.{Future => ScalaFuture}

/**
  * Cre
  * ated by weiwang on 03/06/17.
  */
object GitHubDatabase extends GitHubDatabase(connectors.ContactPoint.local.keySpace("gitmining"))

class GitHubDatabase(
  override val connector: CassandraConnection
) extends Database[GitHubDatabase](connector) {

  object Users extends Users with Connector
  object Repos extends Repos with Connector

  def store[T<:Item](item:T): ScalaFuture[ResultSet] = {
    val entity = item match {
      case i:User => Users
      case i:Repo => Repos
    }
    for{
     done <- entity.store(item).consistencyLevel_=(ConsistencyLevel.QUORUM).future()
    } yield done
  }
}


trait AppDatabase extends DatabaseProvider[GitHubDatabase] {

}
//
//trait GitHubService extends AppDatabase {
//
//  def store[T<:Item](item:T): ScalaFuture[ResultSet] = {
//    val entity = item match {
//      case i:User => db.users
//      case i:Repo => db.repos
//    }
//   for{
//     done <- entity.store(item).consistencyLevel_=(ConsistencyLevel.QUORUM).future()
//   } yield done
//  }
//
//}
//
//object TestConnector {
//  val connector = ContactPoint.local
//    .noHeartbeat()
//    .keySpace("github-mock")
//}
//
//object TestDatabase extends AppDatabase(TestConnector.connector)
//
//trait TestDatabaseProvider extends AppDatabase {
//  override def database: AppDatabase = TestDatabase
//}