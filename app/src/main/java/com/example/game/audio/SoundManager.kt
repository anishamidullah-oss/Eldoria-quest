package com.example.game.audio

import com.example.game.entities.Player
import com.example.game.entities.PlayerState
import com.example.game.world.TileType
import com.example.game.world.WorldMap
import kotlin.math.abs

class SoundManager : FantasySoundEngine() {

    private var footstepTimer = 0f
    private val footstepInterval = 0.32f
    private var lastObservedArea: String = ""

    var masterVolume: Float = 1.0f
    var sfxVolume: Float = 1.0f
    var musicVolume: Float = 0.8f

    fun update(player: Player, world: WorldMap, currentAreaName: String, dt: Float) {
        // 1. Manage terrain footfalls
        updateFootsteps(player, world, dt)

        // 2. Manage area ambience background tracks
        if (currentAreaName != lastObservedArea) {
            lastObservedArea = currentAreaName
            setAreaAmbience(currentAreaName)
        }
    }

    fun updateFootsteps(player: Player, world: WorldMap, dt: Float) {
        val isRunningOnGround = player.isGrounded &&
                (player.state == PlayerState.RUN || abs(player.vel.x) > 20f) &&
                !player.isAttacking() &&
                player.state != PlayerState.DEAD

        if (isRunningOnGround) {
            footstepTimer -= dt
            if (footstepTimer <= 0f) {
                footstepTimer = footstepInterval
                val terrain = getTerrainUnderPlayer(player, world)
                playFootstep(terrain)
            }
        } else {
            // Reset timer so first step sound plays immediately when starting to run
            if (footstepTimer < 0.08f) {
                footstepTimer = 0.08f
            }
        }
    }

    fun getTerrainUnderPlayer(player: Player, world: WorldMap): FootstepTerrain {
        val checkX = player.pos.x + player.width * 0.5f
        val checkY = player.pos.y + player.height + 4f
        val tile = world.getTileAtWorldPos(checkX, checkY)

        return when (tile) {
            TileType.GRASS_TOP,
            TileType.GRASS_LEFT,
            TileType.GRASS_RIGHT,
            TileType.DIRT -> FootstepTerrain.GRASS_DIRT

            TileType.STONE_BLOCK,
            TileType.STONE_BRICK,
            TileType.RUIN_STONE,
            TileType.RUIN_CRUMBLE_BLOCK,
            TileType.VILLAGE_COBBLE,
            TileType.COBBLESTONE,
            TileType.SHRINE_ALTAR,
            TileType.CASTLE_WALL -> FootstepTerrain.STONE

            TileType.CAVE_ROCK,
            TileType.CAVE_WALL -> FootstepTerrain.CAVERN

            TileType.WOOD_PLATFORM,
            TileType.WOOD_PLANK,
            TileType.VILLAGE_ROOF,
            TileType.ROOF_TILE -> FootstepTerrain.WOOD

            TileType.SNOW_TOP,
            TileType.ICE_BLOCK -> FootstepTerrain.ICE_SNOW

            TileType.WATER,
            TileType.WATERFALL -> FootstepTerrain.WATER

            else -> {
                // Check if the current area itself implies cave or forest when falling back
                if (lastObservedArea.contains("Cavern", ignoreCase = true) ||
                    lastObservedArea.contains("Cave", ignoreCase = true)
                ) {
                    FootstepTerrain.CAVERN
                } else {
                    FootstepTerrain.GRASS_DIRT
                }
            }
        }
    }

    fun onPlayerAttack(comboStep: Int = 1) {
        playSwordSwing(comboStep)
    }

    fun onPlayerJump() {
        playJump()
    }

    fun onCoinCollected() {
        playCoin()
    }

    fun onCrystalCollected() {
        playCrystal()
    }

    fun onTargetHit() {
        playHit()
    }

    fun onEnemyDefeated() {
        playEnemyDeath()
    }

    fun onChestOpened() {
        playChestOpen()
    }

    fun onLevelUp() {
        playLevelUp()
    }

    fun onGameOver() {
        playGameOver()
    }

    fun onVictory() {
        playVictory()
    }

    fun onPoisonTick() {
        playPoisonTick()
    }

    fun onBurnTick() {
        playBurnTick()
    }

    fun onExtinguish() {
        playExtinguish()
    }

    fun onSpeedBuffApplied() {
        playSpeedBuff()
    }

    fun onHealTick() {
        playHealTick()
    }
}
