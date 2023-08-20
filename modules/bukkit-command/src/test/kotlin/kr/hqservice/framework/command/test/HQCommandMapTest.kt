package kr.hqservice.framework.command.test

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import kr.hqservice.framework.global.core.extension.print
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class HQCommandMapTest {
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

    class TestTree {
        class TestInnerTree {
            class TestInnerInnerTree
        }
        class TestInnerTree2 {
            class TestInnerInnerTree2
            class TestInnerInnerTree3 {
                class TestInnerInnerInnerTree
            }
        }
    }

    @Test
    fun nestedClassSearchTest() {
        findTreeAll(TestTree::class).forEach { kClass1, kClass2 ->
            println("1: ${kClass1.simpleName}, 2: ${kClass2.simpleName}")
        }
    }

    protected fun findTreeAll(kClass: KClass<*>): Multimap<KClass<*>, KClass<*>> {
        val result = ArrayListMultimap.create<KClass<*>, KClass<*>>()
        fun findTreeAll(kClass: KClass<*>, result: Multimap<KClass<*>, KClass<*>>) {
            for (child in kClass.nestedClasses) {
                result[kClass].add(child)
                findTreeAll(child, result)
            }
        }
        findTreeAll(kClass, result)
        return result
    }

}