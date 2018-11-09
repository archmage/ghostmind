package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.UrbanDeadModel
import javafx.event.{ActionEvent, EventHandler}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, PasswordField}
import scalafx.scene.layout.HBox

class LoginBox extends HBox {

  alignment = Pos.CenterRight
  spacing = 10
  padding = Insets(10)

  val usernameField = new GhostField
  val passwordField = new PasswordField
  val loginLogoutButton = new Button
  val loggedInAsLabel = new Label

  val loginClosure: EventHandler[ActionEvent] = _ => login()
  val logoutClosure: EventHandler[ActionEvent] = _ => logout()

  usernameField.promptText = "username"
  passwordField.promptText = "password"
  loginLogoutButton.text = "login"
  passwordField.style = GhostField.showPromptTextStyle

  usernameField.onAction = loginClosure
  passwordField.onAction = loginClosure
  loginLogoutButton.onAction = loginClosure

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
    this.getChildren.addAll(usernameField, passwordField, loginLogoutButton)
    usernameField.requestFocus()
    usernameField.selectEnd()
    usernameField.deselect()
  }
}
