package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.Contact
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox

class CharactersPane extends HBox {

  alignment = Pos.Center
  spacing = 60

  val characters = List(
    new CharacterVBox("astra.png",
      Contact("Astra Valentine", "Lockettside Valkyries", "Scientist", 9, 183)),
    new CharacterVBox("naomi.png",
      Contact("Naomi Valentine", "[no group]", "Military", 15, 206)),
    new CharacterVBox("lacie.png",
      Contact("Lacie Valentine", "[no group]", "Civilian", 25, 20))
  )

  children = characters
}
