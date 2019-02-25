package com.archmage.ghostmind.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.HBox

class CharactersPane(characters: CharacterBox*) extends HBox with Updateable {

  alignment = Pos.Center
  padding = Insets(5)
  spacing = 10

  children = characters

  def update(): Unit = {
    characters.foreach { _.update() }
  }
}
