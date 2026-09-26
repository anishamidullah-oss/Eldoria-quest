package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.audio.FootstepTerrain
import com.example.game.engine.GameEngine
import com.example.game.entities.PlayerState
import com.example.game.model.GameScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("The Lost Kingdom", appName)
    }

    @Test
    fun `verify player initialization and movement physics`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = GameEngine(context)
        engine.currentScreen = GameScreen.PLAYING
        assertNotNull(engine.player)
        assertEquals(PlayerState.IDLE, engine.player.state)

        // Test horizontal movement input
        engine.onRightPressed()
        engine.update(0.016f)
        assertTrue("Player should accelerate rightward", engine.player.vel.x > 0f)

        // Test jump input
        engine.player.isGrounded = true
        engine.onJumpPressed()
        assertTrue("Player should have upward jump velocity", engine.player.vel.y < 0f)
    }

    @Test
    fun `verify melee sword attack and sound manager integration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = GameEngine(context)
        engine.currentScreen = GameScreen.PLAYING
        engine.onAttackPressed()
        assertTrue("Player should be in attacking state", engine.player.isAttacking())
        assertTrue("Attack timer should be active", engine.player.attackTimer > 0f)

        // Verify sound manager methods execute without errors
        engine.soundManager.onPlayerAttack(1)
        engine.soundManager.onPlayerAttack(2)
        engine.soundManager.playFootstep(FootstepTerrain.GRASS_DIRT)
        engine.soundManager.playFootstep(FootstepTerrain.STONE)
        engine.soundManager.playFootstep(FootstepTerrain.CAVERN)
        engine.soundManager.setAreaAmbience("The Whispering Woods")
        assertEquals("FOREST", engine.soundManager.currentAmbienceArea)
        engine.soundManager.setAreaAmbience("Deeproot Caverns")
        assertEquals("CAVE", engine.soundManager.currentAmbienceArea)
    }
}

