package com.archmage.ghostmind

import com.archmage.ghostmind.model.Constants
import scalax.collection.Graph
import scalax.collection.GraphEdge._
import scalax.collection.GraphPredef._

object ScalaGraphTest extends App {

  type NewEdgeSourceLogic = Graph[Int, UnDiEdge] => Int

  def propagateFromRandomNode(graph: Graph[Int, UnDiEdge]): Int = {
    graph get (Constants.rng.nextInt(graph.nodes.size) + 1)
  }

  def propagateGraph(graph: Graph[Int, UnDiEdge] = Graph(),
                     newEdgeSourceLogic: NewEdgeSourceLogic = propagateFromRandomNode): Graph[Int, UnDiEdge] = {
    if(graph.nodes.size >= 30) graph
    else {
      if(graph.isEmpty) propagateGraph(Graph(1~2, 2~3), newEdgeSourceLogic)
      else {
        val newEdgeSource: Int = newEdgeSourceLogic(graph)
        val newEdge = newEdgeSource~(graph.nodes.size + 1)
        propagateGraph(graph + newEdge, newEdgeSourceLogic)
      }
    }
  }

  println(propagateGraph(Graph(), propagateFromRandomNode))
}