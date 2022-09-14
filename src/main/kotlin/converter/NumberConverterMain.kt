package converter

import java.math.BigInteger
import java.util.stream.IntStream
import kotlin.math.pow

fun main() {
    while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        val input = readln()
        if (input == "/exit") {
            break
        }
        val (source, target) = input.split(" ")
        convert(source.toInt(), target.toInt())
    }
}

fun convert(source: Int, target: Int) {
    while (true) {
        println("Enter number in base $source to converter.convert to base $target (To go back type /back)")
        val input = readln()
        if (input == "/back") {
            break
        }
        if (!input.contains(".")) {
            // no fraction so easy
            println("Conversion result: ${BigInteger(input, source).toString(target)}")
        } else {
            // fraction so first to decimal, then from decimal to target
            val (integerPart, fractionalPart) = input.split(".")
            var decimalFraction = IntStream.range(0, fractionalPart.length)
                .mapToDouble { i ->
                    fractionalPart.substring(i, i + 1).toInt(source) / source.toDouble().pow(i + 1.0)
                }
                .sum()

            var total = BigInteger(integerPart, source).toString(target).plus(".")

            for (digit in 0 until 5) {
                decimalFraction *= target
                total += decimalFraction.toInt().toString(target)
                decimalFraction -= decimalFraction.toInt()
            }
            println("Conversion result: $total")
        }
    }
}