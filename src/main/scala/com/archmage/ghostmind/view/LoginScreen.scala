package com.archmage.ghostmind.view

import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.VBox

class LoginScreen(onSubmit: (String, String) => Unit, completion: () => Unit) extends VBox {

  alignment = Pos.Center
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

  val loginVBox = new LoginVBox(onSubmit, completion)

  children = List(icon, title, loginVBox)
}
