package kr.hqservice.framework.command.test

import kr.hqservice.framework.global.core.extension.print
import org.junit.jupiter.api.Test

class HQCommandTreeTest {
    @Test
    fun test() {
        val rootNode = Node(
            1,
            listOf(
                Node(
                    2,
                    listOf(
                        Node(4, listOf(Node(5, listOf()))),
                        Node(6, listOf(Node(7, listOf())))
                    )
                ),
                Node(3),
                Node(
                    8, listOf(
                        Node(9)
                    )
                ),
            )
        )
        val allNodes = mutableListOf<Node>()
        getAllNodes(rootNode, allNodes)
        allNodes.forEach {
            it.print()
        }
        rootNode.printNode()
    }

    data class Node(val value: Int, val children: List<Node> = listOf()) {
        fun printNode(printer: String = "") {
            for ((index, child) in children.withIndex()) {
                val lastNode = index + 1 == children.size
                val iia = if (lastNode) "└─ " else "├─ "
                println(printer + iia + child.value)
                child.printNode(printer + if (!lastNode) "│ " else "  ")
            }
        }
    }

    fun getAllNodes(node: Node, acc: MutableList<Node>) {
        acc.add(node)
        for (child in node.children) {
            getAllNodes(child, acc)
        }
    }
}