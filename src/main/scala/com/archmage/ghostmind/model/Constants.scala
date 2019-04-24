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
    // positive
    "open your mind",
    "expand your horizons",

    "brave the darkness",
    "disavow your nightmares",

    "befriend anomalies",
    "embrace your quirks",

    "prepare for the future",
    "coordinate and conquer",

    "sequence your acts",
    "advance your agendas",

    "victory is within your reach",
    "defeat was never a possibility",

    // negative
    "be wary of shadows",
    "fear the great beyond",

    "ambience is bliss",
    "violence is unforgiving",

    // ghosts??
    "the ghosts are here to help",
    "the ghosts are your friends",

    "safety among spectres",
    "hidden voices offer guidance",

    // misc
    "ambience abound",
    "mysteries seeped in silence"
  )
}
