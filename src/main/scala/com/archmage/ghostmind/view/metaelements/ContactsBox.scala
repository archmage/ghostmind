package com.archmage.ghostmind.view.metaelements

import com.archmage.ghostmind.view.components.{ContactsTable, GhostField}
import scalafx.geometry.Pos
import scalafx.scene.layout.VBox

class ContactsBox extends VBox {

  alignment = Pos.TopCenter
  spacing = 10

  val searchField = new GhostField

  searchField.promptText = "search"

  children = List(searchField, new ContactsTable)

}
