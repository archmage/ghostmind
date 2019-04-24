package com.archmage.ghostmind.view.components

import com.archmage.ghostmind.model.CharacterSession
import com.archmage.ghostmind.view.Updateable
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class CharacterNameplate(var session: Option[CharacterSession] = None) extends VBox with Updateable {

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
    val level = if(session.isEmpty || session.get.attributes.level.isEmpty) "???"
                else session.get.attributes.level.get
    val characterClass = if(session.isEmpty || session.get.attributes.characterClass.isEmpty) "Mystery"
                         else session.get.attributes.characterClass.get
    details.text = s"the Level $level $characterClass"
  }

  update()
}
