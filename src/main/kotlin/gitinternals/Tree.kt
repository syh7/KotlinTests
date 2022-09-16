package gitinternals

class Tree(
    val objects: MutableList<TreeObject> = mutableListOf(),
    private val subTrees: MutableList<Tree> = mutableListOf(), var folder: String = ""
) {
    fun printFull() {
        objects.forEach { it.printFull() }
    }

    fun printSummary(parentFolder: String) {
        objects.forEach { it.printSummary("$parentFolder$folder") }
        subTrees.forEach { it.printSummary("$parentFolder$folder") }
    }
}

class TreeObject(val hash: String, private val metaDataNumber: String, val fileName: String) {
    fun printFull() {
        println("$metaDataNumber $hash $fileName")
    }

    fun printSummary(prefix: String) {
        println("$prefix$fileName")
    }
}

fun tree(bytes: List<Byte>): Tree {
    val tree = Tree()
    var bytesLeft = bytes
    do {
        val whitespaceIndex = bytesLeft.indexOfFirst { it.toInt().toChar().isWhitespace() }
        val nullIndex = bytesLeft.indexOfFirst { it.toInt() == 0 }

        val metaDataNumber = bytesLeft.filterIndexed { index, _ -> index in 0 until whitespaceIndex }
            .joinToString("") { it.toInt().toChar().toString() }
        val fileName = bytesLeft.filterIndexed { index, _ -> index in whitespaceIndex + 1 until nullIndex }
            .joinToString("") { it.toInt().toChar().toString() }
        val hash = bytesLeft.filterIndexed { index, _ -> index in nullIndex + 1..nullIndex + 20 }
            .joinToString("") { String.format("%02x", it) }

        val treeObject = TreeObject(hash, metaDataNumber, fileName)
        tree.objects.add(treeObject)

        bytesLeft = bytesLeft.filterIndexed { index, _ -> index >= nullIndex + 21 }
    } while (bytesLeft.size > 20)
    return tree
}