package com.archmage.ghostmind.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane

class MailIcon extends StackPane {
  alignment = Pos.TopLeft

  val icon = new ImageView {
    image = new Image(getClass.getResourceAsStream("assets/mail.png"))
    preserveRatio = true
    fitWidth = 30
  }

  val mailCount = new Label {
    padding = Insets(10, 0, 0, 16)
    id = "MailCount"
    text = "99+"
    alignment = Pos.Center
  }

  children = List(icon, mailCount)
}
