package parkinglot

import java.util.*

class ParkingLot(size: Int) {

    private val lot = arrayOfNulls<Car>(size)

    init {
        println("Created a parking lot with $size spots.")
    }

    fun park(car: Car): Int {
        for (i in lot.indices) {
            if (lot[i] == null) {
                lot[i] = car
                println("${car.colour} car parked in spot ${i + 1}.")
                return i
            }
        }
        println("Sorry, the parking lot is full.")
        return -1
    }

    fun leave(spot: Int): Car? {
        val car = lot[spot]
        if (car == null) {
            println("There is no car in spot ${spot + 1}.")
            return null
        }
        lot[spot] = null
        println("Spot ${spot + 1} is free.")
        return car
    }

    fun status() {
        var found = false
        lot.indices.forEach { index ->
            val car = lot[index]
            if (car != null) {
                println("${index + 1} ${car.numberPlate} ${car.colour}")
                found = true
            }
        }
        if (!found) {
            println("Parking lot is empty.")
        }
    }

    fun platesByColour(colour: String) {
        val stringJoiner = StringJoiner(", ")
        lot.indices.forEach { index ->
            val car = lot[index]
            if (car != null && car.colour.uppercase() == colour) {
                stringJoiner.add(car.numberPlate)
            }
        }
        val length = stringJoiner.length()
        if (length == 0) {
            println("No cars with color $colour were found.")
        } else {
            println(stringJoiner.toString())
        }
    }

    fun spotsByColour(colour: String) {
        val stringJoiner = StringJoiner(", ")
        lot.indices.forEach { index ->
            val car = lot[index]
            if (car != null && car.colour.uppercase() == colour) {
                stringJoiner.add((index + 1).toString())
            }
        }
        val length = stringJoiner.length()
        if (length == 0) {
            println("No cars with color $colour were found.")
        } else {
            println(stringJoiner.toString())
        }
    }

    fun spotByPlate(plate: String) {
        val stringJoiner = StringJoiner(", ")
        lot.indices.forEach { index ->
            val car = lot[index]
            if (car != null && car.numberPlate == plate) {
                stringJoiner.add((index + 1).toString())
            }
        }
        val length = stringJoiner.length()
        if (length == 0) {
            println("No cars with registration number $plate were found.")
        } else {
            println(stringJoiner.toString())
        }
    }
}