package com.archmage.ghostmind.view

import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.HBox

class CharactersPane(_characters: CharacterBox*) extends HBox with Updateable {

  val characters = new ObservableBuffer[CharacterBox]
  characters.onChange( (_, _) => children = characters )
  characters ++= _characters

  alignment = Pos.Center
  padding = Insets(5)
  spacing = 10

  children = _characters

  def update(): Unit = {
    characters.forEach { _.update() }
  }
}
