package com.archmage.ghostmind.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.VBox

class LoginScreen extends VBox {

  alignment = Pos.Center
  spacing = 10

  val icon = new ImageView{
    image = new Image(getClass.getResourceAsStream("assets/ghost.png"))
    fitWidth = 80
    fitHeight = 80
  }

  val title: Label = new Label {
    id = "GhostmindLogo"
    text = "ghostmind"
  }

  val loginVBox = new LoginVBox

  children = List(icon, title, loginVBox)
}
