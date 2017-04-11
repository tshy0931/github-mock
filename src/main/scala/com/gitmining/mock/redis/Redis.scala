package com.gitmining.mock.redis

object Redis {

  import com.gitmining.mock.actors.LinkType
  import com.redis._
  import com.gitmining.mock.models.Item
  import java.util.concurrent.CountDownLatch
  import scala.concurrent.Await
  import scala.concurrent.duration._
  
  private val REDIS_DB_INDEX = 7
  private lazy val clients = new RedisClientPool("localhost",6379, database=REDIS_DB_INDEX)

  val addItems = (itemType:String, items:Seq[Any]) => {
    sadd(itemType, items)
  }
  
  val addLinks = (itemType:String, id:String, linkType:String, items:Seq[String]) => {
    sadd(s"$itemType:$id:$linkType", items)
  }
  
  val getRandomItems = (itemType:String, count:Int) => {
    clients.withClient {
      client => client.srandmember(itemType, count)
    }
  }

  /**
   * pipeline a sequence of commands to Redis to save RTT.
   */
  val bulkExec = (commands:Seq[() => Any]) => {
    clients.withClient {
      client => {
        val result = client.pipelineNoMulti(commands)
        result map {p => Await.result(p.future, 2 minutes)}
      }
    }
  }
  
  val dropCollection = (key:String) => {
    clients.withClient {
      client => client.del(key)
    }
  }
  
  val flushDB = () => {
    clients.withClient(_.flushdb)
  }
  
  val sadd = (key:String, values:Any) => {
    clients.withClient {
      client => values match {
        case x :: xs => client.sadd(key, x, xs:_*)
        case x => client.sadd(key, x)
      }
    }
  }
  

}