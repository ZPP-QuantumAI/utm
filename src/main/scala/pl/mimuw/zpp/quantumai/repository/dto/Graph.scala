package pl.mimuw.zpp.quantumai.repository.dto

import org.mongodb.scala.bson.ObjectId

final case class Graph(_id: ObjectId, id: String, name: String, nodes: Seq[Node])
final case class Node(x: Double, y: Double)

object Graph {
  def apply(id: String, name: String, nodes: Seq[Node]): Graph =
    Graph(new ObjectId(), id, name, nodes)

  def toInput(graph: Graph): String = {
    val nodeStrings = graph.nodes.map { node =>
      s"${node.x} ${node.y}"
    }
    s"${graph.nodes.size}\n${nodeStrings.mkString("\n")}"
  }
}
