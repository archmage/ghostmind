package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import javafx.event.{ActionEvent, EventHandler}
import net.ruippeixotog.scalascraper.model.Document
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Button
import scalafx.scene.layout.{FlowPane, HBox, Priority, VBox}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ActionButtonBox(session: CharacterSession) extends FlowPane {
  alignment = Pos.TopLeft
  padding = Insets(10)
  hgap = 5
  vgap = 5
  hgrow = Priority.Always

  val speechClosure: EventHandler[ActionEvent] = _ => speech()
  val searchClosure: EventHandler[ActionEvent] = _ => search()

  val speechField = new GhostField {
    hgrow = Priority.Always
    prefWidth = 300
    promptText = "say something"
    onAction = speechClosure
  }

  val speechSubmitButton = new Button {
    text = "speak your mind"
    onAction = speechClosure
  }

  val searchButton = new Button {
    text = "search"
    onAction = searchClosure
  }

  // TODO disable ones that won't do anything
  val movementButtons = List(
    (-1, -1, "NW"), (0, -1, "N"), (1, -1, "NE"),
    (-1, 0, "W"), (0, 0, "."), (1, 0, "E"),
    (-1, 1, "SW"), (0, 1, "S"), (1, 1, "SE")
  ).map { data => new Button {
    text = data._3
    prefWidth = 45
    focusTraversable = false
    onAction = _ => move(data._1, data._2)
    // disable = (if data coords + session.coords is outside bounds!)
  }}

  movementButtons(4).disable = true

  val movementButtonsBox = new VBox {
    children = List (
      new HBox { children = movementButtons.slice(0, 3) },
      new HBox { children = movementButtons.slice(3, 6) },
      new HBox { children = movementButtons.slice(6, 9) },
    )
  }

  def performAction(startMessage: String, endMessage: String, action: () => Option[Document]): Unit = {
    StatusBar.status = startMessage
    Future[Option[Document]] {
      action.apply()
    } map { response =>
      UrbanDeadModel.parseMapCgi(response.get, session)
    } map { _ =>
      Platform.runLater(() => {
        UIModel.updateUI()
        StatusBar.status = endMessage
      })
    }
  }

  def speech(): Unit = {
    val message = speechField.text.value.trim
    speechField.text.value = ""
    performAction("raising our voice...", "done speaking",
      () => UrbanDeadModel.speakAction(message, session))
  }

  def search(): Unit = {
    performAction("searching for an item...", "done searching",
      () => UrbanDeadModel.searchAction(session))
  }

  def move(x: Int, y: Int): Unit = {
    performAction("moving...", "done moving",
      () => UrbanDeadModel.moveAction(session, x, y))
  }

  children = List(searchButton, speechField, speechSubmitButton, movementButtonsBox)
}
