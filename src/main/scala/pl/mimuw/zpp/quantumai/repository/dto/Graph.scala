package pl.mimuw.zpp.quantumai.repository.dto

final case class Graph(_id: String, name: String, nodes: Seq[Node])
final case class Node(x: String, y: String)

object Graph {
  def apply(id: String, name: String, nodes: Seq[Node]): Graph =
    Graph(id, name, nodes)

  def toInput(graph: Graph): String = {
    val nodeStrings = graph.nodes.map { node =>
      s"${node.x} ${node.y}"
    }
    s"${graph.nodes.size}\n${nodeStrings.mkString("\n")}"
  }
}
