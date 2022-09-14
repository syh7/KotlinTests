package minesweeper

class Square {
    var value = 0
    var bomb = false
    var marked = false
    var visible = false

    fun click(): Boolean {
        this.visible = true
        this.marked = false
        return !this.bomb && this.value == 0
    }

    fun mark() {
        this.marked = !marked
    }

    fun print() {
        when {
            marked -> Chars.MARKED.print()
            visible && bomb -> Chars.BOMB.print()
            visible && value > 0 -> print(value)
            visible -> Chars.SAFE.print()
            else -> Chars.UNKNOWN.print()
        }
    }
}