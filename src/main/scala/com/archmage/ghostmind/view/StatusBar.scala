package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Authenticating, ConnectivityState}
import scalafx.application.Platform
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout._

object StatusBar {
  var status: StringProperty = StringProperty("a ghost approaches the terminal")
  var connectivity: ObjectProperty[ConnectivityState] = ObjectProperty(Authenticating)

  def status_=(status: String): Unit = StatusBar.status.value = status

  def connectivity_=(connectivity: ConnectivityState): Unit = StatusBar.connectivity.value = connectivity
}

class StatusBar extends StackPane {

  id = "StatusBar"
  alignment = Pos.CenterRight
  padding = Insets(5)

  val statusLabel = new Label {
    id = "WhiteText"
    alignment = Pos.Center
    // explicitly not binding this because it breaks when multithreading
    // text <== StatusBar.status

    // instead... have THIS CLASS be where UI async shit lives
    StatusBar.status.onChange((newValue, _, _) => Platform.runLater(() => text = newValue.value))
  }

  // needs to be separate due to scope limitations
  statusLabel.prefWidth <== width

  val connectivityLabel = new Label {
    id = "ConnectivityStatusText"
    style = "-fx-text-fill: #ff0000;"
    text = "OFFLINE"
  }

  children = List(statusLabel, connectivityLabel)
}