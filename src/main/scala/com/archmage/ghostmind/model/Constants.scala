package com.archmage.ghostmind.model

import java.time.format.DateTimeFormatter

import scala.util.Random

object Constants {
  val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
  val humanReadableFormatter = DateTimeFormatter.ofPattern("HH:mm, dd MMMM")
  val rng = new Random()
}
