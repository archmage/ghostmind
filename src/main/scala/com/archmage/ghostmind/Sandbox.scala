package com.archmage.ghostmind

import java.time.LocalDateTime

import com.archmage.ghostmind.model.Event

object Sandbox extends App {
  val now = LocalDateTime.now()
  val parsedDate = Event.parseTimeText("(6 seconds ago)")
  println(now)
  println(parsedDate)
}

