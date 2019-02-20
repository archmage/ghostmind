package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.control.{Label, Tab, TabPane}
import scalafx.scene.layout._

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

class MainBorderPane extends BorderPane {
  implicit val ec: ExecutionContext = ExecutionContext.global

  id = "root"

  // higher-level organising elements
  val centreTabPane = new TabPane {
    style = "-fx-background-color: -darker-grey;"
    tabClosingPolicy = TabClosingPolicy.Unavailable
    vgrow = Priority.Always
  }
  val leftTabPane = new TabPane {
    style = "-fx-background-color: -darker-grey;"
    tabClosingPolicy = TabClosingPolicy.Unavailable
  }
  val rightTabPane = new TabPane {
    style = "-fx-background-color: -darker-grey;"
    tabClosingPolicy = TabClosingPolicy.Unavailable
  }
  val centreVBox = new VBox
  val skillsStackPane = new StackPane

  // actual interface elements
  val logoBox = new LogoBox
  val charactersPane = new CharactersPane
  var sessionBar: CharacterBar = _
  var eventsCatchupBox: EventsCatchupBox = _
  var environmentBox: EnvironmentBox = _
  var debugButtonBox: DebugButtonBox = _
  val skillsLabel = new Label {
    id = "WhiteText"
    text = "Skills!"
  }
  var mapBox: MapBox = _
  val contactsBox = new ContactsBox
  val statusBar = new StatusBar

  // assembly
  skillsStackPane.children = skillsLabel

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
    Future[Option[ListBuffer[Option[CharacterSession]]]] {
      UrbanDeadModel.loadCharacters()
    } map { characters =>
      charactersPane.children = characters.getOrElse(ListBuffer.fill(3)(None)).zipWithIndex.map { session =>
        new CharacterBox(session._1, session._2)
      }
      for(i <- charactersPane.children.size() + 1 to 3) charactersPane.children.add(new CharacterBox(None, i))

      UIModel.state.onChange { (_, _, _) => modelStateChanged()}
      UIModel.onUpdateUI = Some(() => update())
      modelStateChanged()
    }
  }

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
          sessionBar = new CharacterBar(UrbanDeadModel.activeSession.get)

          eventsCatchupBox = new EventsCatchupBox(UrbanDeadModel.activeSession.get)
          environmentBox = new EnvironmentBox(UrbanDeadModel.activeSession.get)
          debugButtonBox = new DebugButtonBox()

          centreTabPane.tabs = List(
            tab("environment", environmentBox),
            tab("debug", debugButtonBox)
          )

          centreVBox.alignment = Pos.TopCenter
          centreVBox.children = centreTabPane

          mapBox = new MapBox(UrbanDeadModel.activeSession.get)

          top = sessionBar
          left = mapBox
          right = eventsCatchupBox
          center = centreVBox
          bottom = statusBar
      }
    })
  }

  def update(): Unit = {
    sessionBar.update()
    mapBox.update()
    eventsCatchupBox.update()
  }

  init()
}