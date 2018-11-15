package com.archmage.ghostmind.view

import javafx.scene.{layout => jfxsl}
import scalafx.Includes._
import com.archmage.ghostmind.model.Contact
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.TextAlignment

class CharacterBox(imageName: String, contact: Option[Contact] = None) extends VBox {

  alignment = Pos.TopCenter
  spacing = 10
  padding = Insets(5)

  var online: Boolean = false

  val hoverBackground =
    new Background(Array(new BackgroundFill(Color.DarkCyan, CornerRadii.Empty, Insets.Empty)))

  background <== when(hover) choose hoverBackground otherwise jfxsl.Background.EMPTY

  val avatar = new ImageView {
    try {
      image = new Image(getClass.getResourceAsStream(s"assets/$imageName"))
    }
    catch {
      case npe: NullPointerException =>
        image = new Image(getClass.getResourceAsStream("assets/human-icon.png"))
    }
    fitWidth = 90
    fitHeight = 90
  }

  val nameString = if(contact.isDefined) contact.get.name else "Unknown"
  val levelString = if(contact.isDefined) contact.get.level else "???"
  val classString = if(contact.isDefined) contact.get.currentClass else "Mystery"
  val groupString = if(contact.isDefined) contact.get.group else "[unknown group]"

  val name = new Label {
    id = "Title"
    text = nameString
  }

  val details = new Label {
    id = "Subtitle"
    text = s"the Level $levelString $classString\n$groupString"
    padding = Insets(-10, 0, 0, 0)
    textAlignment = TextAlignment.Center
  }

  val status = new Label {
    id = "CharacterBoxStatusText"
    style = "-fx-text-fill: #ff0000;"
    text = "OFFLINE"
    text.onChange { (_, _, _) => updateStatus() }
  }

  def updateStatus(): Unit = {
    status.text.value match {
      case "OFFLINE" => status.style = "-fx-text-fill: #ff0000;"
      case "CONNECTING" => status.style = "-fx-text-fill: #dddd00;"
      case "ONLINE" => status.style = "-fx-text-fill: #00ff00;"
    }
  }

  children = List(avatar, name, details, status)
}
