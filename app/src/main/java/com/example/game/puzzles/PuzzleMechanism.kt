package com.example.game.puzzles

import com.example.game.model.RectF2D
import com.example.game.model.Vector2D

enum class MechanismType {
    PRESSURE_PLATE,
    LEVER_SWITCH,
    LOCKED_GATE,
    MOVING_PLATFORM,
    RUNIC_TORCH,
    CRUMBLE_WALL
}

data class PressurePlate(
    val id: String,
    val pos: Vector2D,
    val targetMechanismId: String,
    val width: Float = 28f,
    val height: Float = 8f,
    var isPressed: Boolean = false,
    var pressTimer: Float = 0f
) {
    val bounds: RectF2D = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)
    val targetGateId: String get() = targetMechanismId

    fun checkOverlap(other: RectF2D): Boolean {
        return bounds.overlaps(other)
    }
}

data class LeverSwitch(
    val id: String,
    val pos: Vector2D,
    val targetMechanismId: String,
    val width: Float = 24f,
    val height: Float = 24f,
    var isSwitchedOn: Boolean = false,
    var animTimer: Float = 0f
) {
    val bounds: RectF2D = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)
    val targetGateId: String get() = targetMechanismId
    val isOn: Boolean get() = isSwitchedOn

    fun toggle(): Boolean {
        isSwitchedOn = !isSwitchedOn
        animTimer = 0.3f
        return isSwitchedOn
    }
}

data class LockedGate(
    val id: String,
    val pos: Vector2D,
    val width: Float = 20f,
    val height: Float = 64f,
    val requiredKeyId: String? = null,
    val triggerMechanismId: String? = null,
    var isOpen: Boolean = false,
    var openProgress: Float = 0f // 0f = fully closed, 1f = fully open
) {
    val bounds: RectF2D = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)
    val currentX: Float get() = pos.x
    val currentY: Float get() = pos.y - (openProgress * height)

    fun open() {
        isOpen = true
    }

    fun close() {
        isOpen = false
    }

    fun update(dt: Float) {
        if (isOpen && openProgress < 1f) {
            openProgress = (openProgress + dt * 2.5f).coerceAtMost(1f)
        } else if (!isOpen && openProgress > 0f) {
            openProgress = (openProgress - dt * 2.5f).coerceAtLeast(0f)
        }
        // When gate opens, its collision box slides upward
        val currentH = height * (1f - openProgress)
        bounds.set(pos.x, pos.y, pos.x + width, pos.y + currentH)
    }

    val isSolid: Boolean get() = openProgress < 0.85f
}

data class MovingPlatform(
    val id: String,
    val startPos: Vector2D,
    val endPos: Vector2D,
    val width: Float = 64f,
    val height: Float = 14f,
    val speed: Float = 45f,
    var requiresActivation: Boolean = false,
    var isActivated: Boolean = true,
    var progress: Float = 0f,
    var movingForward: Boolean = true
) {
    val pos: Vector2D = Vector2D(startPos.x, startPos.y)
    val vel: Vector2D = Vector2D(0f, 0f)
    val bounds: RectF2D = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)

    val currentX: Float get() = pos.x
    val currentY: Float get() = pos.y

    fun update(dt: Float) {
        if (requiresActivation && !isActivated) {
            vel.set(0f, 0f)
            return
        }

        val totalDist = startPos.distanceTo(endPos)
        if (totalDist < 1f) return

        val step = (speed * dt) / totalDist
        if (movingForward) {
            progress += step
            if (progress >= 1f) {
                progress = 1f
                movingForward = false
            }
        } else {
            progress -= step
            if (progress <= 0f) {
                progress = 0f
                movingForward = true
            }
        }

        val targetX = startPos.x + (endPos.x - startPos.x) * progress
        val targetY = startPos.y + (endPos.y - startPos.y) * progress

        vel.x = (targetX - pos.x) / dt
        vel.y = (targetY - pos.y) / dt

        pos.x = targetX
        pos.y = targetY
        bounds.set(pos.x, pos.y, pos.x + width, pos.y + height)
    }
}

data class RunicTorch(
    val id: String,
    val pos: Vector2D,
    val groupId: String,
    val targetMechanismId: String,
    val width: Float = 22f,
    val height: Float = 34f,
    var isLit: Boolean = false,
    var flameAnimTimer: Float = (Math.random() * 5.0).toFloat()
) {
    val bounds: RectF2D = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)

    fun ignite(): Boolean {
        if (!isLit) {
            isLit = true
            return true
        }
        return false
    }

    fun update(dt: Float) {
        flameAnimTimer += dt
    }
}

data class SecretCrumbleWall(
    val id: String,
    val col: Int,
    val row: Int,
    val tileSize: Float = 32f,
    var hitsRemaining: Int = 2,
    var isBroken: Boolean = false
) {
    val pos = Vector2D(col * tileSize, row * tileSize)
    val bounds = RectF2D(pos.x, pos.y, pos.x + tileSize, pos.y + tileSize)

    fun hit(): Boolean {
        if (isBroken) return false
        hitsRemaining--
        if (hitsRemaining <= 0) {
            isBroken = true
            return true // Wall shattered
        }
        return false
    }
}
