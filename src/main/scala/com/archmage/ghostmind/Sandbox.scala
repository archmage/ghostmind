package com.archmage.ghostmind

import com.archmage.ghostmind.model.UrbanDeadModel
import com.archmage.ghostmind.view.StatusBar

object Sandbox extends App {
  println(UrbanDeadModel.loadCharacters())
  println(UrbanDeadModel.sessions)

  StatusBar.status.onChange((newValue, _, _) => println(newValue))

  UrbanDeadModel.sessions.flatten.headOption.map {
    head => UrbanDeadModel.loginExistingSession(head, UrbanDeadModel.sessions.indexOf(Some(head)))
  }
}