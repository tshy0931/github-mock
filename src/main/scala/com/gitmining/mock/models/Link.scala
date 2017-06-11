package com.gitmining.mock.models

/**
  * Created by weiwang on 09/06/17.
  */
case class Link(
               `type`:String,
               src:Long,
               dst:Long,
               w8t:Double
               ) extends Item