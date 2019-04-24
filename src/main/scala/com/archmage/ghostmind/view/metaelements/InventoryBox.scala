package com.archmage.ghostmind.view.metaelements

import com.archmage.ghostmind.model.CharacterSession
import com.archmage.ghostmind.view.Updateable
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Text, TextFlow}

class InventoryBox(session: CharacterSession) extends VBox with Updateable {

  val inventoryText = new TextFlow {
    val text = new Text {
      fill = Color.White
    }
    children = text
  }

  val encumbranceText = new Label {
    id = "WhiteText"
  }

  def update(): Unit = {
    inventoryText.text.text = session.attributes.inventory match {
      case Some(inventory) => s"inventory: ${inventory.toString()}"
      case None => "(no inventory data found)"
    }

    encumbranceText.text = session.attributes.encumbrance match {
      case Some(encumbrance) => s"encumbrance: $encumbrance%"
      case None => "(no encumbrance data found)"
    }
  }

  update()
  children = List(inventoryText, encumbranceText)
}
