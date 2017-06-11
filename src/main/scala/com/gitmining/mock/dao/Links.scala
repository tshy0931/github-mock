package com.gitmining.mock.dao

import com.gitmining.mock.models.Link
import com.outworkers.phantom.dsl.Table
import com.outworkers.phantom.dsl._

/**
  * Created by weiwang on 09/06/17.
  */
abstract class Links extends Table[Links, Link]{
  override lazy val tableName = "link"

  object `type` extends StringColumn with PartitionKey
  object src extends LongColumn with PartitionKey
  object dst extends LongColumn
  object w8t extends DoubleColumn with ClusteringOrder with Descending
}
