package minesweeper

enum class Chars(val char: Char) {
    UNKNOWN('.'), SAFE('/'), MARKED('*'), BOMB('X');

    fun print() {
        print(this.char)
    }
}