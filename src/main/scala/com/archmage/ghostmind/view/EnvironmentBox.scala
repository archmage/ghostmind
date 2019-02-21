package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.Insets
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Text, TextFlow}

// shows what's going on at your current location
class EnvironmentBox(session: CharacterSession) extends VBox with Updateable {

  padding = Insets(10)
  spacing = 10

  val textFlow = new TextFlow {
    id = "SolidGreyBorder"
    padding = Insets(10)
    children = new Text {
      fill = Color.White
      text = session.environment.getOrElse("no environment")
    }
  }

  def update(): Unit = {
    textFlow.children = new Text {
      fill = Color.White
      text = session.environment.getOrElse("no environment")
    }
  }

  children = textFlow

}
