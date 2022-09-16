package gitinternals

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Commit(
    var hash: String = "",
    var merged: Boolean = false,
    var parents: MutableList<String> = mutableListOf(),
    var tree: String = "",
    private var author: String = "",
    private var committer: String = "",
    private var commitMessage: MutableList<String> = mutableListOf()
) {

    fun printFull() {
        if (hash.isNotBlank()) {
            println("hash: $hash")
        }
        if (tree.isNotBlank()) {
            println("tree: $tree")
        }
        if (parents.isNotEmpty()) {
            println(parents.joinToString(prefix = "parents: ", separator = " | "))
        }
        if (author.isNotBlank()) {
            println("author: $author")
        }
        if (committer.isNotBlank()) {
            println("committer: $committer")
        }
        if (commitMessage.isNotEmpty()) {
            println("commit message:")
            commitMessage.forEach { println(it) }
        }
    }

    fun printSummary() {
        if (hash.isNotBlank()) {
            if (merged) {
                println("Commit: $hash (merged)")
            } else {
                println("Commit: $hash")
            }
        }
        if (committer.isNotBlank()) {
            println(committer)
        }
        if (commitMessage.isNotEmpty()) {
            commitMessage.forEach { println(it) }
        }
    }

    fun addInfoToCommit(line: String) {
        if (line.isBlank()) {
            return
        }
        val (first, _) = line.split(" ", limit = 2)
        when (first) {
            "tree" -> this.tree = line.split(" ", limit = 2)[1]
            "parent" -> this.parents.add(line.split(" ", limit = 2)[1])
            "author" -> {
                val (type, name, email, seconds, zone) = line.split(" ")
                this.author = "$name ${formatEmail(email)} ${formatTimestamp(type, seconds, zone)}"
            }

            "committer" -> {
                val (type, name, email, seconds, zone) = line.split(" ")
                this.committer = "$name ${formatEmail(email)} ${formatTimestamp(type, seconds, zone)}"
            }

            else -> this.commitMessage.add(line)
        }
    }

    private fun formatEmail(str: String): String {
        // <email> to email
        return str.substring(1, str.length - 1)
    }

    private fun formatTimestamp(type: String, seconds: String, zone: String): String {
        val timestamp = Instant.ofEpochSecond(seconds.toLong()).atZone(ZoneOffset.of(zone))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx"))
        var prefix = ""
        if (type == "author") {
            prefix = "original"
        } else if (type == "committer") {
            prefix = "commit"
        }
        return "$prefix timestamp: $timestamp"
    }

}