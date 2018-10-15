import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.graph.DefaultEdge
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

const val N = 8
const val K = 3

data class RBNVertex(val id : Int, var value: Boolean, var inputs : Int, val function : Long)

// Utility function to get the nth bit of an integer. Useful for getting the next state from a node's boolean
// function given it's
fun pthBitOfQ(p: Int, q: Long) : Boolean {
    return q shr (p - 1) and 1 == 1.toLong()
}

fun printState(vertices : ArrayList<RBNVertex>) {
    for (vertex in vertices) {
        if (vertex.value == true) {
            print(1)
        }
        else {
            print(0)
        }
    }
    println()
}

fun main(args: Array<String>) {

    // Set up the graph
    println("Creating RBN with N = $N, K = $K")
    val graph = SimpleDirectedGraph<RBNVertex, DefaultEdge>(DefaultEdge::class.java)
    val vertices : ArrayList<RBNVertex> = ArrayList(N)

    // Create the vertices and add them to an arraylist so that they we have direct access
    for (i in 0..N) {
        val booleanFunction = ThreadLocalRandom.current().nextLong(2.0.pow(2.0.pow(K)).toLong())
        val vertex = RBNVertex(id=i, value=false, inputs=-1, function=booleanFunction)
        vertex.value = ThreadLocalRandom.current().nextBoolean()
        println(vertex)
        graph.addVertex(vertex)
        vertices.add(vertex)
    }

    // Create the random edges
    for (toVertex in vertices) {
        // For each vertex we select K other vertices from which to
        // create an edge.
        // The best way to do it is to shuffle the vertices and then
        // choose the first K, skipping one when fromVertex == toVertex.
        val indices = (0..N).shuffled()
        var index = 0
        var edgesCreated = 0
        while (edgesCreated < K) {
            val fromVertex = vertices[indices[index]]
            if (fromVertex != toVertex) {
                // No loops allowed
                graph.addEdge(fromVertex, toVertex)
                println("Vertex added from $fromVertex to $toVertex")
                edgesCreated += 1
            }
            index += 1
        }
    }

    // Calculate the next values of each node
    for (thisVertex in vertices) {
        assert(graph.inDegreeOf(thisVertex) == K)
        // Each vertex is going to have K inputs which we have to consider in order.
        // The built-in function incomingEdgesOf() returns a set, which is unordered.
        // Hence we have to loop through the arraylist from 0 to N-1
        var bitPosition = K-1
        var sum = 0
        for (thatVertex in vertices) {
            if (thisVertex != thatVertex && graph.getEdge(thatVertex, thisVertex) != null) {
                if (thatVertex.value == true) {
                    sum += 2.0.pow(bitPosition).toInt()
                }
                bitPosition -= 1
            }
        }
        assert(bitPosition == 0)
        thisVertex.inputs = sum
        println("$thisVertex $sum")
    }

    printState(vertices)

    // Change the values of the nodes
    for (vertex in vertices) {
        vertex.value = pthBitOfQ(p=vertex.inputs, q=vertex.function)
    }
    printState(vertices)

/*
  Note that the default graph implementations guarantee predictable ordering for the collections that they maintain; so, for example, if you add vertices in the order [B, A, C], you can expect to see them in that order when iterating over the vertex set. However, this is not a requirement of the Graph interface, so other graph implementations are not guaranteed to honor it.

    Assigning random boolean functions
    ==================================
    Assign random boolean functions to each vertex (this should be done on instantiation).
    There are K inputs to each node and so the boolean function is a random number from 0 to K-1.
    Example with K=3:
    Possible inputs : output
    0 0 0 : 1
    0 0 1 : 0
    0 1 0 : 0
    0 1 1 : 1
    1 0 0 : 1
    1 0 1 : 0
    1 1 0 : 0
    1 1 1 : 1
    E.g. inputs of 0 1 1 give output of 1, inputs of 1 1 0 give output of 0, etc.
    The output column forms the number 10011001, which is 128 + 16 + 8 + 1 = 153.
    So the node's boolean function is summarised by a denary number from 0 - 2^(2^K)-1.
    (K inputs means 2^K possible sets of inputs means 2^(2^K) bits in the boolean function.
*/

}