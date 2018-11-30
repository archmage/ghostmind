package com.archmage.ghostmind

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneId}

object Sandbox extends App {
  val now = LocalDateTime.now().atZone(ZoneId.systemDefault())
  val minutesSinceRollover = LocalDateTime.MIN.until(now, ChronoUnit.MINUTES)

  val lastHit = now.minusHours(10)
  val apRecoveredLastHit = LocalDateTime.MIN.until(lastHit, ChronoUnit.MINUTES) / 30
  val apRecoveredNow = LocalDateTime.MIN.until(now, ChronoUnit.MINUTES) / 30
  val newAp = Math.min(50, 0 + (apRecoveredNow - apRecoveredLastHit).intValue())

  println(
    s"""
      |The time now is $now.
      |With a hypothetical lastHit of $lastHit...
      |the difference in time between now and then is ${lastHit.until(now, ChronoUnit.MINUTES)} minutes.
      |That should be 10 hours.
      |Now, since AP recovery is on a fixed cadence (one every 30min, synced to :00 and :30)...
      |it is crucial that we divide each by 30min, boxing them to discrete 30min intervals...
      |but to do this we need a point of reference (an epoch) that's at exactly :00 or :30.
      |I'm using LocalDateTime.MIN as my epoch, which is ${LocalDateTime.MIN}.
      |The AP recovered between the epoch and the last hit is $apRecoveredLastHit.
      |The AP recovered between the epoch and now is $apRecoveredNow.
      |The difference is $newAp, which should be 19-20. I think?
    """.stripMargin)
}

