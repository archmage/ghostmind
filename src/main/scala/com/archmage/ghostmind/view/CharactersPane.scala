package com.archmage.ghostmind.view

import scalafx.geometry.Pos
import scalafx.scene.layout.HBox

class CharactersPane(characters: CharacterBox*) extends HBox {

  alignment = Pos.Center

  children = characters
}
