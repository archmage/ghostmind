package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.UrbanDeadModel
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{Label, Tab, TabPane}
import scalafx.scene.layout.{BorderPane, StackPane, VBox}

class MainBorderPane extends BorderPane {

  this.id = "root"

  // higher-level organising elements
  val leftTabPane = new TabPane
  val rightTabPane = new TabPane
  val centreVBox = new VBox {
    padding = Insets(10)
    spacing = 10
  }
  val skillsStackPane = new StackPane

  // actual interface elements
  val logoBox = new LogoBox
  val charactersPane = new CharactersPane
  var sessionBar: CharacterSessionBar = _
  var eventsCatchupBox: EventsCatchupBox = _
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

  // view helpers
  def tab(title: String, contentNode: Node): Tab = {
    new Tab {
      text = title
      content = contentNode
    }
  }

  // --- logic ---

  // change state
  def modelStateChanged(): Unit = {
    Platform.runLater(() => {
      UIModel.state.value match {
        case Characters() =>
          centreVBox.alignment = Pos.Center
          centreVBox.children = List(logoBox, charactersPane)

          top = null
          left = null
          right = null
          center = centreVBox
          bottom = statusBar
        case Main() =>
          sessionBar = new CharacterSessionBar(UrbanDeadModel.activeSession.get)
          eventsCatchupBox = new EventsCatchupBox(UrbanDeadModel.activeSession.get)
          centreVBox.alignment = Pos.TopCenter
          centreVBox.children = eventsCatchupBox

          top = sessionBar
          left = leftTabPane
          right = null
          center = centreVBox
          bottom = statusBar
      }
    })
  }

  // init stuff
  def init(): Unit = {
    UrbanDeadModel.loadCharacters(() => {
      charactersPane.children = UrbanDeadModel.sessions.zipWithIndex.map { session =>
        new CharacterBox(session._1, session._2)
      }
      for(i <- charactersPane.children.size() + 1 to 3)
        charactersPane.children.add(new CharacterBox(None, i))

      UIModel.state.onChange { (_, _, _) => modelStateChanged()}

      modelStateChanged()
    })
  }

  init()
}