package com.archmage.ghostmind.model

import java.time.format.DateTimeFormatter

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.util.Random

object Constants {
  val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
  val humanReadableFormatter = DateTimeFormatter.ofPattern("HH:mm, dd MMMM")
  val browser = new JsoupBrowser(UrbanDeadModel.useragent)
  val rng = new Random()

  val sessionQuotes = List(
    "expand your horizons",
    "open your mind",
    "advance your agendas",
    "be wary of shadows",
    "fear the great beyond",
    "ambience is bliss",
    "disavow your nightmares",
    "violence is unforgiving",
    "among friends, mysteries linger",
    "brave the darkness",
    "coordinate and conquer",
    "best be prepared for what lies ahead",
    "befriend anomalies",
    "embrace your quirks"
  )
}
