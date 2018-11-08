package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Contact, UrbanDeadModel}
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{Tab, TableColumn, TableView}

class ContactsTab extends Tab {

  this.text = "Contacts"

//  val data = ObservableBuffer[Contact](
//    Contact("Astra Valentine", "Lockettside Valkyries", "Scientist", 9, 183),
//    Contact("Naomi Valentine", "-", "Military", 15, 206),
//    Contact("Lacie Valentine", "-", "Zombie", 23, 220)
//  )

  val tableView = new TableView(UrbanDeadModel.contactsBuffer)
  val columnName = new TableColumn[Contact, String]("Name")
  val columnGroup = new TableColumn[Contact, String]("Group")
  val columnClass = new TableColumn[Contact, String]("Class")
  val columnLevel = new TableColumn[Contact, Int]("Level")
  val columnXp = new TableColumn[Contact, Int]("Xp")

  columnName.cellValueFactory = data => StringProperty(data.value.name)
  columnGroup.cellValueFactory = data => StringProperty(data.value.group)
  columnClass.cellValueFactory = data => StringProperty(data.value.currentClass)
  columnLevel.cellValueFactory = data => ObjectProperty(data.value.level)
  columnXp.cellValueFactory = data => ObjectProperty(data.value.xp)

  tableView.columns ++= List(columnName, columnGroup, columnClass, columnLevel, columnXp)

  this.content = tableView
}
