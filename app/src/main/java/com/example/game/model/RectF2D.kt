package com.example.game.model

data class RectF2D(
    var left: Float = 0f,
    var top: Float = 0f,
    var right: Float = 0f,
    var bottom: Float = 0f
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun set(l: Float, t: Float, r: Float, b: Float): RectF2D {
        left = l
        top = t
        right = r
        bottom = b
        return this
    }

    fun set(other: RectF2D): RectF2D {
        left = other.left
        top = other.top
        right = other.right
        bottom = other.bottom
        return this
    }

    fun offset(dx: Float, dy: Float): RectF2D {
        left += dx
        top += dy
        right += dx
        bottom += dy
        return this
    }

    fun overlaps(other: RectF2D): Boolean {
        return left < other.right && right > other.left &&
                top < other.bottom && bottom > other.top
    }

    fun contains(px: Float, py: Float): Boolean {
        return px in left..right && py in top..bottom
    }

    fun copyRect(): RectF2D = RectF2D(left, top, right, bottom)
}
