package gitinternals

import java.io.File
import java.io.FileInputStream
import java.util.zip.InflaterInputStream

// necessary to split in 7 variables
operator fun <T> List<T>.component6(): T = get(5)
operator fun <T> List<T>.component7(): T = get(6)

fun main() {
    println("Enter .git directory location:")
    val directory = readln()
    println("Enter command:")
    when (readln()) {
        "log" -> log(directory)
        "cat-file" -> catFile(directory)
        "commit-tree" -> commitTree(directory)
        "list-branches" -> listBranches(directory)
    }
}

private fun log(directory: String) {
    println("Enter branch name:")
    val branchName = readln()
    val headPath = "$directory/refs/heads/$branchName"
    val firstCommit = File(headPath).readText().removeSuffix("\n")
    val commits = iterativelyRetrieveCommits(directory, firstCommit)
    commits.forEach {
        it.printSummary()
        println()
    }
}

private fun iterativelyRetrieveCommits(directory: String, hash: String): MutableList<Commit> {
    val commits = mutableListOf<Commit>()
    var currentHash = hash
    while (true) {
        val commit = createCommit(directory, currentHash, false)
        commits.add(commit)
        if (commit.parents.isEmpty()) {
            break
        }

        if (commit.parents.size > 1) {
            commits.add(createCommit(directory, commit.parents[1], true))
        }
        currentHash = commit.parents[0]
    }
    return commits
}

private fun createCommit(directory: String, hash: String, merged: Boolean = false): Commit {
    val path = "$directory/objects/${hash.substring(0, 2)}/${hash.substring(2)}"
    val byteArray = InflaterInputStream(FileInputStream(path)).readAllBytes()
    val commit = Commit()
    val headerEnd = byteArray.indexOfFirst { it.toInt() == 0 }
    byteArray.filterIndexed { index, _ -> index > headerEnd }
        .map { if (it.toInt() == 0) "\n" else it.toInt().toChar() }
        .joinToString("")
        .split("\n")
        .forEach { commit.addInfoToCommit(it) }
    commit.hash = hash
    commit.merged = merged
    return commit
}

private fun listBranches(directory: String) {
    val headFile = File("$directory/HEAD")
    val head = headFile.readText().split("\n")[0].split("/").last()

    val refHead = "$directory/refs/heads"
    val refDirectory = File(refHead)
    refDirectory.list()!!.forEach {
        if (it == head) {
            println("* $it")
        } else {
            println("  $it")
        }
    }
}

private fun catFile(directory: String) {
    println("Enter git object hash:")
    val hash = readln()

    val path = "$directory/objects/${hash.substring(0, 2)}/${hash.substring(2)}"
    val byteArray = InflaterInputStream(FileInputStream(path)).readAllBytes()

    val headerEnd = byteArray.indexOfFirst { it.toInt() == 0 }
    val fullHeader = byteArray
        .filterIndexed { index, _ -> index in 0 until headerEnd }
        .map { it.toInt().toChar() }
        .joinToString("")
    val content = byteArray.filterIndexed { index, _ -> index > headerEnd }
    val charContent = content.map { if (it.toInt() == 0) "\n" else it.toInt().toChar() }
        .joinToString("")

    val (type, _) = fullHeader.split(" ")
    println("*${type.uppercase()}*")

    when (type) {
        "blob" -> printBlob(charContent)
        "commit" -> printCommit(charContent)
        "tree" -> printTree(content)
    }
}

private fun commitTree(directory: String) {
    println("Enter commit-hash:")
    val commitHash = readln()
    val commit = createCommit(directory, commitHash)
    val treeHash = commit.tree

    val tree = recursivelyReadTree(directory, treeHash, "")!!
    tree.printSummary("")
}

private fun recursivelyReadTree(directory: String, hash: String, folder: String): Tree? {
    val path = "$directory/objects/${hash.substring(0, 2)}/${hash.substring(2)}"
    val byteArray = InflaterInputStream(FileInputStream(path)).readAllBytes()

    val headerEnd = byteArray.indexOfFirst { it.toInt() == 0 }
    val fullHeader = byteArray
        .filterIndexed { index, _ -> index in 0 until headerEnd }
        .map { it.toInt().toChar() }
        .joinToString("")
    if (fullHeader.split(" ")[0] != "tree") {
        return null
    }

    val content = byteArray.filterIndexed { index, _ -> index > headerEnd }
    val tree = tree(content)
    val actualObjects = mutableListOf<TreeObject>()
    val subTrees = mutableListOf<Tree>()
    for (i in tree.objects.indices) {
        val possibleTree = recursivelyReadTree(directory, tree.objects[i].hash, "${tree.objects[i].fileName}/")
        if (possibleTree != null) {
            subTrees.add(possibleTree)
        } else {
            actualObjects.add(tree.objects[i])
        }
    }

    return Tree(actualObjects, subTrees, folder)
}

private fun printBlob(blob: String) {
    println(blob)
}

private fun printTree(bytes: List<Byte>, printFull: Boolean = true) {
    val tree = tree(bytes)
    if (printFull) {
        tree.printFull()
    }
}

private fun printCommit(commitStr: String) {
    val commit = Commit()
    commitStr.split("\n").forEach { commit.addInfoToCommit(it) }
    commit.printFull()
}
