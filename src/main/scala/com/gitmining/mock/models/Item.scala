package com.gitmining.mock.models

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

trait Item {
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  def toDateTime(str:String):ZonedDateTime = {
    ZonedDateTime.parse(str, dateTimeFormatter)
  }
}