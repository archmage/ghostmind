package com.archmage.ghostmind.view.assets

import scalafx.scene.image.Image

object AssetManager {
  /*
  hashmap
  have it scan the directory upon calling a function
  make a hashmap of files and filenames
  i'll do it later lmao
   */

  val humanImage = new Image(getClass.getResourceAsStream("human-icon.png"))
  val plusImage = new Image(getClass.getResourceAsStream("plus.png"))
  val mailImage = new Image(getClass.getResourceAsStream("mail.png"))

  // event icons
  val eventDefault = new Image(getClass.getResourceAsStream("event.png"))
  val eventHeal = new Image(getClass.getResourceAsStream("heal.png"))
  val eventDeath = new Image(getClass.getResourceAsStream("grave.png"))
  val eventSpeech = new Image(getClass.getResourceAsStream("love-speech.png"))
  val eventStand = new Image(getClass.getResourceAsStream("stand.png"))
}
