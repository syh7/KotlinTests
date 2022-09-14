package calculator

import java.math.BigInteger

const val EXIT = "/exit"
const val HELP = "/help"

enum class Operand(val sign: String, val priority: Int) {
    PLUS("+", 0), MINUS("-", 0), TIMES("*", 1), DIVIDE("/", 1), LEFT_PARENTHESIS("(", 5), RIGHT_PARENTHESIS(")", 5)
}

class CalculatorException(message: String) : Exception(message)

fun operandFromSign(sign: String): Operand {
    for (enum in Operand.values()) {
        if (enum.sign == sign) {
            return enum
        }
    }
    throw IllegalArgumentException("cannot transform sign $sign to operand")
}

val map = mutableMapOf<String, BigInteger>()

val assignmentRegex = Regex(".+=.+")
val variableRegex = Regex("[a-zA-Z]+")
val operandRegex = Regex("[\\+\\-\\*/\\(\\)]")
val letterRegex = Regex("[a-zA-Z]")
val numbersRegex = Regex("\\d+")

fun main() {
    while (true) {
        val input = readln().replace(" ", "")
        when {
            input == EXIT -> {
                println("Bye!")
                break
            }

            input == HELP -> println("Help I don't get this anymore")
            input.startsWith("/") -> println("Unknown command")
            input.isBlank() -> continue

            assignmentRegex.matches(input) -> tryAssignment(input)
            else -> calculate(input)
        }
    }
}

fun tryAssignment(input: String) {
    val (name, value) = input.split("=")
    if (!variableRegex.matches(name)) {
        println("Invalid identifier")
        return
    }

    if (numbersRegex.matches(value) || map.containsKey(value)) {
        map[name] = getIntFromInputOrMap(value)
    } else {
        println("Invalid assignment")
    }
}

fun calculate(input: String) {
    try {
        val postfix = infixToPostfix(input)
        val result = calculatePostfixString(postfix)
        println(result)
    } catch (e: CalculatorException) {
        // our own exception, print the message
        println(e.message)
    } catch (e: Exception) {
        // catch to prevent breaking problem but just say invalid expressoin
        println("Invalid expression")
    }
}

fun infixToPostfix(input: String): MutableList<String> {
    val postfixString = mutableListOf<String>()
    val operandStack = ArrayDeque<Operand>()
    val split = splitInputForCalculation(input)

    index@ for (i in split.indices) {
        val currentStr = split[i]
        if (operandRegex.matches(currentStr)) {
            val currentOperand = operandFromSign(currentStr)

            // if operand is right parenthesis
            // pop stack until left parenthesis is found
            // do NOT put parentheses in postfix
            if (currentOperand == Operand.RIGHT_PARENTHESIS) {
                while (true) {
                    val previousOperand = operandStack.pop()
                    if (previousOperand == Operand.LEFT_PARENTHESIS) {
                        continue@index
                    } else {
                        postfixString.add(previousOperand.sign)
                    }
                }
            }

            // when we get an operand
            // pop the stack until there is no operand with higher or equal priority
            // then add operand to the stack
            previousOperand@ while (!operandStack.isEmpty()) {
                val previousOperand = operandStack.pop()
                if (previousOperand.priority >= currentOperand.priority && previousOperand != Operand.LEFT_PARENTHESIS) {
                    postfixString.add(previousOperand.sign)
                } else {
                    operandStack.push(previousOperand) // previous operand is lower in priority so put back on stack
                    break@previousOperand // break the while loop to pop operands with lower
                }
            }
            operandStack.push(currentOperand)
        } else {
            // if it is not an operand
            // get value from string (either because it is an int, or because it is a known variable)
            val value = getIntFromInputOrMap(currentStr)
            postfixString.add(value.toString())
        }
    }
    while (!operandStack.isEmpty()) {
        postfixString.add(operandStack.pop().sign)
    }
    println(postfixString)
    return postfixString
}

/**
 * We expect the postfixString to no longer contain variables or multiple operands in a row
 * Those should be transformers in an earlier stadium
 */
fun calculatePostfixString(postfix: MutableList<String>): BigInteger {
    val stack = ArrayDeque<String>()

    for (i in postfix.indices) {
        val currentStr = postfix[i]
        if (numbersRegex.matches(currentStr)) {
            stack.push(currentStr)
        } else {
            val operand = operandFromSign(currentStr)
            val a = BigInteger(stack.pop())
            val b = BigInteger(stack.pop())
            // note calculate BOA not AOB because of stack order
            val temp = when (operand) {
                Operand.PLUS -> b.plus(a)
                Operand.MINUS -> b.minus(a)
                Operand.TIMES -> b.times(a)
                Operand.DIVIDE -> b.divide(a)
                else -> throw CalculatorException("Invalid expression")
            }
            stack.push(temp.toString())
        }
    }
    return BigInteger(stack.pop())
}

private fun getIntFromInputOrMap(input: String): BigInteger {
    return if (numbersRegex.matches(input)) {
        BigInteger(input)
    } else {
        if (map.containsKey(input)) {
            return map[input]!!
        } else {
            throw CalculatorException("Unknown variable")
        }
    }
}

fun splitInputForCalculation(input: String): List<String> {
    val tempList = mutableListOf<String>()
    val split: List<String> = input.split("").filter { it.isNotBlank() }

    var fullStr = ""
    var lastRegex = letterRegex
    for (i in split.indices) {
        val currentChar = split[i]
        if (lastRegex.matches(currentChar)) {
            if (lastRegex == operandRegex) {
                // in general, we add them singularly to the tempList to be returned
                // in some cases we want to combine them, notable when we have multiple pluses or minuses in a row
                // multiple minuses should be combined following the math rules
                if (currentChar == "-" && (fullStr == "-" || fullStr == "+")) {
                    fullStr = if (fullStr == "-") "+" else "-"
                } else if (currentChar == "+" && fullStr == "+") {
                    // do nothing
                } else {
                    // only + and - can be concatenated, rest should be separate operands and thus separate items in the list
                    tempList.add(fullStr)
                    fullStr = currentChar
                }
            } else {
                // not operands (e.g. numbers and chars) should just be combined with each other
                fullStr += currentChar
            }
        } else {
            tempList.add(fullStr)
            fullStr = currentChar
            lastRegex = when {
                operandRegex.matches(currentChar) -> operandRegex
                letterRegex.matches(currentChar) -> letterRegex
                numbersRegex.matches(currentChar) -> numbersRegex
                else -> throw IllegalArgumentException("Cannot match regex")
            }
        }
        if (i == split.size - 1) {
            tempList.add(fullStr)
        }
    }

    // last filter necessary for trailing white space if first character is not a letter
    return tempList.filter { it.isNotBlank() }
}

fun <T> ArrayDeque<T>.push(element: T) = addLast(element)
fun <T> ArrayDeque<T>.pop() = removeLastOrNull()!!
