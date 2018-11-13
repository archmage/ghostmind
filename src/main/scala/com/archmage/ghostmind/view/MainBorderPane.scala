package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
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
  val loginScreen = new LoginScreen(UrbanDeadModel.loginRequest, onLoginCompletion)
  val charactersPane = new CharactersPane
  var sessionBar: CharacterSessionBar = _
  val skillsLabel = new Label {
    id = "WhiteText"
    text = "Skills!"
  }
  val mapBox = new MapBox
  val contactsBox = new ContactsBox


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
  centreVBox.children = loginScreen
  centreVBox.alignment = Pos.Center
  centreVBox.spacing = 10

  // placement
  center = centreVBox

  def tab(title: String, contentNode: Node): Tab = {
    new Tab {
      text = title
      content = contentNode
    }
  }

  def onLoginCompletion(): Unit = {
    val characterBox = new CharacterBox(s"${UrbanDeadModel.activeSession.get.username}.png")
    characterBox.name.text = UrbanDeadModel.activeSession.get.username
    charactersPane.children = characterBox

    centreVBox.children = List(loginScreen, charactersPane)
  }

  def startSession(session: CharacterSession): Unit = {
    left = leftTabPane
    right = rightTabPane

    sessionBar = new CharacterSessionBar(UrbanDeadModel.activeSession.get)
    top = sessionBar
  }
}