package com.archmage.ghostmind.view

import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

object StatusBar {
  var status: StringProperty = new StringProperty("a ghost approaches the terminal")

  def status_=(status: String): Unit = {
    Platform.runLater(() => StatusBar.status.value = status)
  }
}

class StatusBar extends HBox {

  id = "StatusBar"
  alignment = Pos.Center
  padding = Insets(5)

  val label = new Label {
    id = "WhiteText"
    text <== StatusBar.status
  }

  children = label
}