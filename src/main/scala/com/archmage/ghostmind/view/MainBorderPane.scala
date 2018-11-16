package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.{Label, Tab, TabPane}
import scalafx.scene.layout.{BorderPane, StackPane, VBox}

class MainBorderPane extends BorderPane {

  this.id = "root"

  // higher-level organising elements
  val leftTabPane = new TabPane
  val rightTabPane = new TabPane
  val centreVBox = new VBox
  val skillsStackPane = new StackPane

  // actual interface elements
  val logoBox = new LogoBox
  val charactersPane = new CharactersPane
  var sessionBar: CharacterSessionBar = _
  val skillsLabel = new Label {
    id = "WhiteText"
    text = "Skills!"
  }
  val mapBox = new MapBox
  val contactsBox = new ContactsBox
  val statusBar = new StatusBar

  // assembly
  skillsStackPane.children = skillsLabel
  leftTabPane.tabs = List(
    tab("Maps", mapBox)
  )
  rightTabPane.tabs = List(
    tab("Contacts", contactsBox),
    tab("Skills", skillsStackPane)
  )

  // tweaking
  centreVBox.children = logoBox
  centreVBox.alignment = Pos.Center
  centreVBox.spacing = 10

  // placement
  center = centreVBox
  bottom = statusBar

  // view helpers
  def tab(title: String, contentNode: Node): Tab = {
    new Tab {
      text = title
      content = contentNode
    }
  }

  // --- logic ---

  // init stuff
  def init(): Unit = {
    UrbanDeadModel.loadCharacters()
    charactersPane.children = UrbanDeadModel.sessions.map { session =>
      new CharacterBox(Some(session))
    }
    if (charactersPane.children.size() < 3) {
      charactersPane.children.add(new CharacterBox)
    }
    centreVBox.children = List(logoBox, charactersPane)
  }

  init()
}