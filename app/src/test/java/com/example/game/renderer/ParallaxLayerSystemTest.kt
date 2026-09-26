package com.example.game.renderer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for multi-layer 2D parallax rendering system and speed factor mechanics.
 */
class ParallaxLayerSystemTest {

    @Test
    fun parallaxFactors_hierarchyIsStrictlyOrdered() {
        // Horizontal factors: Background < Midground < Gameplay (1.0x) < Near Foreground < Extreme Foreground
        assertTrue(
            "Far sky factor must be slower than distant mountains",
            ParallaxConfig.FACTOR_FAR_SKY_X < ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_X
        )
        assertTrue(
            "Distant mountains must be slower than mid-background",
            ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_X < ParallaxConfig.FACTOR_MID_BACKGROUND_X
        )
        assertTrue(
            "Mid-background must be slower than near-midground",
            ParallaxConfig.FACTOR_MID_BACKGROUND_X < ParallaxConfig.FACTOR_NEAR_MIDGROUND_X
        )
        assertTrue(
            "Near-midground must be slower than gameplay plane (1.0x)",
            ParallaxConfig.FACTOR_NEAR_MIDGROUND_X < ParallaxConfig.FACTOR_GAMEPLAY_X
        )
        assertTrue(
            "Gameplay plane must be exactly 1.0x",
            ParallaxConfig.FACTOR_GAMEPLAY_X == 1.0f
        )
        assertTrue(
            "Near foreground must scroll faster than gameplay plane (> 1.0x)",
            ParallaxConfig.FACTOR_NEAR_FOREGROUND_X > ParallaxConfig.FACTOR_GAMEPLAY_X
        )
        assertTrue(
            "Extreme foreground must scroll faster than near foreground",
            ParallaxConfig.FACTOR_EXTREME_FOREGROUND_X > ParallaxConfig.FACTOR_NEAR_FOREGROUND_X
        )

        // Vertical factors: Far sky < Mountains < Mid-bg < Near-midground < Gameplay < Foreground
        assertTrue(
            "Vertical far sky factor must be slower than mountains",
            ParallaxConfig.FACTOR_FAR_SKY_Y < ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_Y
        )
        assertTrue(
            "Vertical mountains factor must be slower than mid-background",
            ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_Y < ParallaxConfig.FACTOR_MID_BACKGROUND_Y
        )
        assertTrue(
            "Vertical mid-background factor must be slower than near-midground",
            ParallaxConfig.FACTOR_MID_BACKGROUND_Y < ParallaxConfig.FACTOR_NEAR_MIDGROUND_Y
        )
        assertTrue(
            "Vertical near-midground factor must be slower than gameplay",
            ParallaxConfig.FACTOR_NEAR_MIDGROUND_Y < ParallaxConfig.FACTOR_GAMEPLAY_Y
        )
        assertTrue(
            "Vertical near foreground must be greater than gameplay",
            ParallaxConfig.FACTOR_NEAR_FOREGROUND_Y > ParallaxConfig.FACTOR_GAMEPLAY_Y
        )
        assertTrue(
            "Vertical extreme foreground must be greater than near foreground",
            ParallaxConfig.FACTOR_EXTREME_FOREGROUND_Y > ParallaxConfig.FACTOR_NEAR_FOREGROUND_Y
        )
    }

    @Test
    fun foregroundLayers_moveFasterThanCamera() {
        val deltaCamX = 100f

        val gameplayDisplacement = ParallaxConfig.calculateScreenDisplacement(deltaCamX, ParallaxConfig.FACTOR_GAMEPLAY_X)
        val nearFgDisplacement = ParallaxConfig.calculateScreenDisplacement(deltaCamX, ParallaxConfig.FACTOR_NEAR_FOREGROUND_X)
        val extremeFgDisplacement = ParallaxConfig.calculateScreenDisplacement(deltaCamX, ParallaxConfig.FACTOR_EXTREME_FOREGROUND_X)

        assertEquals(-100f, gameplayDisplacement, 0.001f)
        assertEquals(-125f, nearFgDisplacement, 0.001f)
        assertEquals(-155f, extremeFgDisplacement, 0.001f)

        // Near foreground moves 25% faster than camera
        assertTrue(
            "Near foreground screen displacement magnitude must exceed gameplay",
            kotlin.math.abs(nearFgDisplacement) > kotlin.math.abs(gameplayDisplacement)
        )
        // Extreme foreground moves 55% faster than camera
        assertTrue(
            "Extreme foreground screen displacement magnitude must exceed near foreground",
            kotlin.math.abs(extremeFgDisplacement) > kotlin.math.abs(nearFgDisplacement)
        )

        // Helper classification checks
        assertTrue(ParallaxConfig.isForeground(ParallaxConfig.FACTOR_NEAR_FOREGROUND_X))
        assertTrue(ParallaxConfig.isForeground(ParallaxConfig.FACTOR_EXTREME_FOREGROUND_X))
        assertFalse(ParallaxConfig.isForeground(ParallaxConfig.FACTOR_GAMEPLAY_X))
        assertFalse(ParallaxConfig.isForeground(ParallaxConfig.FACTOR_NEAR_MIDGROUND_X))
    }

