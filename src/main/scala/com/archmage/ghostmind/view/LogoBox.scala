package com.archmage.ghostmind.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.VBox

class LogoBox extends VBox {

  alignment = Pos.Center
  padding = Insets(10)
  spacing = 10

  val icon = new ImageView{
    image = new Image(getClass.getResourceAsStream("assets/ghost.png"))
    fitWidth = 80
    fitHeight = 80
  }

  val title = new Label {
    id = "GhostmindLogo"
    text = "ghostmind"
  }

  children = List(icon, title)
}
