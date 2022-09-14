package phonebook

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

private const val DIRECTORY_PATH = "C:\\Users\\1036945\\Downloads\\directory.txt"
private const val NAMES_PATH = "C:\\Users\\1036945\\Downloads\\find.txt"

fun main() {
    val directory = File(DIRECTORY_PATH).readLines().map { it.split(" ", limit = 2) }.toMutableList()
    val names = File(NAMES_PATH).readLines()

    val linearSearch = linearSearch(names, directory)

    bubbleSortAndJumpSearch(names, directory, linearSearch)
    quickSortAndBinarySearch(names, directory, linearSearch)
}


private fun linearSearch(names: List<String>, directory: List<List<String>>): Long {
    println("Start searching (linear search)...")
    var found = 0
    val startTime = System.currentTimeMillis()
    nameLoop@ for (name in names) {
        for (contact in directory) {
            if (contact[1].contains(name)) {
                found++
                continue@nameLoop
            }
        }
    }
    val endTime = System.currentTimeMillis()
    printSearchProperties(found, names.size, endTime - startTime)
    return endTime - startTime
}

private fun bubbleSortAndJumpSearch(names: List<String>, directory: MutableList<List<String>>, linearTime: Long) {
    println("Start searching (bubble sort + jump search)...")
    val sortTime = bubbleSort(directory, linearTime)
    val searchTime = jumpSearch(names, directory, sortTime, linearTime)
    println("Sorting time: ${convertMillisToTimeString(sortTime)}")
    println("Searching time: ${convertMillisToTimeString(searchTime)}")
}

private fun bubbleSort(list: MutableList<List<String>>, linearTime: Long): Long {
    var swapped = true
    val startTime = System.currentTimeMillis()
    sort@ while (swapped) {
        swapped = false
        for (i in 0 until list.size - 1) {
            if (list[i][1] < list[i + 1][1]) {
                val temp = list[i]
                list[i] = list[i + 1]
                list[i + 1] = temp
                swapped = true
            }

            val intermediateTime = System.currentTimeMillis()
            if (intermediateTime - startTime > linearTime) {
                break@sort
            }
        }
    }
    val endTime = System.currentTimeMillis()
    return endTime - startTime
}

private fun jumpSearch(
    names: List<String>,
    directory: MutableList<List<String>>,
    sortTime: Long,
    linearTime: Long
): Long {
    var found = 0
    val startTime = System.currentTimeMillis()
    for (name in names) {
        val index = jumpSearch(directory, name, linearTime)
        if (index != -1) {
            found++
        }
    }
    val endTime = System.currentTimeMillis()
    printSearchProperties(found, names.size, sortTime + endTime - startTime)
    return endTime - startTime
}

private fun jumpSearch(directory: MutableList<List<String>>, name: String, linearTime: Long): Int {
    val startTime = System.currentTimeMillis()
    val jumpSize = sqrt(directory.size.toDouble()).toInt()

    var currentLastIndex = jumpSize - 1

    // find block
    while (currentLastIndex < directory.size && directory[currentLastIndex][1] < name) {
        currentLastIndex += jumpSize
    }
    currentLastIndex = currentLastIndex.coerceAtMost(directory.size - 1)

    val lowerStep = (currentLastIndex - jumpSize).coerceAtLeast(0)
    for (i in currentLastIndex downTo lowerStep) {
        if (directory[i][1] == name) {
            return i
        }
        if (System.currentTimeMillis() - startTime > linearTime) {
            return -1
        }
    }

    return -1
}

private fun quickSortAndBinarySearch(names: List<String>, directory: MutableList<List<String>>, linearTime: Long) {
    println("Start searching (quick sort + binary search)...")
    val startSort = System.currentTimeMillis()
    val sortedDirectory = quickSort(directory, linearTime, startSort)
    val endSort = System.currentTimeMillis()
    val sortTime = endSort - startSort
    val searchTime = binarySearch(names, sortedDirectory, sortTime, linearTime)
    println("Sorting time: ${convertMillisToTimeString(sortTime)}")
    println("Searching time: ${convertMillisToTimeString(searchTime)}")
}

private fun quickSort(directory: List<List<String>>, linearTime: Long, startSort: Long): List<List<String>> {
    val middleSort = System.currentTimeMillis()
    if (middleSort - startSort > linearTime) {
        return directory
    }
    if (directory.size < 2) {
        return directory
    }
    val pivot = directory[directory.size / 2]
    val lesser = directory.filter { it[1] < pivot[1] }
    val equal = directory.filter { it[1].compareTo(pivot[1]) == 0 }
    val greater = directory.filter { it[1] > pivot[1] }

    return quickSort(lesser, linearTime, startSort) + equal + quickSort(greater, linearTime, startSort)
}

private fun binarySearch(names: List<String>, directory: List<List<String>>, sortTime: Long, linearTime: Long): Long {
    val startTime = System.currentTimeMillis()
    var found = 0
    for (name in names) {
        if (binarySearch(name, directory, linearTime, startTime) != -1) {
            found++
        }
    }
    val endTime = System.currentTimeMillis()
    printSearchProperties(found, names.size, sortTime + endTime - startTime)
    return endTime - startTime
}

private fun binarySearch(name: String, directory: List<List<String>>, linearTime: Long, startSort: Long): Int {
    var left = 0
    var right = directory.size - 1
    while (left <= right) {
        if (System.currentTimeMillis() - startSort > linearTime) {
            return -1
        }

        val middle = (left + right) / 2
        if (directory[middle][1] == name) {
            return middle
        }
        if (directory[middle][1] > name) {
            right = middle - 1
        } else {
            left = middle - 1
        }
    }
    return -1
}

private fun printSearchProperties(found: Int, size: Int, time: Long) {
    println("Found $found / $size entries. " + "Time taken ${convertMillisToTimeString(time)}")
}

private fun convertMillisToTimeString(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
    val millisLeft = millis - TimeUnit.SECONDS.toMillis(seconds) - TimeUnit.MINUTES.toMillis(minutes)
    return "$minutes min. $seconds sec. $millisLeft ms."
}