    @Test
    fun backgroundAndMidgroundLayers_moveSlowerThanCamera() {
        val deltaCamX = 100f

        val skyDisplacement = ParallaxConfig.calculateScreenDisplacement(deltaCamX, ParallaxConfig.FACTOR_FAR_SKY_X)
        val mountainDisplacement = ParallaxConfig.calculateScreenDisplacement(deltaCamX, ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_X)
        val midBgDisplacement = ParallaxConfig.calculateScreenDisplacement(deltaCamX, ParallaxConfig.FACTOR_MID_BACKGROUND_X)
        val midgroundDisplacement = ParallaxConfig.calculateScreenDisplacement(deltaCamX, ParallaxConfig.FACTOR_NEAR_MIDGROUND_X)

        assertEquals(-4f, skyDisplacement, 0.001f)
        assertEquals(-16f, mountainDisplacement, 0.001f)
        assertEquals(-36f, midBgDisplacement, 0.001f)
        assertEquals(-68f, midgroundDisplacement, 0.001f)

        assertTrue(
            "Midground moves slower than gameplay",
            kotlin.math.abs(midgroundDisplacement) < 100f
        )
        assertTrue(
            "Mid-background moves slower than midground",
            kotlin.math.abs(midBgDisplacement) < kotlin.math.abs(midgroundDisplacement)
        )
        assertTrue(
            "Distant mountains move slower than mid-background",
            kotlin.math.abs(mountainDisplacement) < kotlin.math.abs(midBgDisplacement)
        )
        assertTrue(
            "Far sky moves slower than mountains",
            kotlin.math.abs(skyDisplacement) < kotlin.math.abs(mountainDisplacement)
        )

        assertTrue(ParallaxConfig.isBackgroundOrMidground(ParallaxConfig.FACTOR_FAR_SKY_X))
        assertTrue(ParallaxConfig.isBackgroundOrMidground(ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_X))
        assertTrue(ParallaxConfig.isBackgroundOrMidground(ParallaxConfig.FACTOR_MID_BACKGROUND_X))
        assertTrue(ParallaxConfig.isBackgroundOrMidground(ParallaxConfig.FACTOR_NEAR_MIDGROUND_X))
        assertFalse(ParallaxConfig.isBackgroundOrMidground(ParallaxConfig.FACTOR_GAMEPLAY_X))
        assertFalse(ParallaxConfig.isBackgroundOrMidground(ParallaxConfig.FACTOR_NEAR_FOREGROUND_X))
    }

    @Test
    fun layerScrollOffset_seamlessLoopingAndPositiveBounds() {
        val loopWidth = 800f

        // Test at start
        val offsetAtZero = ParallaxConfig.calculateLayerScrollOffset(0f, 0.36f, loopWidth)
        assertEquals(0f, offsetAtZero, 0.001f)

        // Test along the level
        val testCamPositions = listOf(100f, 500f, 1280f, 2560f, 5120f, 10240f, -50f)
        for (camX in testCamPositions) {
            val offset = ParallaxConfig.calculateLayerScrollOffset(camX, 0.68f, loopWidth)
            assertTrue("Scroll offset must be >= 0 for camX=$camX", offset >= 0f)
            assertTrue("Scroll offset must be < loopWidth for camX=$camX", offset < loopWidth)
        }
    }

    @Test
    fun verticalParallax_attenuatesJumpingArtifacts() {
        val baseOffsetY = 200f
        val camY1 = 100f
        val camY2 = 200f // Player jumped down or fell 100px
        val deltaCamY = camY2 - camY1

        // Distant mountains vertical shift
        val mountainY1 = ParallaxConfig.calculateVerticalOffset(camY1, ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_Y, baseOffsetY)
        val mountainY2 = ParallaxConfig.calculateVerticalOffset(camY2, ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_Y, baseOffsetY)
        val mountainShift = mountainY1 - mountainY2
        assertEquals(deltaCamY * ParallaxConfig.FACTOR_DISTANT_MOUNTAINS_Y, mountainShift, 0.001f)
        assertEquals(8f, mountainShift, 0.001f) // Only shifted 8px despite 100px camera change!

        // Near foreground vertical shift
        val fgY1 = ParallaxConfig.calculateVerticalOffset(camY1, ParallaxConfig.FACTOR_NEAR_FOREGROUND_Y, baseOffsetY)
        val fgY2 = ParallaxConfig.calculateVerticalOffset(camY2, ParallaxConfig.FACTOR_NEAR_FOREGROUND_Y, baseOffsetY)
        val fgShift = fgY1 - fgY2
        assertEquals(deltaCamY * ParallaxConfig.FACTOR_NEAR_FOREGROUND_Y, fgShift, 0.001f)
        assertEquals(118f, fgShift, 0.001f) // Shifts 118px, exceeding the 100px camera movement
    }
}
