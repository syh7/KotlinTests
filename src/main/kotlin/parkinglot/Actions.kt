package parkinglot

enum class Actions(val input: String) {
    CREATE("create"),
    LEAVE("leave"),
    PARK("park"),
    REG_BY_COLOR("reg_by_color"),
    SPOT_BY_COLOR("spot_by_color"),
    SPOT_BY_REG("spot_by_reg"),
    STATUS("status"),
    EXIT("exit")
}