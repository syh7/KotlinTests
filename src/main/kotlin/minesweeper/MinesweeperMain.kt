package minesweeper

import kotlin.random.Random

enum class Action {
    CLICK, MARK
}

fun main() {
    val size = 12
    val bombCount = readBombCount()

    val field = createField(size)
    addBombs(field, bombCount)
    addValues(field)

    playGame(field)
}

private fun printField(field: MutableList<MutableList<Square>>) {
    println(" |1 2 3 4 5 6 7 8 9 |")
    println("-|- - - - - - - - - |")
    var rowIndex = 1
    field.forEach { row ->
        print(rowIndex)
        print("|")
        row.forEach { square ->
            run {
                square.print()
                print(" ")
            }
        }
        println("|")
        rowIndex++
    }
    println("-|- - - - - - - - - |")
}

private fun readBombCount(): Int {
    println("How many mines do you want on the field?")
    return readln().toInt()
}

private fun createField(size: Int): MutableList<MutableList<Square>> {
    return MutableList(size) { MutableList(size) { Square() } }
}

private fun addBombs(field: MutableList<MutableList<Square>>, bombCount: Int) {
    val columnCount = field.size
    val rowCount = field[0].size

    var placedBombs = 0
    while (placedBombs != bombCount) {
        val colIndex = Random.nextInt(0, columnCount)
        val rowIndex = Random.nextInt(0, rowCount)
        if (!field[rowIndex][colIndex].bomb) {
            field[rowIndex][colIndex].bomb = true
            placedBombs++
        }
    }
}

private fun addValues(field: MutableList<MutableList<Square>>) {
    for (x in 0 until field.size) {
        for (y in 0 until field.size) {
            if (!field[x][y].bomb) {
                calculateValue(field, x, y)
            }
        }
    }
}

private fun calculateValue(field: MutableList<MutableList<Square>>, rowIndex: Int, columnIndex: Int) {
    var value = 0
    value += containsBomb(field, rowIndex - 1, columnIndex - 1)
    value += containsBomb(field, rowIndex - 1, columnIndex)
    value += containsBomb(field, rowIndex - 1, columnIndex + 1)
    value += containsBomb(field, rowIndex, columnIndex - 1)
    value += containsBomb(field, rowIndex, columnIndex + 1)
    value += containsBomb(field, rowIndex + 1, columnIndex - 1)
    value += containsBomb(field, rowIndex + 1, columnIndex)
    value += containsBomb(field, rowIndex + 1, columnIndex + 1)
    field[rowIndex][columnIndex].value = value
}

private fun containsBomb(field: MutableList<MutableList<Square>>, rowIndex: Int, columnIndex: Int): Int {
    if (coordsInField(field.size, rowIndex, columnIndex) && field[rowIndex][columnIndex].bomb) {
        return 1
    }
    return 0
}

private fun playGame(field: MutableList<MutableList<Square>>) {
    printField(field)
    var finished = false
    while (!finished) {
        val userInput = getUserInput()
        when (userInput.action) {
            Action.MARK -> field[userInput.x][userInput.y].mark()
            Action.CLICK -> recursiveClickSafeNeighbours(field, userInput.x, userInput.y)
        }
        printField(field)
        if (lostGame(field, userInput)) {
            println("You stepped on a mine and failed!")
            finished = true
        } else if (wonGame(field)) {
            println("Congratulations! You found all the mines!")
            finished = true
        }
    }
}

private fun recursiveClickSafeNeighbours(field: MutableList<MutableList<Square>>, x: Int, y: Int) {
    if (coordsInField(field.size, x, y) && !field[x][y].visible && field[x][y].click()) {
        recursiveClickSafeNeighbours(field, x - 1, y - 1)
        recursiveClickSafeNeighbours(field, x - 1, y)
        recursiveClickSafeNeighbours(field, x - 1, y + 1)
        recursiveClickSafeNeighbours(field, x, y - 1)
        recursiveClickSafeNeighbours(field, x, y + 1)
        recursiveClickSafeNeighbours(field, x + 1, y - 1)
        recursiveClickSafeNeighbours(field, x + 1, y)
        recursiveClickSafeNeighbours(field, x + 1, y + 1)
    }
}

private fun coordsInField(size: Int, x: Int, y: Int): Boolean {
    return x >= 0 && y >= 0 && x < size && y < size
}

private fun getUserInput(): UserInput {
    println("Set/unset mine marks or claim a cell as free:")
    val (y, x, action) = readln().split(" ")
    return if (action == "free") {
        UserInput(x.toInt() - 1, y.toInt() - 1, Action.CLICK)
    } else {
        UserInput(x.toInt() - 1, y.toInt() - 1, Action.MARK)
    }
}

private fun lostGame(field: MutableList<MutableList<Square>>, userInput: UserInput): Boolean {
    if (field[userInput.x][userInput.y].bomb && userInput.action != Action.MARK) {
        return true
    }
    return false
}

private fun wonGame(field: MutableList<MutableList<Square>>): Boolean {
    var allBombsMarked = true
    var allSafeFieldsFound = true
    field.forEach { row ->
        row.forEach { square ->
            if (square.bomb && !square.marked) {
                allBombsMarked = false
            } else if (!square.bomb && !square.visible) {
                allSafeFieldsFound = false
            }
        }
    }
    return allBombsMarked || allSafeFieldsFound
}