package com.archmage.ghostmind.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.HBox

class CharactersPane(characters: CharacterBox*) extends HBox {

  alignment = Pos.Center
  padding = Insets(5)
  spacing = 10

  children = characters
}
