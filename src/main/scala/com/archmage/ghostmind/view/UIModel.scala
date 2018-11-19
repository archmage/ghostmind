package com.archmage.ghostmind.view

import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty

object UIModel {
  var state: ObjectProperty[UIState] = ObjectProperty(Characters())

  def state_=(state: UIState): Unit = {
    Platform.runLater(() => UIModel.state.value = state)
  }
}

sealed trait UIState
case class Characters() extends UIState
case class Main() extends UIState