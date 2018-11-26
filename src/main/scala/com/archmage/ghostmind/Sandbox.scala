package com.archmage.ghostmind

import java.time.LocalDateTime

import com.archmage.ghostmind.model.{CharacterSession, Event, UrbanDeadModel}

object Sandbox extends App {
  val now = LocalDateTime.now()
  val parsedDate = Event.parseTimeText("(10 hours and 37 minutes ago)")
  println(now)
  println(parsedDate)
}

