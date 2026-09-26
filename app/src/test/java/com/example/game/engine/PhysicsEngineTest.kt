package com.example.game.engine

import com.example.game.entities.Player
import com.example.game.world.TileType
import com.example.game.world.WorldMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PhysicsEngineTest {

    private lateinit var world: WorldMap
    private lateinit var physics: PhysicsEngine
    private lateinit var player: Player

    @Before
    fun setUp() {
        world = WorldMap()
        physics = PhysicsEngine(world)
        // Set player in an open space: row 5, col 5
        val ts = world.tileSize
        player = Player(startX = 5 * ts, startY = 5 * ts)
    }

    @Test
    fun horizontalMovement_acceleratesTowardMaxSpeed() {
        player.isGrounded = true
        val initialVelX = player.vel.x

        // Simulate moving right
        physics.updatePlayer(player, inputX = 1f, inputY = 0f, dt = 0.1f, onHitHazard = {})

        assertTrue("Player should have positive horizontal velocity", player.vel.x > initialVelX)
        assertTrue("Player facing direction should be right", player.facingRight)
        assertTrue("Velocity should not exceed effectiveMoveSpeed", player.vel.x <= player.effectiveMoveSpeed)
    }

    @Test
    fun gravity_acceleratesPlayerDownwardAndClampsToTerminalVelocity() {
        player.isGrounded = false
        player.vel.y = 0f

        // Apply several physics frames in midair with no ground underneath
        // Set tiles under player to AIR to test pure freefall
        val startRow = (player.pos.y / world.tileSize).toInt()
        for (r in startRow..world.rows - 1) {
            world.setTile(r, 5, TileType.AIR)
            world.setTile(r, 6, TileType.AIR)
        }

        physics.updatePlayer(player, inputX = 0f, inputY = 0f, dt = 0.1f, onHitHazard = {})
        assertTrue("Gravity should increase downward velocity", player.vel.y > 0f)

        // Simulate falling for a long duration to test terminal velocity clamp
        for (i in 0..50) {
            physics.updatePlayer(player, inputX = 0f, inputY = 0f, dt = 0.1f, onHitHazard = {})
        }

        assertEquals("Vertical speed must clamp to TERMINAL_FALL_SPEED",
            PhysicsEngine.TERMINAL_FALL_SPEED, player.vel.y, 0.01f)
    }

    @Test
    fun wallCollision_stopsPlayerAgainstSolidWall() {
        val ts = world.tileSize
        // Place player near col 3, and place a solid stone block at col 4
        player.pos.x = 3 * ts
        player.pos.y = 5 * ts
        player.vel.x = 200f

        for (r in 4..7) {
            world.setTile(r, 4, TileType.STONE_BLOCK)
        }

        physics.updatePlayer(player, inputX = 1f, inputY = 0f, dt = 0.1f, onHitHazard = {})

        // Player's right edge should not penetrate into col 4 (4 * ts)
        assertTrue("Player must be stopped at the wall boundary",
            player.pos.x + player.width <= 4 * ts)
        assertEquals("Horizontal velocity must be zeroed upon hitting wall", 0f, player.vel.x, 0.01f)
        assertTrue("Player should register touching wall right", player.isTouchingWallRight)
    }

    @Test
    fun platformCollision_landsOnSolidPlatform() {
        val ts = world.tileSize
        // Place a solid platform at row 7
        for (c in 3..7) {
            world.setTile(7, c, TileType.GRASS_TOP)
        }

        // Place player just above row 7 platform falling downward
        player.pos.x = 4 * ts
        player.pos.y = 7 * ts - player.height - 2f
        player.vel.y = 150f

        physics.updatePlayer(player, inputX = 0f, inputY = 0f, dt = 0.05f, onHitHazard = {})

        assertTrue("Player should be grounded on platform", player.isGrounded)
        assertEquals("Vertical velocity should be zero on landing", 0f, player.vel.y, 0.01f)
        assertEquals("Feet should align with the platform top",
            7 * ts - player.height, player.pos.y, 0.05f)
    }

    @Test
    fun ceilingCollision_stopsUpwardJumpVelocity() {
        val ts = world.tileSize
        // Place solid ceiling at row 2
        for (c in 3..7) {
            world.setTile(2, c, TileType.STONE_BLOCK)
        }

        // Place player moving upward directly below ceiling
        player.pos.x = 4 * ts
        player.pos.y = 3 * ts + 1f
        player.vel.y = -300f

        physics.updatePlayer(player, inputX = 0f, inputY = 0f, dt = 0.05f, onHitHazard = {})

        assertTrue("Head must stay below ceiling bottom", player.pos.y >= 3 * ts)
        assertEquals("Upward velocity should be zeroed after ceiling impact", 0f, player.vel.y, 0.01f)
    }

    @Test
    fun oneWayPlatform_allowsJumpingUpwardThroughIt() {
        val ts = world.tileSize
        // Place one-way wood platform at row 5
        for (c in 3..7) {
            world.setTile(5, c, TileType.WOOD_PLATFORM)
        }

        // Place player below row 5 jumping upward
        player.pos.x = 4 * ts
        player.pos.y = 6 * ts
        player.vel.y = -350f

        physics.updatePlayer(player, inputX = 0f, inputY = 0f, dt = 0.05f, onHitHazard = {})

        // Player should NOT be blocked by one-way platform while jumping up
        assertTrue("Player should still be moving upward through one-way platform", player.vel.y < 0f)
        assertFalse("Player must not be grounded when jumping up through platform", player.isGrounded)
    }

    @Test
    fun oneWayPlatform_dropThroughAllowsPassingDown() {
        val ts = world.tileSize
        for (c in 3..7) {
            world.setTile(6, c, TileType.WOOD_PLATFORM)
        }

        player.pos.x = 4 * ts
        player.pos.y = 6 * ts - player.height
        player.isGrounded = true

        // Trigger drop-through with Down + Jump
        player.jump(inputY = 1.0f)

        assertTrue("Drop through timer should be active", player.dropThroughTimer > 0f)

        // Update physics - player should drop through instead of snapping back to platform
        physics.updatePlayer(player, inputX = 0f, inputY = 1.0f, dt = 0.05f, onHitHazard = {})
        assertTrue("Player should fall through the one-way platform", player.pos.y > 6 * ts - player.height)
    }

    @Test
    fun wallSliding_reducesFallSpeedWhenPressingIntoWall() {
        val ts = world.tileSize
        // Place wall on right at col 6
        for (r in 0..10) {
            world.setTile(r, 6, TileType.STONE_BLOCK)
        }

        // Place player against the wall falling
        player.pos.x = 6 * ts - player.width - 0.01f
        player.pos.y = 4 * ts
        player.vel.y = 400f
        player.isGrounded = false

        // Update with input pushing into the right wall
        physics.updatePlayer(player, inputX = 1.0f, inputY = 0f, dt = 0.05f, onHitHazard = {})

        assertTrue("Player should be wall sliding", player.isWallSliding)
        assertTrue("Fall velocity should be capped by wall slide max speed",
            player.vel.y <= PhysicsEngine.WALL_SLIDE_MAX_FALL_SPEED + 10f)
    }

    @Test
    fun jumpBuffering_executesJumpWhenLanding() {
        val ts = world.tileSize
        for (c in 3..7) {
            world.setTile(7, c, TileType.GRASS_TOP)
        }

        // Player falling in air
        player.pos.x = 4 * ts
        player.pos.y = 7 * ts - player.height - 3f
        player.vel.y = 100f
        player.isGrounded = false

        // Player presses jump right before landing
        player.jump(inputY = 0f)
        assertTrue("Jump buffer timer should be set", player.jumpBufferTimer > 0f)

        var jumpTriggered = false
        // Landing on platform triggers the buffered jump
        physics.updatePlayer(
            player,
            inputX = 0f,
            inputY = 0f,
            dt = 0.05f,
            onHitHazard = {},
            onJumpTriggered = { jumpTriggered = true }
        )

        assertTrue("Buffered jump should have been triggered upon landing", jumpTriggered)
        assertTrue("Player should have jump impulse velocity", player.vel.y < 0f)
    }
}
