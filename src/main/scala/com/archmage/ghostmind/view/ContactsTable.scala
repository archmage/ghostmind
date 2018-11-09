package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Contact, UrbanDeadModel}
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.scene.control.{TableColumn, TableView}

class ContactsTable extends TableView[Contact](UrbanDeadModel.contactsBuffer) {

  val columnName = new TableColumn[Contact, String]("Name")
  val columnGroup = new TableColumn[Contact, String]("Group")
  val columnLevel = new TableColumn[Contact, Int]("Level")

  columnName.cellValueFactory = data => StringProperty(data.value.name)
  columnGroup.cellValueFactory = data => StringProperty(data.value.group)
  columnLevel.cellValueFactory = data => ObjectProperty(data.value.level)

  columns ++= List(columnLevel, columnName, columnGroup)
}
