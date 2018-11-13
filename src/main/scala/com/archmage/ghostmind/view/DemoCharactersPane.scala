package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.Contact
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox

class DemoCharactersPane extends HBox {

  alignment = Pos.Center
  spacing = 60

  val characters = List(
    new CharacterBox("Astra Valentine.png",
      Some(Contact("Astra Valentine", "Lockettside Valkyries", "Scientist", 9, 183))),
    new CharacterBox("Naomi Valentine.png",
      Some(Contact("Naomi Valentine", "[no group]", "Military", 15, 206))),
    new CharacterBox("Lacie Valentine.png",
      Some(Contact("Lacie Valentine", "[no group]", "Civilian", 25, 20)))
  )

  children = characters
}
