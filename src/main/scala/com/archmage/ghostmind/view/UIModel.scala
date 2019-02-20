package com.archmage.ghostmind.view

import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty

object UIModel {
  var state: ObjectProperty[UIState] = ObjectProperty(Characters())

  def state_=(state: UIState): Unit = {
    Platform.runLater(() => UIModel.state.value = state)
  }

  var onUpdateUI: Option[() => Unit] = None

  def updateUI(): Unit = {
    if(onUpdateUI.isDefined) onUpdateUI.get.apply()
  }
}

sealed trait UIState
case class Characters() extends UIState
case class Main() extends UIState