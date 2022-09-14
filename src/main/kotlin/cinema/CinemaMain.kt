package cinema

class Seat(var char: Char, var cost: Int)

fun main() {
    println("Enter the number of rows:")
    val totalRows = readln().toInt()
    println("Enter the number of seats in each row:")
    val totalColumns = readln().toInt()

    val room = createRoom(totalRows, totalColumns)
    printRoom(room)
    addCost(room)

    while (true) {
        println("1. Show the seats")
        println("2. Buy a ticket")
        println("3. Statistics")
        println("0. Exit")
        when (readln()) {
            "1" -> printRoom(room)
            "2" -> buyTicket(room)
            "3" -> statistics(room)
            "0" -> break
        }
    }
}

private fun statistics(room: MutableList<MutableList<Seat>>) {
    var boughtSeats = 0
    var currentIncome = 0
    var totalIncome = 0
    room.forEach { row ->
        row.forEach {
            if (it.char == 'B') {
                boughtSeats++
                currentIncome += it.cost
            }
            totalIncome += it.cost
        }
    }
    val totalSeats = room.size * room.first().size
    val percentage = boughtSeats.toFloat() / totalSeats.toFloat() * 100

    println("Number of purchased tickets: $boughtSeats")
    println("Percentage: ${"%.2f".format(percentage)}%")
    println("Current income: $$currentIncome")
    println("Total income: $$totalIncome")
}

private fun buyTicket(room: MutableList<MutableList<Seat>>) {
    println("Enter a row number:")
    val row = readln().toInt() - 1
    println("Enter a seat number in that row:")
    val column = readln().toInt() - 1

    if (row >= room.size || column >= room.first().size || row < 0 || column < 0) {
        println("Wrong input!")
        buyTicket(room)
    } else if (room[row][column].char == 'B') {
        println("That ticket has already been purchased")
        buyTicket(room)
    } else {
        room[row][column].char = 'B'
        println("Ticket price: $${room[row][column].cost}")
    }
}

private fun addCost(room: MutableList<MutableList<Seat>>) {
    val rows = room.size
    val columns = room.first().size
    val totalSeats = rows * columns
    if (totalSeats <= 60) {
        room.forEach { row -> row.forEach { it.cost = 10 } }
    } else {
        val firstHalf = rows / 2
        room.forEachIndexed { index, row ->
            val cost = if ((0 until firstHalf).contains(index)) 10 else 8
            row.forEach { it.cost = cost }
        }
    }
}

private fun printRoom(room: MutableList<MutableList<Seat>>) {
    println("Cinema:")
    println("  ${(1..room.first().size).joinToString(" ")}")
    room.forEachIndexed { index, row ->
        println(row.joinToString(prefix = "${index + 1} ", separator = " ") { it.char.toString() })
    }
}

private fun createRoom(rows: Int, columns: Int): MutableList<MutableList<Seat>> {
    return MutableList(rows) { MutableList(columns) { Seat('S', 10) } }
}