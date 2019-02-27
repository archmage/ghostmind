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

  val eventClaw = new Image(getClass.getResourceAsStream("claw.png"))
  val eventBite = new Image(getClass.getResourceAsStream("bite.png"))
  val eventInfect = new Image(getClass.getResourceAsStream("infect.png"))
  val eventAlarm = new Image(getClass.getResourceAsStream("alarm.png"))
  val eventRadio = new Image(getClass.getResourceAsStream("radio.png"))
  val eventLightsOn = new Image(getClass.getResourceAsStream("light-on.png"))
  val eventLightsOff = new Image(getClass.getResourceAsStream("light-off.png"))
  val eventFlare = new Image(getClass.getResourceAsStream("firework.png"))
  val eventMegaphone = new Image(getClass.getResourceAsStream("megaphone.png"))
  val eventRefuel = new Image(getClass.getResourceAsStream("gas.png"))

  val eventExtract = new Image(getClass.getResourceAsStream("dna.png"))
  val eventRevive = new Image(getClass.getResourceAsStream("syringe.png"))
  val eventAttacked = new Image(getClass.getResourceAsStream("pk.png"))
  val eventDumped = new Image(getClass.getResourceAsStream("dump.png"))

  val eventSearchFind = new Image(getClass.getResourceAsStream("item.png"))
  val eventSearchFail = new Image(getClass.getResourceAsStream("search.png"))
}
