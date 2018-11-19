package com.archmage.ghostmind.view

import com.archmage.ghostmind.model._
import javafx.scene.{layout => jfxsl}
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout._
import scalafx.scene.text.TextAlignment

class CharacterBox(var session: Option[CharacterSession] = None) extends VBox {

  alignment = Pos.Center
  padding = Insets(5)
  spacing = 10
  prefWidth = 180
  prefHeight = 202

  background <== when(hover) choose Colors.hoverBackground otherwise Colors.normalBackground

  // session absent
  val plusIcon = new ImageView{
    image = new Image(getClass.getResourceAsStream("assets/plus.png"))
    fitWidth = 50
    fitHeight = 50
  }
  val addCharacterLabel = new Label {
    id = "Subtitle"
    text = "add a character"
  }
  val loginBox = new LoginVBox(login, loginComplete)

  // session present
  val nameString = if(session.isDefined) session.get.username else "Unknown"
  val levelString = "???"
  val classString = "Mystery"
  val groupString = "[unknown group]"

  val avatar = new ImageView {
    fitWidth = 90
    fitHeight = 90
  }
  val mailLabel = new Label {
    // turn this into a stacked image and label combo
//    id = "WhiteText"
//    text = "(0)"
  }
  val deleteButton = new Button {
    text = "X"
  }
  val topStackPane = new StackPane {
    alignment = Pos.TopCenter
    children = List(avatar, new HBox {
        children = List(mailLabel, new Region { hgrow = Priority.Always }, deleteButton)
      }
    )
  }
  val name = new Label {
    id = "Title"
    text = nameString
  }
  val details = new Label {
    id = "Subtitle"
    text = s"the Level $levelString $classString\n$groupString"
    padding = Insets(-10, 0, 0, 0)
    textAlignment = TextAlignment.Center
  }
  val status = new Label {
    id = "CharacterBoxStatusText"
    style = "-fx-text-fill: #ff0000;"
    text = "OFFLINE"
    text.onChange { (_, _, _) => update() }
  }

  def update(): Unit = {
    Platform.runLater(() => {
      if(session.isEmpty) {
        style =
          """-fx-border-style: segments(12, 6);
            |-fx-border-color: grey;
            |-fx-border-width: 2px;
          """.stripMargin
        children = List(plusIcon, addCharacterLabel)
        onMouseReleased = _ => {
          children = loginBox
        }
      }
      else {
        session.get.state.onChange { (_, _, _) => update() }
        try {
          avatar.image = new Image(getClass.getResourceAsStream(s"assets/${session.get.username}.png"))
        }
        catch {
          case _: Exception =>
            avatar.image = new Image(getClass.getResourceAsStream("assets/human-icon.png"))
        }
        name.text = session.get.username
        status.text = session.get.state.value.toString.dropRight(2).toUpperCase
        session.get.state.value match {
          case Offline() => status.style = "-fx-text-fill: #ff0000;"
          case Connecting() => status.style = "-fx-text-fill: #dddd00;"
          case Online() => status.style = "-fx-text-fill: #00ff00;"
        }
        children = List(topStackPane, name, details, status)
        onMouseReleased = _ => {
          session.get.state.value match {
            case Offline() =>
              new Thread(() => login(session.get.username, session.get.password)).start()
            case Connecting() => ()
            case Online() => startSession()
          }
        }
      }
    })
  }

  def login(username: String, password: String): Unit = {
    if(session.isEmpty) {
      session = Some(CharacterSession(username, password))
      update()
    }
    val result = UrbanDeadModel.loginExistingSession(session.get)
    if(!result) {
      children = loginBox
    }
  }

  def loginComplete(): Unit = {
    session.get.state.value = Online()
    update()

    onMouseReleased = _ => startSession()
  }

  def startSession(): Unit = {
    UrbanDeadModel.activeSession = session
    UIModel.state = Main()
  }

  update()
}
