package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.CharacterSession
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.text.TextAlignment

class CharacterNameplate(var session: Option[CharacterSession] = None) extends VBox {

  alignment = Pos.Center
  padding = Insets(5)

  val name = new Label {
    id = "Title"
    text = ""
  }
  val details = new Label {
    id = "Subtitle"
    text = ""
    padding = Insets(-3, 0, 0, 0)
  }

  children = List(name, details)

  def update(): Unit = {
    name.text = if(session.isEmpty) "Unknown" else session.get.username
    val level = if(session.isEmpty || session.get.attributes.isEmpty) "???"
                else session.get.attributes.get.level
    val characterClass = if(session.isEmpty || session.get.attributes.isEmpty) "Mystery"
                         else session.get.attributes.get.characterClass
    details.text = s"the Level $level $characterClass"
  }

  update()
}
