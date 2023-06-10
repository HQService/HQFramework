package kr.hqservice.framework.command.test

import kr.hqservice.framework.global.core.extension.print
import org.junit.jupiter.api.Test

class HQCommandTreeTest {
    @Test
    fun test() {
        val rootNode = Node(1, listOf(Node(2, listOf(Node(4, listOf(Node(5, listOf()))))), Node(3, emptyList())))
        val allNodes = mutableListOf<Node>()
        getAllNodes(rootNode, allNodes)
        allNodes.forEach {
            it.print()
        }
    }

    data class Node(val value: Int, val children: List<Node>)

    fun getAllNodes(node: Node, acc: MutableList<Node>) {
        acc.add(node)
        for (child in node.children) {
            getAllNodes(child, acc)
        }
    }
}