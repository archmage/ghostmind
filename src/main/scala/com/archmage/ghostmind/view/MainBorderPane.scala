package com.archmage.ghostmind.view

import scalafx.scene.Node
import scalafx.scene.control.{Tab, TabPane}
import scalafx.scene.layout.{BorderPane, StackPane}

class MainBorderPane extends BorderPane {

  this.id = "root"

  val loginBar = new LoginBox
  val leftTabPane = new TabPane
  val rightTabPane = new TabPane
  val centreStackPane = new StackPane

  def tab(title: String, contentNode: Node): Tab = {
    new Tab {
      text = title
      content = contentNode
    }
  }

  leftTabPane.tabs = List(
    tab("Maps", new MapBox)
  )
  rightTabPane.tabs = List(
    tab("Contacts", new ContactsBox)
  )

//  centreStackPane.children = new LoginScreen
  centreStackPane.children = new LoginScreen

//  top = loginBar
//  left = leftTabPane
//  right = rightTabPane
  center = centreStackPane
}

