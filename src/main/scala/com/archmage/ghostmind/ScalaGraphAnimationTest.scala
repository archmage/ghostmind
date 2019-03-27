package com.archmage.ghostmind

import com.archmage.ghostmind.model.Constants
import scalafx.Includes._
import scalafx.animation.{Interpolator, Timeline}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.Pane
import scalafx.scene.paint.{Color, CycleMethod, LinearGradient, Stop}
import scalafx.scene.shape.{Circle, Line}
import scalax.collection.Graph

import scala.language.postfixOps

object ScalaGraphAnimationTest extends JFXApp {

  def generate(): (List[Circle], List[Line]) = {
    val sampleGraph = ScalaGraphTest.propagateGraph(Graph(), ScalaGraphTest.propagateFromRandomNode)

    val colours = (0 until sampleGraph.nodes.size).map { _ =>
      Color.hsb(
        Constants.rng.nextDouble() * 360,
        Constants.rng.nextDouble() / 2 + 0.5,
        1)
    }

    val nodes: List[Circle] = sampleGraph.nodes.map { node =>
      new Circle {
        centerX = 45 + Constants.rng.nextInt(640 - 90)
        centerY = 45 + Constants.rng.nextInt(480 - 90)
        radius = 5
        fill = colours(node.value - 1)
      }
    }.toList

    val edges: List[Line] = sampleGraph.edges.map { edge =>
      val line = new Line {
        println(s"$edge ${}")
        startX <==> nodes(edge._1 - 1).centerX
        startY <==> nodes(edge._1 - 1).centerY
        endX <==> nodes(edge._2 - 1).centerX
        endY <==> nodes(edge._2 - 1).centerY
        stroke = LinearGradient.apply(startX.value, startY.value, endX.value, endY.value, false, CycleMethod.NoCycle,
          List(Stop(0, colours(edge._1.value - 1)), Stop(1, colours(edge._2.value - 1))))
      }

      line

    }.toList

    (nodes, edges)
  }

  def generatePane() = {
    val graph = generate()

    val timeline = new Timeline {
      cycleCount = Timeline.Indefinite
      autoReverse = true
      keyFrames = graph._1.flatMap { node =>
        List(at(2 s) { node.centerX -> (node.centerX.value - 50 + Constants.rng.nextInt(100)) tween Interpolator.EaseBoth },
         at(2 s) { node.centerY -> (node.centerY.value - 50 + Constants.rng.nextInt(100)) tween Interpolator.EaseBoth })
      }
    }

    timeline.play()

    new Pane {
      children = graph._2 ::: graph._1
    }
  }

  def initialise(): Unit = {
    stage = new PrimaryStage {
      width = 640
      height = 480
      scene = new Scene {
        content = generatePane()
        fill = Color.Black
        onMouseClicked = { _ => content = generatePane() }
      }
    }
  }

  initialise()
}
