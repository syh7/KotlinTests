package parkinglot

fun actionFromInput(input: String): Actions {
    for (enum in Actions.values()) {
        if (enum.input == input) {
            return enum
        }
    }
    return Actions.EXIT
}

var parkingLot: ParkingLot? = null

fun main() {
    while (true) {
        val input = readln().split(" ")
        val action = actionFromInput(input[0])

        if (parkingLot == null && !(action == Actions.CREATE || action == Actions.EXIT)) {
            println("Sorry, a parking lot has not been created.")
            continue
        }

        when (action) {
            Actions.CREATE -> create(input)
            Actions.LEAVE -> leave(input, parkingLot!!)
            Actions.PARK -> park(input, parkingLot!!)
            Actions.REG_BY_COLOR -> getPlatesByColour(input, parkingLot!!)
            Actions.SPOT_BY_COLOR -> spotsByColour(input, parkingLot!!)
            Actions.SPOT_BY_REG -> spotByPlate(input, parkingLot!!)
            Actions.STATUS -> status(parkingLot!!)
            Actions.EXIT -> break
        }
    }

}

private fun create(input: List<String>) {
    parkingLot = ParkingLot(input[1].toInt())
}

private fun status(parkingLot: ParkingLot) {
    parkingLot.status()
}

private fun leave(input: List<String>, parkingLot: ParkingLot) {
    val spot = input[1].toInt() - 1
    parkingLot.leave(spot)
}

private fun park(input: List<String>, parkingLot: ParkingLot) {
    val car = Car(input[2], input[1])
    parkingLot.park(car)
}

private fun getPlatesByColour(input: List<String>, parkingLot: ParkingLot) {
    parkingLot.platesByColour(input[1].uppercase())
}

private fun spotsByColour(input: List<String>, parkingLot: ParkingLot) {
    parkingLot.spotsByColour(input[1].uppercase())
}

private fun spotByPlate(input: List<String>, parkingLot: ParkingLot) {
    parkingLot.spotByPlate(input[1])
}