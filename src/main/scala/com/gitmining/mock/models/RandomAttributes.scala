package com.gitmining.mock.models

object RandomAttributes {
  
  import com.gitmining.mock.redis.Redis
  
  sealed trait AttributeType
  case object Location extends AttributeType
  case object Company extends AttributeType
  case object Language extends AttributeType
  
  val companies = Seq(
    "IBM",
    "Intel",
    "Huawei",
    "Qualcomm",
    "Ericsson",
    "Google",
    "Datalex",
    "Zalando",
    "LinkedIn",
    "Airbnb",
    "Booking",
    "Uber",
    "Baidu",
    "Sohu",
    "Netease",
    "Alibaba",
    "Tencent",
    "Walmart",
    "Workday",
    "Phillips",
    "Pwc"
  )
  
  val locations = Seq(
    "Boston, US",
    "Los Angeles, US",
    "Berminham, UK",
    "London, UK",
    "Beijing, China",
    "Chengdu, China",
    "Shenzhen, China",
    "Hangzhou, China",
    "Bayarea, US",
    "Wayne, US",
    "Dublin, Ireland",
    "Cork, Ireland",
    "New Dehli, India",
    "Hongkong, China",
    "Sydney, Australia"
  )
  
  val languages = Seq(
    "Java",
    "JavaScript",
    "Python",
    "C#",
    "C++",
    "C",
    "Scala",
    "PHP",
    "Swift",
    "Kotlin",
    "Clojure"
  )
  
  def load = {
    Redis.bulkExec(
      Seq(
        () => Redis.sadd("companies", companies),
        () => Redis.sadd("languages", languages),
        () => Redis.sadd("locations", locations)
      )    
    )
  }
  
  def random(t: AttributeType):String = { 
    val list:List[String] = t match {
      case Company => Redis.getRandomItems("companies",1)
      case Language => Redis.getRandomItems("languages",1)
      case Location => Redis.getRandomItems("locations",1)
      case _ => throw new Exception("unsupported attribute type")
    }
    list.head
  }
}