package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.Contact
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.VBox
import scalafx.scene.text.TextAlignment

class CharacterVBox(imageName: String, contact: Contact) extends VBox {

  alignment = Pos.TopCenter
  spacing = 10
  padding = Insets(40, 0, 0, 0)

  val avatar = new ImageView {
    try {
      image = new Image(getClass.getResourceAsStream(s"assets/$imageName"))
    }
    catch {
      case npe: NullPointerException => {
        image = new Image(getClass.getResourceAsStream("assets/human-icon.png"))
      }
    }
    fitWidth = 90
    fitHeight = 90
  }

  val name = new Label {
    id = "Title"
    text = contact.name
  }

  val details = new Label {
    id = "Subtitle"
    text = s"the Level ${contact.level} ${contact.currentClass}\n${contact.group}"
    padding = Insets(-10, 0, 0, 0)
    textAlignment = TextAlignment.Center
  }

  children = List(avatar, name, details)
}
