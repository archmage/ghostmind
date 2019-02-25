package com.archmage.ghostmind.view

import com.archmage.ghostmind.model.{Constants, UrbanDeadModel}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.scene.image.Image


object MainView extends JFXApp {

  // views
  val mainBorderPane = new MainBorderPane

  // setup
  val titleText = s"ghostmind - ${Constants.sessionQuotes(Constants.rng.nextInt(Constants.sessionQuotes.size))}"

  // finalisation
  stage = new PrimaryStage {
    title = titleText
    icons += new Image(getClass.getResourceAsStream("assets/ghost.png"))
    minWidth = 1024
    minHeight = 600
    scene = new Scene {
      stylesheets += this.getClass.getResource("assets/style.css").toExternalForm
      root = mainBorderPane
    }
  }

  override def stopApp(): Unit = {
    UrbanDeadModel.saveAll()
    super.stopApp()
  }
}
