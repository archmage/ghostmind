package com.archmage.ghostmind.view

import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{ContentDisplay, Label}
import scalafx.scene.layout._
import scalafx.scene.paint.Color

object StatusBar {
  var status: StringProperty = new StringProperty("a ghost approaches the terminal")

  def status_=(status: String): Unit = {
    Platform.runLater(() => StatusBar.status.value = status)
  }
}

class StatusBar extends StackPane {

  id = "StatusBar"
  alignment = Pos.CenterRight
  padding = Insets(5)

  val label = new Label {
    id = "WhiteText"
    text <== StatusBar.status
    style = "-fx-background-color: blue"
  }

  val connectivityLabel = new Label {
    id = "CharacterBoxStatusText" // TODO refactor this
    style = "-fx-text-fill: #ff0000;"
    text = "OFFLINE"
  }

  children = List(label, connectivityLabel)
}