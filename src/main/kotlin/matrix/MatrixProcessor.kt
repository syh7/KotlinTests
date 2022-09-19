package matrix

import java.util.stream.IntStream
import kotlin.math.pow

typealias Matrix = MutableList<MutableList<Double>>

class MatrixException : Exception()

fun main() {
    while (true) {
        try {
            when (getUserChoice()) {
                1 -> performSum()
                2 -> performConstantMultiplication()
                3 -> performMatrixMultiplication()
                4 -> performTransposition()
                5 -> performDeterminant()
                6 -> performInverse()
                0 -> break
            }
        } catch (e: MatrixException) {
            println("The operation cannot be performed.")
        }
    }
}

private fun getUserChoice(): Int {
    println("1. Add matrices")
    println("2. Multiply matrix by a constant")
    println("3. Multiply matrices")
    println("4. Transpose matrix")
    println("5. Calculate a determinant")
    println("6. Inverse matrix")
    println("0. Exit")
    print("Your choice: ")
    val choice = readln().toInt()
    println()
    return choice
}

private fun readMatrix(str: String = ""): Matrix {
    val matrix: Matrix = mutableListOf()
    println("Enter size of ${str}matrix:")
    val (rowCount, columnCount) = readln().split(" ").map { it.toInt() }
    println("Enter matrix:")
    for (i in 0 until rowCount) {
        val row = readln().split(" ").map { it.toDouble() }.toMutableList()
        if (row.size != columnCount) {
            throw MatrixException()
        }
        matrix.add(row)
    }
    return matrix
}

private fun printMatrix(matrix: Matrix) {
    println("The result is:")
    matrix.forEach { row -> println(row.joinToString(" ") { String.format("%.2f", it) }) }
}

private fun performSum() {
    val a = readMatrix("first ")
    val b = readMatrix("second ")
    if (a.size != b.size || a[0].size != b[0].size) {
        throw MatrixException()
    } else {
        printMatrix(sum(a, b))
    }
}

private fun performConstantMultiplication() {
    val a = readMatrix()
    print("Enter constant: ")
    val b = readln().toDouble()
    println()
    printMatrix(multiply(a, b))
}

private fun performMatrixMultiplication() {
    val a = readMatrix("first ")
    val b = readMatrix("second ")
    printMatrix(multiply(a, b))
}

private fun performTransposition() {
    println("1. Main diagonal")
    println("2. Side diagonal")
    println("3. Vertical line")
    println("4. Horizontal line")
    print("Your choice: ")
    val choice = readln().toInt()
    println()
    val matrix = readMatrix()
    when (choice) {
        1 -> printMatrix(transposeMain(matrix))
        2 -> printMatrix(transposeSide(matrix))
        3 -> printMatrix(transposeVertical(matrix))
        4 -> printMatrix(transposeHorizontal(matrix))
    }
}

private fun performDeterminant() {
    val matrix = readMatrix()
    println("The result is:")
    println(determinant(matrix))
}

private fun performInverse() {
    val matrix = readMatrix()
    val determinant = determinant(matrix)
    if (determinant == 0.0) {
        println("This matrix doesn't have an inverse.")
        return
    }
    val adjacencyMatrix = createAdjacencyMatrix(matrix)
    val factor = 1 / determinant
    val inverse = multiply(adjacencyMatrix, factor)
    printMatrix(inverse)
}

private fun createAdjacencyMatrix(a: Matrix): Matrix {
    val result = emptyMatrix(a.size, a[0].size)
    for (i in a.indices) {
        for (j in a[0].indices) {
            val subMatrix = getSubMatrix(a, i, j)
            val subDeterminant = determinant(subMatrix)
            val factor = calculateFactor(i, j)
            result[i][j] = factor * subDeterminant
        }
    }
    return transposeMain(result)
}

private fun calculateFactor(i: Int, j: Int): Int {
    return (-1.0).pow((i + j).toDouble()).toInt()
}

private fun determinant(a: Matrix): Double {
    if (a.size == 2) {
        return a[0][0] * a[1][1] - a[0][1] * a[1][0]
    }
    var determinant = 0.0
    for (i in a[0].indices) {
        val subMatrix = getSubMatrix(a, 0, i)
        val subDeterminant = a[0][i] * determinant(subMatrix)
        if (i % 2 == 0) {
            determinant += subDeterminant
        } else {
            determinant -= subDeterminant
        }
    }
    return determinant
}

private fun getSubMatrix(a: Matrix, skipRowIndex: Int, skipColumnIndex: Int): Matrix {
    val result = emptyMatrix(a.size - 1, a[0].size - 1)
    for (i in a.indices) {
        if (i == skipRowIndex) {
            continue
        }
        if (i < skipRowIndex) {
            a[i].filterIndexed { index, _ -> index != skipColumnIndex }
                .forEachIndexed { j, value -> result[i][j] = value }
        } else {
            a[i].filterIndexed { index, _ -> index != skipColumnIndex }
                .forEachIndexed { j, value -> result[i - 1][j] = value }
        }
    }
    return result
}

private fun transposeMain(a: Matrix): Matrix {
    val result = emptyMatrix(a.size, a[0].size)
    for (i in a.indices) {
        for (j in a[0].indices) {
            result[j][i] = a[i][j]
        }
    }
    return result
}

private fun transposeSide(a: Matrix): Matrix {
    val rows = a.size
    val columns = a[0].size
    val result = emptyMatrix(rows, columns)
    for (i in a.indices) {
        for (j in a[0].indices) {
            result[rows - 1 - j][columns - 1 - i] = a[i][j]
        }
    }
    return result
}

private fun transposeVertical(a: Matrix): Matrix {
    val columns = a[0].size
    val result = emptyMatrix(a.size, columns)
    for (i in a.indices) {
        for (j in a[0].indices) {
            result[i][columns - 1 - j] = a[i][j]
        }
    }
    return result
}

private fun transposeHorizontal(a: Matrix): Matrix {
    val rows = a.size
    val result = emptyMatrix(rows, a[0].size)
    for (i in a.indices) {
        for (j in a[0].indices) {
            result[rows - 1 - i][j] = a[i][j]
        }
    }
    return result
}

private fun sum(a: Matrix, b: Matrix): Matrix {
    val result = emptyMatrix(a.size, a[0].size)
    for (i in 0 until a.size) {
        for (j in 0 until a[0].size) {
            result[i][j] = a[i][j] + b[i][j]
        }
    }
    return result
}

private fun multiply(a: Matrix, b: Matrix): Matrix {
    val result = emptyMatrix(a.size, b[0].size)
    for (i in 0 until a.size) {
        for (j in b[0].indices) {
            val column = IntStream.range(0, b.size).mapToDouble { b[it][j] }.toArray().asList()
            result[i][j] = getDotProduct(a[i], column)
        }
    }
    return result
}

private fun getDotProduct(a: List<Double>, b: List<Double>): Double {
    var result = 0.0
    for (i in a.indices) {
        result += a[i] * b[i]
    }
    return result
}

private fun multiply(matrix: Matrix, constant: Double): Matrix {
    val result = emptyMatrix(matrix.size, matrix[0].size)
    for (i in 0 until matrix.size) {
        for (j in 0 until matrix[0].size) {
            result[i][j] = matrix[i][j] * constant
        }
    }
    return result
}

private fun emptyMatrix(rows: Int, columns: Int) = MutableList(rows) { MutableList(columns) { 0.0 } }
