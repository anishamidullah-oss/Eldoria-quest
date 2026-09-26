package com.example.game.engine

import com.example.game.entities.Player
import com.example.game.model.RectF2D
import com.example.game.model.Vector2D
import com.example.game.world.WorldMap
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Smooth 2D camera follow system that tracks the player's position,
 * incorporates directional look-ahead and vertical soft deadzone,
 * and maintains strict bounding within the game level boundaries.
 */
class CameraFollowSystem(
    var world: WorldMap,
    var viewportWidth: Float = 800f,
    var viewportHeight: Float = 450f
) {
    // Current camera position (top-left of the viewport in world coordinates)
    val position = Vector2D(0f, 0f)

    // Render position after applying screen shake, strictly clamped within level boundaries
    val renderPosition = Vector2D(0f, 0f)

    // Look-ahead parameters
    var lookAheadDistance: Float = 64f
    var currentLookAheadX: Float = 0f
    var lookAheadSpeed: Float = 4.0f

    // Smoothing parameters
    var horizontalSmoothSpeed: Float = 7.5f
    var verticalSmoothSpeed: Float = 5.0f

    // Framing offsets (where the player character is targeted within the viewport: 0.5 = center)
    var targetScreenFractionX: Float = 0.46f // Slightly left of center when facing right for look-ahead
    var targetScreenFractionY: Float = 0.58f // Slightly below center so upper platforms are visible

    // Vertical soft deadzone to prevent camera jitter on small steps / micro hops
    var verticalDeadzoneHalfHeight: Float = 18f
    var lastTargetY: Float = 0f

    // Screen shake
    var shakeIntensity: Float = 0f
    var shakeDecay: Float = 22f

    // Bounds limits
    val minCamX: Float get() = 0f
    val maxCamX: Float get() = (world.width - viewportWidth).coerceAtLeast(0f)
    val minCamY: Float get() = 0f
    val maxCamY: Float get() = (world.height - viewportHeight).coerceAtLeast(0f)

    init {
        snapTo(0f, 0f)
    }

    /**
     * Updates viewport dimensions reported by the rendering surface.
     */
    fun updateViewport(width: Float, height: Float) {
        if (width > 0f && height > 0f) {
            viewportWidth = width
            viewportHeight = height
            // Re-clamp positions to ensure new dimensions do not violate bounds
            position.x = clampX(position.x)
            position.y = clampY(position.y)
            renderPosition.x = clampX(renderPosition.x)
            renderPosition.y = clampY(renderPosition.y)
        }
    }

    /**
     * Instantly snaps the camera to target the given player and clamps to level bounds.
     */
    fun snapToPlayer(player: Player) {
        val playerCenterX = player.pos.x + player.width * 0.5f
        val playerCenterY = player.pos.y + player.height * 0.5f

        currentLookAheadX = if (player.facingRight) lookAheadDistance else -lookAheadDistance
        val targetX = (playerCenterX + currentLookAheadX) - (viewportWidth * targetScreenFractionX)
        val targetY = playerCenterY - (viewportHeight * targetScreenFractionY)

        position.x = clampX(targetX)
        position.y = clampY(targetY)
        lastTargetY = targetY
        renderPosition.set(position.x, position.y)
    }

    /**
     * Instantly snaps to an explicit target coordinate.
     */
    fun snapTo(worldX: Float, worldY: Float) {
        val targetX = worldX - (viewportWidth * targetScreenFractionX)
        val targetY = worldY - (viewportHeight * targetScreenFractionY)
        position.x = clampX(targetX)
        position.y = clampY(targetY)
        lastTargetY = targetY
        renderPosition.set(position.x, position.y)
    }

    /**
     * Updates camera tracking with smooth interpolation, look-ahead, and level bounding.
     */
    fun update(player: Player, dt: Float) {
        val clampedDt = dt.coerceIn(0.001f, 0.1f)

        // 1. Directional look-ahead based on facing direction and horizontal velocity
        val targetLookAhead = if (abs(player.vel.x) > 20f) {
            if (player.vel.x > 0f) lookAheadDistance else -lookAheadDistance
        } else {
            if (player.facingRight) lookAheadDistance * 0.6f else -lookAheadDistance * 0.6f
        }
        val lookAheadFactor = 1f - exp(-lookAheadSpeed * clampedDt)
        currentLookAheadX += (targetLookAhead - currentLookAheadX) * lookAheadFactor

        // 2. Compute Target Focus Point
        val playerCenterX = player.pos.x + player.width * 0.5f
        val playerCenterY = player.pos.y + player.height * 0.5f

        // 3. Compute Ideal Camera Coordinates
        val idealTargetX = (playerCenterX + currentLookAheadX) - (viewportWidth * targetScreenFractionX)
        val rawTargetY = playerCenterY - (viewportHeight * targetScreenFractionY)

        // Vertical soft deadzone: small vertical changes (e.g. minor steps) are smoothed
        val targetY = if (player.isGrounded) {
            lastTargetY = rawTargetY
            rawTargetY
        } else {
            val deltaY = rawTargetY - lastTargetY
            if (abs(deltaY) < verticalDeadzoneHalfHeight) {
                lastTargetY
            } else {
                if (deltaY > 0f) {
                    rawTargetY - verticalDeadzoneHalfHeight
                } else {
                    rawTargetY + verticalDeadzoneHalfHeight
                }
            }
        }

        // 4. Clamp Target to Level Bounds
        val boundedTargetX = clampX(idealTargetX)
        val boundedTargetY = clampY(targetY)

        // 5. Adaptive Smoothing (Catches up faster when player is further away from focal area)
        val distX = abs(boundedTargetX - position.x)
        val distY = abs(boundedTargetY - position.y)

        val adaptiveH = if (distX > viewportWidth * 0.25f) horizontalSmoothSpeed * 1.6f else horizontalSmoothSpeed
        val adaptiveV = if (distY > viewportHeight * 0.25f) verticalSmoothSpeed * 1.8f else verticalSmoothSpeed

        val lerpFactorX = 1f - exp(-adaptiveH * clampedDt)
        val lerpFactorY = 1f - exp(-adaptiveV * clampedDt)

        position.x += (boundedTargetX - position.x) * lerpFactorX
        position.y += (boundedTargetY - position.y) * lerpFactorY

        // Strict clamp against level bounds
        position.x = clampX(position.x)
        position.y = clampY(position.y)

        // 6. Apply screen shake and compute clamped render position
        if (shakeIntensity > 0f) {
            shakeIntensity = (shakeIntensity - shakeDecay * clampedDt).coerceAtLeast(0f)
            val shakeOffsetX = (Random.nextFloat() * 2f - 1f) * shakeIntensity
            val shakeOffsetY = (Random.nextFloat() * 2f - 1f) * shakeIntensity
            // Ensure shake never pulls camera outside level boundaries
            renderPosition.x = clampX(position.x + shakeOffsetX)
            renderPosition.y = clampY(position.y + shakeOffsetY)
        } else {
            renderPosition.x = position.x
            renderPosition.y = position.y
        }
    }

    fun triggerShake(intensity: Float) {
        shakeIntensity = max(shakeIntensity, intensity)
    }

    /**
     * Clamps X coordinate to level boundaries.
     * If the level is narrower than the viewport, centers horizontally.
     */
    fun clampX(x: Float): Float {
        return if (world.width <= viewportWidth) {
            (world.width - viewportWidth) * 0.5f
        } else {
            x.coerceIn(minCamX, maxCamX)
        }
    }

    /**
     * Clamps Y coordinate to level boundaries.
     * If the level is shorter than the viewport, centers vertically.
     */
    fun clampY(y: Float): Float {
        return if (world.height <= viewportHeight) {
            (world.height - viewportHeight) * 0.5f
        } else {
            y.coerceIn(minCamY, maxCamY)
        }
    }

    /**
     * Returns the bounding rectangle of the current camera viewport in world space.
     */
    fun getViewportBounds(): RectF2D {
        return RectF2D(
            left = renderPosition.x,
            top = renderPosition.y,
            right = renderPosition.x + viewportWidth,
            bottom = renderPosition.y + viewportHeight
        )
    }

    /**
     * Checks if a world object's bounding box is visible within the current camera viewport,
     * with an optional margin for smooth culling.
     */
    fun isVisible(bounds: RectF2D, margin: Float = 32f): Boolean {
        return bounds.right >= renderPosition.x - margin &&
                bounds.left <= renderPosition.x + viewportWidth + margin &&
                bounds.bottom >= renderPosition.y - margin &&
                bounds.top <= renderPosition.y + viewportHeight + margin
    }
}
