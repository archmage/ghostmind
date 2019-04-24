package com.archmage.ghostmind.view.metaelements

import com.archmage.ghostmind.model.{CharacterSession, UrbanDeadModel}
import com.archmage.ghostmind.view.components.GhostField
import com.archmage.ghostmind.view.{StatusBar, UIModel, Updateable}
import javafx.event.{ActionEvent, EventHandler}
import net.ruippeixotog.scalascraper.model.Document
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{FlowPane, HBox, Priority, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Text, TextFlow}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ActionButtonBox(session: CharacterSession) extends VBox with Updateable {
  alignment = Pos.TopLeft
  padding = Insets(10)
  spacing = 5

  val speechClosure: EventHandler[ActionEvent] = _ => speech()
  val searchClosure: EventHandler[ActionEvent] = _ => search()

  val speechField = new GhostField {
    hgrow = Priority.Always
    prefWidth = 290
    promptText = "say something"
    onAction = speechClosure
  }

  val speechSubmitButton = new Button {
    text = "say"
    onAction = speechClosure
  }

  val searchButton = new Button {
    text = "search"
    onAction = searchClosure
  }

  // TODO disable ones that won't do anything
  val movementButtons = List(
    (-1, -1, "NW"), (0, -1, "N"), (1, -1, "NE"),
    (-1, 0, "W"), (0, 0, ""), (1, 0, "E"),
    (-1, 1, "SW"), (0, 1, "S"), (1, 1, "SE")
  ).map { data => new Button {
    text = data._3
    prefWidth = 50
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
      UrbanDeadModel.processMapCgi(response.get, session)
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

  val inventoryBox = new InventoryBox(session)

  val mainFlowPane = new FlowPane {
    hgap = 5
    vgap = 5
    children = List(new HBox(speechField, speechSubmitButton), searchButton)
  }

  val actionContainer = new HBox {
    spacing = 5
    children = List(movementButtonsBox, mainFlowPane)
  }

  children = List(actionContainer, inventoryBox)

  def update(): Unit = {
    inventoryBox.update()
  }
}
