package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.UrbanDeadModel
import javafx.event.EventHandler
import javafx.event.ActionEvent
import scalafx.geometry.Pos
import scalafx.scene.control.{Button, Label, PasswordField, TextField}
import scalafx.scene.layout.HBox

class LoginBar extends HBox {

  val showPromptTextStyle = "-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%);"

  val usernameField = new TextField
  val passwordField = new PasswordField
  val loginLogoutButton = new Button
  val loggedInAsLabel = new Label

  val loginClosure: EventHandler[ActionEvent] = _ => login()
  val logoutClosure: EventHandler[ActionEvent] = _ => logout()

  alignment = Pos.CenterRight
  spacing = 10

  usernameField.promptText = "username"
  passwordField.promptText = "password"
  loginLogoutButton.text = "login"

  usernameField.style = showPromptTextStyle
  passwordField.style = showPromptTextStyle

  usernameField.onAction = loginClosure
  passwordField.onAction = loginClosure
  loginLogoutButton.onAction = loginClosure

  this.getChildren.addAll(usernameField, passwordField, loginLogoutButton)

  def login(): Unit = {
    val username = usernameField.text.value.trim
    val password = passwordField.text.value.trim

    if(username.isEmpty || password.isEmpty) return
    this.getChildren.clear()
    new Thread(() => {
      val contactsListResponse = UrbanDeadModel.loadContactsList(username, password)
      UrbanDeadModel.parseContactList(contactsListResponse.body)
    }).run()

    loggedInAsLabel.text = s"logged in as $username"
    loginLogoutButton.text = "logout"
    loginLogoutButton.onAction = logoutClosure
    this.getChildren.addAll(loggedInAsLabel, loginLogoutButton)
  }

  def logout(): Unit = {
    this.getChildren.clear()
    loginLogoutButton.text = "login"
    loginLogoutButton.onAction = loginClosure
    this.getChildren.addAll(usernameField, passwordField, loginLogoutButton)
    usernameField.requestFocus()
    usernameField.selectEnd()
    usernameField.deselect()
  }
}
