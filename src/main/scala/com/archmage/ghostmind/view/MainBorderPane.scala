package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Block, Suburb, UrbanDeadModel}
import scalafx.application.Platform
import scalafx.geometry.Pos
import scalafx.scene.control.ProgressIndicator
import scalafx.scene.layout._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class MainBorderPane extends BorderPane {
  id = "root"

  var centreVBox: VBox = _
  var logoBox: LogoBox = _
  var statusBar: StatusBar = _

  var charactersPane: CharactersPane = _
  var sessionBar: CharacterBar = _
  var eventsCatchupBox: EventsCatchupBox = _
  var environmentBox: EnvironmentBox = _
  var actionButtonBox: ActionButtonBox = _
  var mapBox: MapBox = _

  // --- logic ---

  // init stuff
  def init(): Unit = {
    StatusBar.status = "building preliminary UI..."
    logoBox = new LogoBox
    statusBar = new StatusBar

    centreVBox = new VBox {
      alignment = Pos.Center
      children = List(logoBox, new ProgressIndicator { progress = -1.0f })
    }

    center = centreVBox
    bottom = statusBar

    StatusBar.status = "loading data files..."
    Future[Unit] {
      // load suburbs
      StatusBar.status = "loading suburb data..."
      Suburb.suburbs

      StatusBar.status = "querying danger map..."
      Suburb.loadDangerMap() // TODO debug this blocking the rest of the load

      // load blocks
      StatusBar.status = "loading block data..."
      Block.blocks
    } map { _ =>
      StatusBar.status = "loading character info..."
      UrbanDeadModel.loadCharacters()
    } map { characters =>
      StatusBar.status = "assembling character UI..."
      charactersPane = new CharactersPane
      charactersPane.characters ++= characters.getOrElse(ListBuffer.fill(3)(None)).zipWithIndex.map { session =>
        new CharacterBox(session._1, session._2)
      }

      for(i <- charactersPane.characters.size() until 3) {
        charactersPane.characters += new CharacterBox(None, i)
        UrbanDeadModel.sessions += None
      }

      StatusBar.status = "finalising..."
      UIModel.state.onChange { (_, _, _) => modelStateChanged()}
      UIModel.onUpdateUI = Some(() => update())
      modelStateChanged()

      StatusBar.status = "a ghost approaches the terminal"
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
          val session = UrbanDeadModel.activeSession.get

          sessionBar = new CharacterBar(session)

          eventsCatchupBox = new EventsCatchupBox(session)
          environmentBox = new EnvironmentBox(session)
          actionButtonBox = new ActionButtonBox(session)

          centreVBox.alignment = Pos.TopCenter
          centreVBox.children = List(environmentBox, actionButtonBox)

          mapBox = new MapBox(session)

          top = sessionBar
          left = mapBox
          right = eventsCatchupBox
          center = centreVBox
          bottom = statusBar
      }
    })
  }

  def update(): Unit = {
    charactersPane.update()
    sessionBar.update()
    mapBox.update()
    eventsCatchupBox.update()
    environmentBox.update()
  }

  init()
}