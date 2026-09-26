package com.example.game.engine

import com.example.game.entities.Player
import com.example.game.world.WorldMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CameraFollowSystemTest {

    private lateinit var world: WorldMap
    private lateinit var camera: CameraFollowSystem
    private lateinit var player: Player

    @Before
    fun setUp() {
        world = WorldMap()
        camera = CameraFollowSystem(world, viewportWidth = 800f, viewportHeight = 450f)
        player = Player(startX = 200f, startY = 400f)
    }

    @Test
    fun camera_initializesWithinWorldBounds() {
        assertTrue("Camera position X must be >= 0", camera.position.x >= 0f)
        assertTrue("Camera position Y must be >= 0", camera.position.y >= 0f)
        assertTrue("Camera position X must not exceed maxCamX", camera.position.x <= camera.maxCamX)
        assertTrue("Camera position Y must not exceed maxCamY", camera.position.y <= camera.maxCamY)
    }

    @Test
    fun snapToPlayer_centersOnPlayerAndClampsToBounds() {
        player.pos.x = 1000f
        player.pos.y = 500f
        camera.snapToPlayer(player)

        // Viewport bounds verification
        val maxExpectedX = camera.maxCamX
        val maxExpectedY = camera.maxCamY

        assertTrue("Snapped X must be within bounds", camera.position.x in 0f..maxExpectedX)
        assertTrue("Snapped Y must be within bounds", camera.position.y in 0f..maxExpectedY)
        assertEquals("Render position must match position upon snap", camera.position.x, camera.renderPosition.x, 0.001f)
        assertEquals("Render position must match position upon snap", camera.position.y, camera.renderPosition.y, 0.001f)
    }

    @Test
    fun smoothFollow_interpolatesTowardPlayer() {
        player.pos.x = 500f
        player.pos.y = 400f
        camera.snapToPlayer(player)

        val startX = camera.position.x
        // Move player significantly to the right
        player.pos.x = 1500f
        player.facingRight = true

        // Simulate 1 frame of physics / camera update
        camera.update(player, dt = 0.05f)

        // Camera should have moved to the right towards the player, but not snapped completely in 1 frame
        assertTrue("Camera should move toward the player", camera.position.x > startX)
        assertTrue("Camera should interpolate smoothly, not teleport instantly", camera.position.x < 1500f - camera.viewportWidth * 0.4f)
    }

    @Test
    fun leftAndRightBounds_strictlyMaintained() {
        // Player at extreme left edge
        player.pos.x = 0f
        player.facingRight = false
        camera.snapToPlayer(player)
        for (i in 0..20) {
            camera.update(player, dt = 0.05f)
        }
        assertEquals("Camera at left edge must not be negative", 0f, camera.position.x, 0.001f)
        assertEquals("Render position at left edge must not be negative", 0f, camera.renderPosition.x, 0.001f)

        // Player at extreme right edge
        player.pos.x = world.width - player.width
        player.facingRight = true
        camera.snapToPlayer(player)
        for (i in 0..20) {
            camera.update(player, dt = 0.05f)
        }
        assertEquals("Camera at right edge must clamp to maxCamX", camera.maxCamX, camera.position.x, 0.001f)
        assertEquals("Render position at right edge must clamp to maxCamX", camera.maxCamX, camera.renderPosition.x, 0.001f)
    }

    @Test
    fun topAndBottomBounds_strictlyMaintained() {
        // Player at top edge of the map
        player.pos.y = 0f
        camera.snapToPlayer(player)
        for (i in 0..20) {
            camera.update(player, dt = 0.05f)
        }
        assertEquals("Camera at top edge must not be negative", 0f, camera.position.y, 0.001f)

        // Player at bottom edge of the map
        player.pos.y = world.height - player.height
        camera.snapToPlayer(player)
        for (i in 0..20) {
            camera.update(player, dt = 0.05f)
        }
        assertEquals("Camera at bottom edge must clamp to maxCamY", camera.maxCamY, camera.position.y, 0.001f)
    }

    @Test
    fun screenShake_neverExceedsWorldBounds() {
        // Position camera right at (0, 0)
        camera.position.x = 0f
        camera.position.y = 0f
        camera.triggerShake(intensity = 50f)

        // Run update with shake
        for (i in 0..10) {
            camera.update(player, dt = 0.02f)
            assertTrue("Screen shake must never cause renderPosition.x to become negative", camera.renderPosition.x >= 0f)
            assertTrue("Screen shake must never cause renderPosition.y to become negative", camera.renderPosition.y >= 0f)
            assertTrue("Screen shake must never cause renderPosition.x to exceed maxCamX", camera.renderPosition.x <= camera.maxCamX)
            assertTrue("Screen shake must never cause renderPosition.y to exceed maxCamY", camera.renderPosition.y <= camera.maxCamY)
        }
    }

    @Test
    fun directionalLookAhead_shiftsWithFacingDirection() {
        player.pos.x = 3000f
        player.pos.y = 400f
        player.facingRight = true
        camera.snapToPlayer(player)

        // Update several frames facing right
        for (i in 0..20) {
            camera.update(player, dt = 0.05f)
        }
        val rightLookAhead = camera.currentLookAheadX
        assertTrue("Lookahead should be positive when facing right", rightLookAhead > 0f)

        // Turn player to face left
        player.facingRight = false
        player.vel.x = -100f
        for (i in 0..30) {
            camera.update(player, dt = 0.05f)
        }
        val leftLookAhead = camera.currentLookAheadX
        assertTrue("Lookahead should shift to negative when facing left", leftLookAhead < 0f)
    }

    @Test
    fun viewportResize_reClampsBoundsProperly() {
        player.pos.x = world.width - player.width
        camera.snapToPlayer(player)

        // Viewport widened
        camera.updateViewport(width = 1200f, height = 600f)

        assertTrue("Camera must re-clamp within new maxCamX", camera.position.x <= camera.maxCamX)
        assertTrue("Camera must re-clamp within new maxCamY", camera.position.y <= camera.maxCamY)
    }
}
