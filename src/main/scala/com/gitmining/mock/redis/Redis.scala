package com.gitmining.mock.redis

object Redis {

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
      client => client.srandmember(itemType, count).get.flatten
    }
  }

  /**
   * pipeline a sequence of commands to Redis to save RTT.
   */
  val bulkExec = (commands:Seq[() => Any]) => {
    clients.withClient {
      client => {
        val result = client.pipelineNoMulti(commands)
        result map {p => Await.result(p.future, 60 minutes)}
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

  val smembers = (key:String) => {
    clients.withClient {
      _.smembers(key).get.flatten
    }
  }
  
  val hmset = (key:String, map:Map[String,Any]) => {
    clients.withClient {
      client => if(client.hmset(key, map)) Some(1L) else None
    }
  }

  val hmget = (key:String, field:String) => {
    clients.withClient {
      _.hmget(key, field).get.getOrElse(field,"")
    }
  }

  val hgetall = (key:String) => {
    clients.withClient {
      _.hgetall1(key) getOrElse Map[String,String]()
    }
  }
}