package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.UrbanDeadModel
import javafx.event.{ActionEvent, EventHandler}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, PasswordField}
import scalafx.scene.layout.{Priority, VBox}

class LoginVBox extends VBox {

  alignment = Pos.Center
  spacing = 10
  padding = Insets(10)

  val loginClosure: EventHandler[ActionEvent] = _ => login()
  val logoutClosure: EventHandler[ActionEvent] = _ => logout()

  val usernameField = new GhostField {
    maxWidth = 140
    promptText = "username"
    onAction = loginClosure
    alignment = Pos.Center
  }
  val passwordField = new PasswordField {
    maxWidth = 140
    promptText = "password"
    style = GhostField.showPromptTextStyle
    onAction = loginClosure
    alignment = Pos.Center
  }
  val loginLogoutButton = new Button {
    text = "login"
    onAction = loginClosure
  }
  val loggedInAsLabel = new Label {
    id = "WhiteText"
  }

  children = List(usernameField, passwordField, loginLogoutButton)

  def login(): Unit = {
    val username = usernameField.text.value.trim
    val password = passwordField.text.value.trim

    if(username.isEmpty || password.isEmpty) return

    new Thread(() => {
      val contactsListResponse = UrbanDeadModel.loadContactsList(username, password)
      UrbanDeadModel.parseContactList(contactsListResponse.body)
    }).run()

    loggedInAsLabel.text = s"logged in as $username"
    loginLogoutButton.text = "logout"
    loginLogoutButton.onAction = logoutClosure
    children = List(loggedInAsLabel, loginLogoutButton)
  }

  def logout(): Unit = {
    loginLogoutButton.text = "login"
    loginLogoutButton.onAction = loginClosure
    children = List(usernameField, passwordField, loginLogoutButton)
    usernameField.requestFocus()
    usernameField.selectEnd()
    usernameField.deselect()
  }
}
