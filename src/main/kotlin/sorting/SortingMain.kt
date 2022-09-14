package sorting

import java.io.File
import java.util.*

class SortingProperties(val dataType: String, val sortingType: String, val inputFile: String, val outputFile: String)
class SortingException(message: String) : Exception(message)

fun main(args: Array<String>) {
    try {
        val properties = getPropertiesFromArguments(args)
        val output = when (properties.dataType) {
            "long" -> sortLongs(properties.sortingType, properties.inputFile)
            "line" -> readLines(properties.sortingType, properties.inputFile)
            "word" -> readWords(properties.sortingType, properties.inputFile)
            else -> throw SortingException("unknown datatype ${properties.dataType}")
        }
        if (properties.outputFile.isNotBlank()) {
            val file = File(properties.outputFile)
            file.writeText(output.joinToString { "\n" })
        } else {
            output.forEach { println(it) }
        }
    } catch (e: SortingException) {
        println(e.message)
        return
    }
}

private fun getPropertiesFromArguments(args: Array<String>): SortingProperties {
    var dataType = "word"
    var sortingType = "natural"
    var inputFile = ""
    var outputFile = ""

    var i = 0
    while (i < args.size) {
        if (args[i] == "-dataType") {
            try {
                dataType = args[++i]
            } catch (e: Exception) {
                throw SortingException("No data type defined!")
            }
        } else if (args[i] == "-sortingType") {
            try {
                sortingType = args[++i]
            } catch (e: Exception) {
                throw SortingException("No sorting type defined!")
            }
        } else if (args[i] == "-inputFile") {
            inputFile = args[++i]
        } else if (args[i] == "-outputFile") {
            outputFile = args[++i]
        } else {
            println("${args[i]} is not a valid parameter. It will be skipped.")
        }
        i++
    }
    return SortingProperties(dataType, sortingType, inputFile, outputFile)
}

private fun sortLongs(sortingType: String, inputFile: String): MutableList<String> {
    val numbers = readSingleInput(inputFile).mapNotNull { convertToIntAndSkipNonInt(it) }
    val totalNumbers = numbers.size
    val outputString = mutableListOf<String>()
    outputString.add("Total numbers: $totalNumbers.")
    if (sortingType == "natural") {
        outputString.add("Sorted data: ${numbers.sorted().joinToString(" ")}")
    } else {
        outputString.addAll(sortedByCount(numbers.sorted().map { it.toString() }.toMutableList(), totalNumbers))
    }
    return outputString
}

private fun convertToIntAndSkipNonInt(str: String): Int? {
    return if (str.toIntOrNull() != null) {
        str.toInt()
    } else {
        println("$str is not a long. It will be skipped")
        null
    }
}

private fun readWords(sortingType: String, inputFile: String): MutableList<String> {
    val words = readSingleInput(inputFile)
    val totalWords = words.size
    val outputString = mutableListOf<String>()
    outputString.add("Total words: $totalWords.")
    if (sortingType == "natural") {
        outputString.add("Sorted data: ${words.sorted().joinToString(" ")}")
    } else {
        outputString.addAll(sortedByCount(words.sorted() as MutableList<String>, totalWords))
    }
    return outputString
}

private fun readLines(sortingType: String, inputFile: String): MutableList<String> {
    val lines = readLineInput(inputFile)
    val totalLines = lines.size
    val outputString = mutableListOf<String>()
    outputString.add("Total words: $totalLines.")
    if (sortingType == "natural") {
        outputString.add("Sorted data:")
        lines.sorted().forEach { outputString.add(it) }
    } else {
        outputString.addAll(sortedByCount(lines.sorted().toMutableList(), totalLines))
    }
    return outputString
}

private fun sortedByCount(keys: MutableList<String>, size: Int): Collection<String> {
    return keys.groupingBy { it }
        .eachCount()
        .toList()
        .sortedBy { (_, value) -> value }
        .map { (k, v) -> "$k: $v time(s), ${v * 100.0 / size}%" }

}

private fun readSingleInput(inputFile: String): MutableList<String> {
    val input = mutableListOf<String>()

    if (inputFile.isNotBlank()) {
        val lines = File(inputFile).readLines()
        lines.forEach { line -> input.addAll(line.split(" ").filter { it.isNotBlank() }) }
    } else {
        val scanner = Scanner(System.`in`)
        while (scanner.hasNext()) {
            input.addAll(scanner.nextLine().split(" ").filter { it.isNotBlank() })
        }
    }
    return input
}

private fun readLineInput(inputFile: String): MutableList<String> {
    if (inputFile.isNotBlank()) {
        return File(inputFile).readLines().toMutableList()
    }

    val input = mutableListOf<String>()
    val scanner = Scanner(System.`in`)
    while (scanner.hasNext()) {
        input.add(scanner.nextLine())
    }
    return input
}
