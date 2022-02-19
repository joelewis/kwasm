package kwasm

// import kotlinx.serialization.*
// import kotlinx.serialization.json.*

@SymbolName("sayHello")
external fun sayHello(idPtr: Int, idLen: Int): Unit

fun hello(): String = "Hello, Joe Lewis!"

abstract class Node() {
    abstract var children : MutableList<Node>?
}

interface Props

interface State

abstract class Component(
    var props: Props? = null,
    var eventCallbacks: HashMap<String, () -> Unit>? = HashMap<String, () -> Unit>(),
    override var children: MutableList<Node>? = mutableListOf<Node>()
) : Node() {
    abstract fun render() : Node

    fun stateChange(stateMutation: () -> Unit) {
        stateMutation.invoke()
        render()
    }
}


class DOMElement(
    var tag: String, 
    var attributes: HashMap<String, String>?, 
    var key: String = "", 
    override var children: MutableList<Node>?
) : Node()

class TextElement(var text: String = "", override var children : MutableList<Node>? = null) : Node()


fun k(
    tag: String,
    attributes: HashMap<String, String>? = HashMap<String, String>(),
    children: MutableList<Node>? = mutableListOf<Node>()
) : DOMElement {
    var domElement = DOMElement(tag, attributes, children=children)
    return domElement
}

fun k(text: String) : TextElement {
    return TextElement(text)
}

fun expandNode(node: Node) : Node {
    if (isComponent(node)) {
        // expand root child
        val component = node as Component
        return expandNode(component.render())

    } else if (isDOMElement(node)) {
        // expand children if present and then return node
         if (node.children != null) {
             var expandedNodes = mutableListOf<Node>()
             for (child in node.children!!) {
                expandedNodes.add(expandNode(child))
             }
             node.children = expandedNodes
         }
        return node
    } else if (isTextElement(node)) {
        // do not expand
        return node
    } else {
        throw Exception("did not expect this node type")
    }
}

fun renderHTML(node: Node) : Node {
    return expandNode(node)
}



fun printTree(node: Node, padding: Int = 0) {
    var pad = ""
    for (i in 0 until padding) {
        pad = "$pad "
    }
    if (node is DOMElement) {
        println(pad + node.tag)
        if (node.children != null) {
            for (child in node.children!!) {
                printTree(child, padding+2)
            }
        }
    } else if (node is TextElement) {
        println(pad + node.text)
    }
}

//fun <T> attr(hashmap: HashMap<String, String>) : HashMap<T, T> {
//    return hashmap
//}

//fun <T> k(
//    component: KClass,
//    props: Props? = null,
//    callbackMap: HashMap<String, () -> Unit> = HashMap<String, () -> Unit>(),
//    children: MutableList<Node> = mutableListOf<Node>()
//) : T {
//}



class MyApp(
    props: Props?,
    eventCallbacks: HashMap<String, () -> Unit>?,
    children: MutableList<Node>?
) : Component(props, eventCallbacks, children) {

    var count = 0

    fun inc() {
        stateChange {
            count++
        }
    }

    fun dec() {
        stateChange {
            count--
        }
    }

    override fun render() : Node {
        return k("div", hashMapOf("class" to "container"), mutableListOf(
            MyButton(null, hashMapOf("inc" to this::inc ), mutableListOf(k("increment"))),
            k("div", hashMapOf("class" to "count"), mutableListOf(k("count: $count"))),
            MyButton(null, hashMapOf("inc" to this::dec ), mutableListOf(k("decrement")))
        ))
    }

}

class MyButton(
    props: Props? = null,
    eventCallbacks: HashMap<String, () -> Unit>? = null,
    children: MutableList<Node>? = null
) : Component(props, eventCallbacks, children) {

    override fun render() : Node {
        return k("div", children=children)
    }
}


class Dog {

    var callbacks: HashMap<String, () -> Unit > = HashMap<String, () -> Unit >()

    fun bark() {
        println("Woof, Woof!")
        var str = "Hello, World!"
        // sayHello(stringPointer(str), stringLengthBytes(str))
    }
}

// abstract class Component {
//     // spec for a component
//     abstract val children: MutableList<Component>
// }

// class MyButton(props) {

//     this.eventMap = mergeMap(
//         mapOf(".inc click", onClick)
//     )

//     fun render() {
//         return createElement('button', className="inc", attributes={}, children=listOf(["increment"]))
//     }
// }

// class MyApp(val count: Int) : Kwasm.Component {
//     this.state = count

//     fun render() {
//         return Kwasm.createElement('div', className="container", attributes={}, children=listOf([
//             Kwasm.createElement(MyButton, callbackMap=this.onIncrement),
//             Kwasm.createElement('div', className="count-display", children=listOf([""+count])),
//             Kwasm.createElement(MyButton, callbackMap=this.onDecrement)
//         ]))
//     }
// }

/*

1. a tree notation built with instances of Nodes
2. a Node class that defines the structure of what should happen when an instance is created out of it.




*/


/*
*       n('div',
            children = listOf(h('button', {
                onclick: function() {
                    increment()
                }
            }),
            n(Info, {
                props: {
                    text: count
                }
            })
            n('button', {
                onclick: function() {
                    decrement()
                }
            })),
        )
*
*/

// fun h(tag: String, children: MutableList<Component>): Component? {
//     return null
// }

// fun h(tag: String, text: String): Component? {
//     return null
// }

// fun h(component: Component, children: MutableList<Component>): Component? {
//     return null;
// }


fun isComponent(node: Node) : Boolean {
    return node is Component
}

fun isDOMElement(node: Node) : Boolean {
    return node is DOMElement
}

fun isTextElement(node: Node) : Boolean {
    return node is TextElement
}

fun main() {
    val dog = Dog()
    dog.callbacks.put("bark", dog::bark)
    dog.callbacks.get("bark")?.invoke()
    println(dog.bark())
    printTree(renderHTML(MyApp(null, null, null)))
    // if (isComponent(MyButton(null, hashMapOf("inc" to {} ), mutableListOf(k("increment"))))) {
    //     println("yes!")
    // } else {
    //     println("No!")
    // }
}