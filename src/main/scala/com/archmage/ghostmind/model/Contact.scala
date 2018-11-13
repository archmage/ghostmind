package com.archmage.ghostmind.model

object Contact {

}

case class Contact(
  name: String,
  group: String,
  currentClass: String,
  level: Int,
  xp: Int,
  id: Int = 0,
  colour: Int = 0)
