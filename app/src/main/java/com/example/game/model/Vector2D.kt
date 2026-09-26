package com.example.game.model

import kotlin.math.sqrt

data class Vector2D(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun set(newX: Float, newY: Float): Vector2D {
        x = newX
        y = newY
        return this
    }

    fun set(other: Vector2D): Vector2D {
        x = other.x
        y = other.y
        return this
    }

    fun add(dx: Float, dy: Float): Vector2D {
        x += dx
        y += dy
        return this
    }

    fun add(other: Vector2D): Vector2D {
        x += other.x
        y += other.y
        return this
    }

    fun scale(factor: Float): Vector2D {
        x *= factor
        y *= factor
        return this
    }

    fun length(): Float = sqrt(x * x + y * y)

    fun lengthSquared(): Float = x * x + y * y

    fun distanceTo(other: Vector2D): Float {
        val dx = x - other.x
        val dy = y - other.y
        return sqrt(dx * dx + dy * dy)
    }

    fun distanceTo(ox: Float, oy: Float): Float {
        val dx = x - ox
        val dy = y - oy
        return sqrt(dx * dx + dy * dy)
    }

    fun copyVector(): Vector2D = Vector2D(x, y)
}
