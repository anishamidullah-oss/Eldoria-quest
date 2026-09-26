package com.example.game.renderer

/**
 * Configuration and mathematical helpers for multi-layer 2D parallax rendering.
 *
 * Defines distinct horizontal and vertical parallax factors across background,
 * midground, and foreground layers:
 * - Background (< 1.0x): Moves slower than camera to convey vast distance.
 * - Midground (~0.68x): Moves behind the active gameplay plane to establish depth.
 * - Gameplay (1.0x): World tiles, entities, and player plane.
 * - Near Foreground (1.25x): Moves faster than camera in front of player.
 * - Extreme Foreground (1.55x): Moves significantly faster than camera, close to lens.
 */
object ParallaxConfig {

    // Horizontal parallax scroll speed factors (relative to camera movement)
    const val FACTOR_FAR_SKY_X = 0.04f
    const val FACTOR_DISTANT_MOUNTAINS_X = 0.16f
    const val FACTOR_MID_BACKGROUND_X = 0.36f
    const val FACTOR_NEAR_MIDGROUND_X = 0.68f
    const val FACTOR_GAMEPLAY_X = 1.00f
    const val FACTOR_NEAR_FOREGROUND_X = 1.25f
    const val FACTOR_EXTREME_FOREGROUND_X = 1.55f

    // Vertical parallax scroll speed factors (relative to camera vertical motion)
    const val FACTOR_FAR_SKY_Y = 0.02f
    const val FACTOR_DISTANT_MOUNTAINS_Y = 0.08f
    const val FACTOR_MID_BACKGROUND_Y = 0.16f
    const val FACTOR_NEAR_MIDGROUND_Y = 0.38f
    const val FACTOR_GAMEPLAY_Y = 1.00f
    const val FACTOR_NEAR_FOREGROUND_Y = 1.18f
    const val FACTOR_EXTREME_FOREGROUND_Y = 1.45f

    /**
     * Calculates the horizontal drawing position in transformed world space
     * for an element with base layer offset [baseX] repeating with [loopWidth].
     *
     * In transformed world space (where canvas has translate(-camX, -camY)):
     * Screen position = Draw position - camX
     * We want Screen position = (baseX - camX * factorX) % loopWidth
     * Therefore: Draw position = camX + Screen position
     */
    fun calculateLayerScrollOffset(camX: Float, factorX: Float, loopWidth: Float): Float {
        if (loopWidth <= 0f) return 0f
        val rawOffset = (camX * factorX) % loopWidth
        return if (rawOffset < 0f) rawOffset + loopWidth else rawOffset
    }

    /**
     * Computes the vertical screen position offset based on camera Y and vertical parallax factor.
     */
    fun calculateVerticalOffset(camY: Float, factorY: Float, baseOffsetY: Float): Float {
        return baseOffsetY - camY * factorY
    }

    /**
     * Computes the screen displacement delta resulting from a camera movement of [deltaCamX].
     * Negative values indicate movement to the left on the screen as the player moves right.
     */
    fun calculateScreenDisplacement(deltaCamX: Float, factorX: Float): Float {
        return -deltaCamX * factorX
    }

    /**
     * Returns true if the layer is in front of the active gameplay plane (foreground),
     * meaning it scrolls faster than the camera.
     */
    fun isForeground(factorX: Float): Boolean {
        return factorX > FACTOR_GAMEPLAY_X
    }

    /**
     * Returns true if the layer is behind the active gameplay plane (background or midground),
     * meaning it scrolls slower than the camera.
     */
    fun isBackgroundOrMidground(factorX: Float): Boolean {
        return factorX < FACTOR_GAMEPLAY_X
    }
}
