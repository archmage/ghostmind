package com.archmage.ghostmind

import com.archmage.ghostmind.model.UrbanDeadModel
import com.archmage.ghostmind.view.StatusBar

object Sandbox extends App {

  val something = try {
    throw new Exception
  }
  catch {
    case _: Exception => "420"
  }

  println(something)

  def tryUsingModelWithoutUI(): Unit = {
    println(UrbanDeadModel.loadCharacters())
    println(UrbanDeadModel.sessions)

    UrbanDeadModel.sessions.flatten.headOption.map {
      head => UrbanDeadModel.loginExistingSession(head, UrbanDeadModel.sessions.indexOf(Some(head)))
    }
  }
}