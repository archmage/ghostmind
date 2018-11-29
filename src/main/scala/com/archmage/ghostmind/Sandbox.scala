package com.archmage.ghostmind

import java.time.{LocalDateTime, ZoneId}
import java.time.temporal.ChronoUnit

import com.archmage.ghostmind.model.UrbanDeadModel

object Sandbox extends App {
  val now = LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"))
  val nextRollover = UrbanDeadModel.getNextRollover()

  println(
    s"""
       |$now
       |$nextRollover
       |${nextRollover.withZoneSameInstant(ZoneId.of("Australia/Melbourne"))}
       |${now.until(nextRollover, ChronoUnit.HOURS)}
     """.stripMargin)
}

