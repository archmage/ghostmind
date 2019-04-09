package com.archmage.ghostmind.view

import java.awt.Desktop
import java.net.URI

import com.archmage.ghostmind.model.{Connecting, ConnectivityState, UrbanDeadModel}
import scalafx.application.Platform
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Hyperlink, Label}
import scalafx.scene.layout._

object StatusBar {
  var status: StringProperty = StringProperty("a ghost approaches the terminal")
  var udConnectivity: ObjectProperty[ConnectivityState] = ObjectProperty(Connecting)
  var wikiConnectivity: ObjectProperty[ConnectivityState] = ObjectProperty(Connecting)

  def status_=(status: String): Unit = StatusBar.status.value = status

  def udConnectivity_=(connectivity: ConnectivityState): Unit = StatusBar.udConnectivity.value = connectivity

  status.onChange((newValue, _, _) => println(newValue))
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

    // instead... use onChange to fire Platform.runLater, so as to guarantee UI changes happen on the UI thread
    StatusBar.status.onChange((newValue, _, _) => Platform.runLater(() => text = newValue.value))
  }

  // needs to be separate due to scope limitations
  statusLabel.prefWidth <== width

  val udConnectivityLabel = new Hyperlink {
    id = "ConnectivityIndicator"
    style = StatusBar.udConnectivity.value.style
    text = "[ud]"
    focusTraversable = false
    onAction = _ => {
      Desktop.getDesktop.browse(new URI(UrbanDeadModel.baseUrl))
      visited = false
    }


    StatusBar.udConnectivity.onChange((newValue, _, _) => Platform.runLater(() => style = newValue.value.style))
  }

  val wikiConnectivityLabel = new Hyperlink {
    id = "ConnectivityIndicator"
    style = StatusBar.wikiConnectivity.value.style
    text = "[wiki]"
    focusTraversable = false
    onAction = _ => {
      Desktop.getDesktop.browse(new URI(UrbanDeadModel.wikiBaseUrl))
      visited = false
    }

    StatusBar.wikiConnectivity.onChange((newValue, _, _) => Platform.runLater(() => style = newValue.value.style))
  }

  val connectivityBox = new HBox {
    alignment = Pos.CenterRight
    hgrow = Priority.Never
    children = List(udConnectivityLabel, wikiConnectivityLabel)
  }

  children = List(statusLabel, connectivityBox)
}