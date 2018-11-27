package com.archmage.ghostmind.view

import com.archmage.ghostmind.model._
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseButton
import scalafx.scene.layout._
import scalafx.scene.text.TextAlignment

class CharacterBox(var session: Option[CharacterSession] = None, val index: Int) extends VBox {

  alignment = Pos.Center
  padding = Insets(5)
  spacing = 10
  prefWidth = 180
  prefHeight = 210

  var deleteConfirm = false

  background <== when(hover) choose Colors.hoverBackground otherwise Colors.normalBackground

  onMouseExited = _ => {
    if(deleteConfirm) {
      deleteConfirm = false
      update()
    }
  }

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
  var loginBox = new LoginVBox(login, loginComplete)

  val avatar = new ImageView {
    fitWidth = 90
    fitHeight = 90
  }
  val mailIcon = new MailIcon
  val deleteButton = new Button {
    id = "RedXButton"
    text = "X"
    onMouseReleased = _ => {
      if(deleteConfirm) {
        UrbanDeadModel.sessions(index) = None
        UrbanDeadModel.saveCharacters()

        session = None
        loginBox = new LoginVBox(login, loginComplete)

        deleteConfirm = false
      }
      else {
        deleteConfirm = true
      }
      update()
    }
  }
  val topStackPane = new StackPane {
    alignment = Pos.TopCenter
    children = List(
      avatar, new HBox {
        alignment = Pos.TopCenter
        padding = Insets(0, 2, 0, 2)
        children = List(mailIcon, new Region { hgrow = Priority.Always }, deleteButton)
      }
    )
  }

  val groupLabel = new Label {
    id = "Subtitle"
    text = ""
    padding = Insets(-10, 0, 0, 0)
  }

  val nameplate = new CharacterNameplate()

  val status = new Label {
    id = "CharacterBoxStatusText"
    style = "-fx-text-fill: #ff0000;"
    text = "OFFLINE"
    text.onChange { (_, _, _) => update() }
  }

  def update(): Unit = {
    Platform.runLater(() => {
      if(session.isEmpty) {
        id = "DottedGreyBorder"
        children = List(plusIcon, addCharacterLabel)
        onMouseClicked = event => {
          if(event.getButton == MouseButton.PRIMARY) children = loginBox
        }
      }
      else {
        session.get.state.onChange { (_, _, _) => update() }
        id = "SolidGreyBorder"
        try {
          avatar.image = new Image(getClass.getResourceAsStream(s"assets/${session.get.username}.png"))
        }
        catch {
          case _: Exception =>
            avatar.image = new Image(getClass.getResourceAsStream("assets/human-icon.png"))
        }

        val groupString = if(session.get.attributes.isEmpty) "[unknown group]"
                          else s"[${
          if(session.get.attributes.get.group == "none") "no group" else session.get.attributes.get.group
        }]"
        if(deleteConfirm) {
          nameplate.details.visible = false
          groupLabel.id = "RedWarnText"
          groupLabel.text = "click again to\ndelete this character"
        }
        else {
          nameplate.details.visible = true
          groupLabel.id = "Subtitle"
          groupLabel.text = groupString
        }
        status.text = session.get.state.value.toString.dropRight(2).toUpperCase
        session.get.state.value match {
          case Offline() => status.style = "-fx-text-fill: #ff0000;"
          case Connecting() => status.style = "-fx-text-fill: #ffff00;"
          case Online() => status.style = "-fx-text-fill: #00ff00;"
        }
        mailIcon.visible = session.get.events.isDefined
        if(session.get.events.isDefined) mailIcon.mailCount.text = session.get.events.get.size.toString
        children = List(topStackPane, nameplate, groupLabel, status)

        onMouseClicked = event => {
          if(event.getButton == MouseButton.PRIMARY) session.get.state.value match {
            case Offline() =>
              new Thread(() => login(session.get.username, session.get.password)).start()
            case Connecting() => ()
            case Online() => startSession()
          }
          else if(event.getButton == MouseButton.SECONDARY) {
            if(session.get.state.value == Online()) logout()
          }
        }
      }
      nameplate.session = session
      nameplate.update()
    })
  }

  def login(username: String, password: String): Unit = {
    if(session.isEmpty) {
      session = Some(CharacterSession(username, password))
      update()
    }
    val result = UrbanDeadModel.loginExistingSession(session.get, index)
    if(!result) {
      Platform.runLater(() => children = loginBox)
    }
  }

  def loginComplete(): Unit = {
    session.get.state.value = Online()
  }

  def startSession(): Unit = {
    UrbanDeadModel.activeSession = session
    UIModel.state = Main()
  }

  def logout(): Unit = {
    session.get.resetBrowser()
    StatusBar.status = s"""logged out as "${session.get.username}""""
    session.get.state.value = Offline()
  }

  update()
}
