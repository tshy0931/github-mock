package com.gitmining.mock.dao

import com.datastax.driver.core.{ConsistencyLevel, HostDistance, PoolingOptions}
import com.gitmining.mock.models.{Item, Link, Repo, User}
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.DatabaseProvider
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.dsl.context

import scala.concurrent.{Future => ScalaFuture}

/**
  * Created by weiwang on 03/06/17.
  */
object GitHubDatabase extends GitHubDatabase(
  ContactPoint.local.withClusterBuilder(
    _.withPoolingOptions(
      new PoolingOptions()
        .setMaxConnectionsPerHost(HostDistance.LOCAL, Int.MaxValue)
        .setCoreConnectionsPerHost(HostDistance.LOCAL, 100)
        .setMaxRequestsPerConnection(HostDistance.LOCAL, 32767)
        .setPoolTimeoutMillis(600000)
    )
  ).keySpace("gitmining")
)

class GitHubDatabase(
  override val connector: CassandraConnection
) extends Database[GitHubDatabase](connector) {

  object Users extends Users with Connector
  object Repos extends Repos with Connector
  object Links extends Links with Connector

  def store[T<:Item](item:T): ScalaFuture[ResultSet] = {
    for{
     done <- item.store(item)
    } yield done
  }

  implicit class RichItem[T<:Item](item:T) {
    def store(item:T):ScalaFuture[ResultSet] = {
      val colFam = item match {
        case i:User => Users
        case i:Repo => Repos
        case i:Link => Links
      }
      colFam.store(item).consistencyLevel_=(ConsistencyLevel.QUORUM).future()
    }
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