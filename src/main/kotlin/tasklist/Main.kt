package tasklist

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import java.io.File

class LocalDateTimeAdapter {
    @ToJson
    fun toJson(dateTime: LocalDateTime): String {
        return dateTime.toString()
    }

    @FromJson
    fun fromJson(value: String): LocalDateTime {
        val (date, time) = value.split("T")
        val (year, month, day) = date.split("-")
        val (hour, minute) = time.split(":")
        return LocalDateTime(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt())
    }
}

enum class Priority { C, H, N, L }
enum class DueTag { T, I, O }

private fun generateDueTag(dateTime: LocalDateTime): DueTag {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
    val numberOfDays = currentDate.daysUntil(dateTime.date)
    return when {
        numberOfDays == 0 -> DueTag.T
        numberOfDays > 0 -> DueTag.I
        else -> DueTag.O
    }
}

class Task(var priority: Priority, var dateTime: LocalDateTime, val list: MutableList<String>) {
    fun add(s: String) {
        list.add(s)
    }

    fun replaceDate(newDate: LocalDate) {
        dateTime = LocalDateTime(newDate.year, newDate.month, newDate.dayOfMonth, dateTime.hour, dateTime.minute)
    }

    fun replaceTime(newTime: LocalDateTime) {
        dateTime = LocalDateTime(dateTime.year, dateTime.month, dateTime.dayOfMonth, newTime.hour, newTime.minute)
    }
}

fun main() {
    val moshi = Moshi.Builder()
        .add(LocalDateTimeAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
    val taskListType = Types.newParameterizedType(MutableList::class.java, Task::class.java)
    val adapter = moshi.adapter<MutableList<Task>>(taskListType)

    val jsonFile = File("tasklist.json").also { it.createNewFile() }
    val taskList = if (jsonFile.readText().isNotEmpty()) {
        adapter.fromJson(jsonFile.readText())!!
    } else {
        mutableListOf()
    }

    actionLoop(taskList)
    jsonFile.writeText(adapter.toJson(taskList))
}

private fun actionLoop(taskList: MutableList<Task>) {
    while (true) {
        println("Input an action (add, print, edit, delete, end):")
        val input = readln()
        when (input.lowercase()) {
            "add" -> addTaskToList(taskList)
            "print" -> printList(taskList)
            "edit" -> editList(taskList)
            "delete" -> deleteTasks(taskList)
            "end" -> {
                println("Tasklist exiting!")
                break
            }

            else -> println("The input action is invalid")
        }
    }
}

private fun editList(taskList: MutableList<Task>) {
    printList(taskList)
    if (taskList.isEmpty()) {
        return
    }
    val task: Task = getTaskWithUserInput(taskList)

    while (true) {
        try {
            println("Input a field to edit (priority, date, time, task):")
            when (readln()) {
                "priority" -> task.priority = readPriority()
                "date" -> task.replaceDate(readDate())
                "time" -> task.replaceTime(readTime())
                "task" -> {
                    task.list.clear()
                    addNewLinesToTask(task)
                }

                else -> throw IllegalArgumentException()
            }
            break
        } catch (e: Exception) {
            println("Invalid field")
        }
    }
    println("The task is changed")
}

private fun getTaskWithUserInput(taskList: MutableList<Task>): Task {
    while (true) {
        try {
            println("Input the task number (1-${taskList.size}):")
            val input = readln().toInt() - 1
            if (input < 0) {
                throw IllegalArgumentException()
            }
            return taskList[input]
        } catch (e: Exception) {
            println("Invalid task number")
        }
    }
}

private fun deleteTasks(taskList: MutableList<Task>) {
    printList(taskList)
    if (taskList.isEmpty()) {
        return
    }
    val task = getTaskWithUserInput(taskList)
    taskList.remove(task)
    println("The task is deleted")
}

private fun addTaskToList(taskList: MutableList<Task>) {
    val priority = readPriority()
    val date = readDate()
    val time = readTime()

    val task =
        Task(priority, LocalDateTime(date.year, date.month, date.dayOfMonth, time.hour, time.minute), mutableListOf())

    addNewLinesToTask(task)

    if (task.list.isEmpty()) {
        println("The task is blank")
    } else {
        taskList.add(task)
    }
}

private fun addNewLinesToTask(task: Task) {
    println("Input a new task (enter a blank line to end):")
    while (true) {
        val input = readln().trimIndent()
        if (input.isEmpty()) {
            break
        } else {
            task.add(input)
        }
    }
}

private fun readPriority(): Priority {
    while (true) {
        try {
            println("Input the task priority (C, H, N, L):")
            return Priority.valueOf(readln().uppercase())
        } catch (e: IllegalArgumentException) {
            // just try again without saying anything because jetbrains says so
        }
    }
}

private fun readDate(): LocalDate {
    while (true) try {
        println("Input the date (yyyy-mm-dd):")
        val (y, m, d) = readln().split("-").map { it.toInt() }
        return LocalDate(y, m, d)
    } catch (e: Exception) {
        println("The input date is invalid")
    }
}

private fun readTime(): LocalDateTime {
    while (true) try {
        println("Input the time (hh:mm):")
        val (h, m) = readln().split(":").map { it.toInt() }
        return LocalDateTime(2000, 1, 1, h, m)
    } catch (e: Exception) {
        println("The input time is invalid")
    }
}

private fun printList(taskList: MutableList<Task>) {
    if (taskList.isEmpty()) {
        println("No tasks have been input")
        return
    }

    val emptyInfo = "|    |            |       |   |   |"
    val separatorLine = "+----+------------+-------+---+---+--------------------------------------------+"
    val header = "| N  |    Date    | Time  | P | D |                   Task                     |"

    println(separatorLine)
    println(header)
    println(separatorLine)

    taskList.forEachIndexed { index, task ->
        val taskInfo = createLineWithTaskInfo(index, task)

        task.list.forEachIndexed { subIndex, subTask ->
            val chunked = subTask.chunked(44)
            var chunkStart = 0
            if (subIndex == 0) {
                println("$taskInfo${chunked[0].padEnd(44, ' ')}|")
                chunkStart = 1
            }
            for (i in chunkStart until chunked.size) {
                println("$emptyInfo${chunked[i].padEnd(44, ' ')}|")
            }
        }
        println(separatorLine)
    }
}

private fun createLineWithTaskInfo(index: Int, task: Task): String {
    val indexStr = (index + 1).toString().padEnd(2, ' ')
    val (date, time) = task.dateTime.toString().split("T")
    val priorityColour = convertPriorityToColour(task.priority)
    val dueTagColour = convertDueTagToColour(generateDueTag(task.dateTime))
    return "| $indexStr | $date | $time | $priorityColour | $dueTagColour |"
}

fun convertPriorityToColour(priority: Priority): String {
    return when (priority) {
        Priority.C -> "\u001B[101m \u001B[0m"
        Priority.H -> "\u001B[103m \u001B[0m"
        Priority.N -> "\u001B[102m \u001B[0m"
        Priority.L -> "\u001B[104m \u001B[0m"
    }
}

fun convertDueTagToColour(dueTag: DueTag): String {
    return when (dueTag) {
        DueTag.O -> "\u001B[101m \u001B[0m"
        DueTag.T -> "\u001B[103m \u001B[0m"
        DueTag.I -> "\u001B[102m \u001B[0m"
    }
}
