package com.gitmining.mock.main

object test {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  import com.redis._
  import com.gitmining.mock.redis._

	Redis.dropCollection("test2")             //> SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
                                                  //| SLF4J: Defaulting to no-operation (NOP) logger implementation
                                                  //| SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further de
                                                  //| tails.
                                                  //| res0: Option[Long] = Some(1)

  Redis.addItems("test2", (1 to 10000).toList)    //> res1: Option[Long] = Some(10000)
  Redis.getRandomItems("test2", 3)                //> res2: Option[List[Option[String]]] = Some(List(Some(3144), Some(5750), Some(
                                                  //| 7279)))
  Redis.addLinks(10, "followers", Seq(20,30))     //> res3: Boolean = true
  
//  val clients = new RedisClientPool("localhost",6379)
//	clients.withClient {
//		client => client.hmset("testHM", Map(3 -> List("repo_1","repo_2","repo_3","repo_4","repo_5").mkString(",")))
//	}
//	var res = clients.withClient {
//		client => client.hmget("testHM", 3)
//	}
//  res.get(3) split ","
         
}