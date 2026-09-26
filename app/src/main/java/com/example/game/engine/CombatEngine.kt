package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.audio.FantasySoundEngine
import com.example.game.entities.Collectible
import com.example.game.entities.CollectibleType
import com.example.game.entities.Enemy
import com.example.game.entities.EnemyAIState
import com.example.game.entities.EnemyType
import com.example.game.entities.Particle
import com.example.game.entities.ParticleType
import com.example.game.entities.Player
import com.example.game.model.Vector2D
import com.example.game.world.TileType
import com.example.game.world.WorldMap
import kotlin.random.Random

class CombatEngine(
    private val soundEngine: FantasySoundEngine
) {

    fun updateCombat(
        player: Player,
        enemies: MutableList<Enemy>,
        collectibles: MutableList<Collectible>,
        particles: MutableList<Particle>,
        world: WorldMap? = null,
        onPlayerDied: () -> Unit,
        onTriggerScreenShake: (intensity: Float) -> Unit,
        onBossDefeated: ((Enemy) -> Unit)? = null,
        onPuzzleTriggered: ((String) -> Unit)? = null
    ) {
        // 1. Player Melee Attack vs Enemies & Environmental Puzzle Objects (synced to peak swing frame)
        if (player.isAttackHitboxActive && !player.hasHitTargetThisSwing) {
            val attackBounds = player.attackHitbox

            // A. Check enemies
            for (enemy in enemies) {
                if (enemy.aiState != EnemyAIState.DEAD && attackBounds.overlaps(enemy.bounds)) {
                    player.hasHitTargetThisSwing = true

                    // Calculate damage (base + combo bonus + player level bonus + crit chance)
                    val isCrit = Random.nextFloat() < 0.22f
                    val comboBase = when (player.comboStep) {
                        2 -> 35
                        else -> 22
                    }
                    val baseDamage = ((comboBase + player.bonusDamage) * player.effectiveDamageMultiplier).toInt()
                    val totalDamage = if (isCrit) (baseDamage * 1.5f).toInt() else baseDamage
                    val knockbackDir = if (player.facingRight) 180f else -180f

                    val died = enemy.takeDamage(totalDamage, knockbackDir)
                    soundEngine.playHit()
                    onTriggerScreenShake(if (isCrit) 10f else 5f)

                    // Spawn impact sparks
                    for (i in 0..6) {
                        particles.add(
                            Particle(
                                pos = Vector2D(enemy.pos.x + enemy.width * 0.5f, enemy.pos.y + enemy.height * 0.5f),
                                velocity = Vector2D(Random.nextFloat() * 200f - 100f, Random.nextFloat() * -160f - 40f),
                                life = 0.35f,
                                maxLife = 0.35f,
                                color = if (isCrit) Color(0xFFFFD700) else Color(0xFFFF6B6B),
                                size = if (isCrit) 6f else 4f,
                                type = ParticleType.SWORD_SPARK
                            )
                        )
                    }

                    // Spawn floating damage text
                    particles.add(
                        Particle(
                            pos = Vector2D(enemy.pos.x + enemy.width * 0.5f - 10f, enemy.pos.y - 12f),
                            velocity = Vector2D(Random.nextFloat() * 30f - 15f, -90f),
                            life = 0.85f,
                            maxLife = 0.85f,
                            color = if (isCrit) Color(0xFFFFCC00) else Color(0xFFFFFFFF),
                            size = if (isCrit) 18f else 14f,
                            type = ParticleType.DAMAGE_TEXT,
                            text = if (isCrit) "CRIT $totalDamage!" else "$totalDamage",
                            isCritical = isCrit
                        )
                    )

                    if (died) {
                        handleEnemyDeath(enemy, player, collectibles, particles)
                        if (enemy.isBoss) {
                            onBossDefeated?.invoke(enemy)
                        }
                    }
                    break // One hit per swing arc
                }
            }

            // B. Check Runic Torches (strike with weapon to ignite)
            if (world != null) {
                for (torch in world.runicTorches) {
                    if (!torch.isLit && attackBounds.overlaps(torch.bounds)) {
                        val ignited = torch.ignite()
                        if (ignited) {
                            player.hasHitTargetThisSwing = true
                            soundEngine.playCrystal()
                            onTriggerScreenShake(6f)

                            // Fire burst particles
                            for (i in 0..12) {
                                particles.add(
                                    Particle(
                                        pos = Vector2D(torch.pos.x + torch.width * 0.5f, torch.pos.y + 10f),
                                        velocity = Vector2D(Random.nextFloat() * 140f - 70f, Random.nextFloat() * -120f - 30f),
                                        life = 0.6f,
                                        maxLife = 0.6f,
                                        color = if (i % 2 == 0) Color(0xFFFF9100) else Color(0xFF00E5FF),
                                        size = 5f,
                                        type = ParticleType.COIN_SPARKLE
                                    )
                                )
                            }
                            onPuzzleTriggered?.invoke(torch.groupId)
                        }
                    }
                }

                // C. Check Crumble Walls (strike to shatter)
                for (wall in world.crumbleWalls) {
                    if (!wall.isBroken && attackBounds.overlaps(wall.bounds)) {
                        player.hasHitTargetThisSwing = true
                        val shattered = wall.hit()
                        soundEngine.playHit()
                        onTriggerScreenShake(8f)

                        // Debris particles
                        for (i in 0..10) {
                            particles.add(
                                Particle(
                                    pos = Vector2D(wall.pos.x + 16f, wall.pos.y + 16f),
                                    velocity = Vector2D(Random.nextFloat() * 180f - 90f, Random.nextFloat() * -160f - 20f),
                                    life = 0.5f,
                                    maxLife = 0.5f,
                                    color = Color(0xFF8D6E63),
                                    size = 6f,
                                    type = ParticleType.DEATH_POOF
                                )
                            )
                        }

                        if (shattered) {
                            world.setTile(wall.row, wall.col, TileType.AIR)
                            soundEngine.playChestOpen()
                            onTriggerScreenShake(12f)
                            particles.add(
                                Particle(
                                    pos = Vector2D(wall.pos.x - 10f, wall.pos.y - 15f),
                                    velocity = Vector2D(0f, -50f),
                                    life = 1.6f,
                                    maxLife = 1.6f,
                                    color = Color(0xFFFFD700),
                                    size = 14f,
                                    type = ParticleType.DAMAGE_TEXT,
                                    text = "SECRET PASSAGE REVEALED!"
                                )
                            )
                            player.secretsFoundCount++
                        }
                    }
                }
            }
        }

        // 2. Enemy Attacks vs Player
        for (enemy in enemies) {
            if (enemy.aiState == EnemyAIState.DEAD) continue

            // Touch damage or melee hitbox damage
            val touchesPlayer = enemy.bounds.overlaps(player.bounds)
            val strikesPlayer = enemy.aiState == EnemyAIState.ATTACK &&
                    enemy.stateTimer in 0.25f..0.45f &&
                    enemy.attackHitbox.overlaps(player.bounds)

            if ((touchesPlayer || strikesPlayer) && !player.isInvulnerable) {
                val damage = enemy.attackDamage
                val knockbackDir = if (enemy.pos.x < player.pos.x) 240f else -240f
                val tookDamage = player.takeDamage(damage, knockbackDir)

                if (tookDamage) {
                    soundEngine.playHit()
                    onTriggerScreenShake(8f)

                    // Spawn hurt particles
                    for (i in 0..5) {
                        particles.add(
                            Particle(
                                pos = Vector2D(player.pos.x + player.width * 0.5f, player.pos.y + player.height * 0.5f),
                                velocity = Vector2D(Random.nextFloat() * 160f - 80f, Random.nextFloat() * -140f - 30f),
                                life = 0.35f,
                                maxLife = 0.35f,
                                color = Color(0xFFFF3333),
                                size = 4f,
                                type = ParticleType.BLOOD_SPARK
                            )
                        )
                    }

                    // Floating damage for player
                    particles.add(
                        Particle(
                            pos = Vector2D(player.pos.x + player.width * 0.5f - 8f, player.pos.y - 12f),
                            velocity = Vector2D(0f, -80f),
                            life = 0.8f,
                            maxLife = 0.8f,
                            color = Color(0xFFFF4444),
                            size = 15f,
                            type = ParticleType.DAMAGE_TEXT,
                            text = "-$damage"
                        )
                    )

                    if (player.health <= 0) {
                        soundEngine.playGameOver()
                        onPlayerDied()
                    } else {
                        // Apply enemy status effects
                        when (enemy.type) {
                            EnemyType.MOSS_SLIME -> {
                                player.applyStatusEffect(com.example.game.effects.StatusEffect.poison(duration = 5.0f, tickInterval = 1.0f, tickDamage = 3))
                            }
                            EnemyType.CAVE_CRAWLER -> {
                                player.applyStatusEffect(com.example.game.effects.StatusEffect.poison(duration = 6.0f, tickInterval = 0.9f, tickDamage = 4))
                            }
                            EnemyType.SHADOW_LICH -> {
                                player.applyStatusEffect(com.example.game.effects.StatusEffect.burning(duration = 5.0f, tickInterval = 0.65f, tickDamage = 4))
                            }
                            EnemyType.FROST_WURM -> {
                                player.applyStatusEffect(com.example.game.effects.StatusEffect.frostChill(duration = 4.0f, speedMultiplier = 0.65f))
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun handleEnemyDeath(
        enemy: Enemy,
        player: Player,
        collectibles: MutableList<Collectible>,
        particles: MutableList<Particle>
    ) {
        soundEngine.playEnemyDeath()
        player.enemiesDefeated++
        player.score += if (enemy.isBoss) 500 else 100

        // Quest Tracking & XP Gain
        val xpGain = when (enemy.type) {
            EnemyType.MOSS_SLIME -> {
                player.questSlimesDefeated++
                25
            }
            EnemyType.GRASSWALKER -> 35
            EnemyType.BUSH_BEETLE -> 40
            EnemyType.CAVE_CRAWLER -> 55
            EnemyType.CAVE_BAT -> 30
            EnemyType.SHADOW_GOBLIN -> 45
            EnemyType.STONE_GIANT -> 75
            EnemyType.SKELETON_KNIGHT -> 120
            EnemyType.DREADFANG_WOLF -> 250
            EnemyType.FROST_WURM -> 320
            EnemyType.SHADOW_LICH -> 350
            EnemyType.RUIN_COLOSSUS -> {
                player.questBossDefeated = true
                500
            }
        }

        if (enemy.isBoss) {
            player.defeatedBosses.add(enemy.id)
            if (enemy.type == EnemyType.RUIN_COLOSSUS) {
                player.questBossDefeated = true
            }
            // Spawn Boss Defeated Banner
            particles.add(
                Particle(
                    pos = Vector2D(enemy.pos.x + enemy.width * 0.5f - 50f, enemy.pos.y - 45f),
                    velocity = Vector2D(0f, -40f),
                    life = 2.5f,
                    maxLife = 2.5f,
                    color = Color(0xFFFFD700),
                    size = 18f,
                    type = ParticleType.DAMAGE_TEXT,
                    text = "${enemy.displayName.uppercase()} SLAIN!"
                )
            )
        }

        // Spawn XP text
        particles.add(
            Particle(
                pos = Vector2D(enemy.pos.x + enemy.width * 0.5f - 8f, enemy.pos.y - 24f),
                velocity = Vector2D(0f, -60f),
                life = 0.9f,
                maxLife = 0.9f,
                color = Color(0xFF81D4FA),
                size = 13f,
                type = ParticleType.DAMAGE_TEXT,
                text = "+$xpGain XP"
            )
        )

        // Process Level Up
        val didLevelUp = player.addXP(xpGain)
        if (didLevelUp) {
            soundEngine.playCheckpoint()
            // Level up text
            particles.add(
                Particle(
                    pos = Vector2D(player.pos.x + player.width * 0.5f - 24f, player.pos.y - 30f),
                    velocity = Vector2D(0f, -70f),
                    life = 1.6f,
                    maxLife = 1.6f,
                    color = Color(0xFFFFD700),
                    size = 16f,
                    type = ParticleType.DAMAGE_TEXT,
                    text = "LEVEL UP! Lv.${player.level}"
                )
            )
            // Golden sparkles
            for (i in 0..16) {
                particles.add(
                    Particle(
                        pos = Vector2D(player.pos.x + player.width * 0.5f, player.pos.y + player.height * 0.5f),
                        velocity = Vector2D(Random.nextFloat() * 200f - 100f, Random.nextFloat() * -180f - 40f),
                        life = 0.6f,
                        maxLife = 0.6f,
                        color = Color(0xFFFFD700),
                        size = 5f,
                        type = ParticleType.COIN_SPARKLE
                    )
                )
            }
        }

        // Spawn death poof particles
        for (i in 0..14) {
            particles.add(
                Particle(
                    pos = Vector2D(enemy.pos.x + enemy.width * 0.5f, enemy.pos.y + enemy.height * 0.5f),
                    velocity = Vector2D(Random.nextFloat() * 220f - 110f, Random.nextFloat() * 220f - 110f),
                    life = 0.5f,
                    maxLife = 0.5f,
                    color = when (enemy.type) {
                        EnemyType.MOSS_SLIME -> Color(0xFF7CB342)
                        EnemyType.GRASSWALKER -> Color(0xFF2E7D32)
                        EnemyType.BUSH_BEETLE -> Color(0xFF33691E)
                        EnemyType.CAVE_CRAWLER -> Color(0xFF7C4DFF)
                        EnemyType.CAVE_BAT -> Color(0xFF7E57C2)
                        EnemyType.SHADOW_GOBLIN -> Color(0xFFFF7043)
                        EnemyType.STONE_GIANT -> Color(0xFF78909C)
                        EnemyType.SKELETON_KNIGHT -> Color(0xFFE0E0E0)
                        EnemyType.DREADFANG_WOLF -> Color(0xFF37474F)
                        EnemyType.FROST_WURM -> Color(0xFF80DEEA)
                        EnemyType.SHADOW_LICH -> Color(0xFF6A1B9A)
                        EnemyType.RUIN_COLOSSUS -> Color(0xFFFF3D00)
                    },
                    size = Random.nextFloat() * 6f + 3f,
                    type = ParticleType.DEATH_POOF
                )
            )
        }

        // Spawn loot drop
        val dropCoins = if (enemy.isBoss) 8 else Random.nextInt(1, 3)
        for (i in 0 until dropCoins) {
            collectibles.add(
                Collectible(
                    id = "loot_coin_${System.nanoTime()}_$i",
                    pos = Vector2D(enemy.pos.x + (i * 12f) - 6f, enemy.pos.y - 8f),
                    type = CollectibleType.COIN,
                    value = 10
                )
            )
        }

        if (enemy.isBoss || Random.nextFloat() < 0.35f) {
            collectibles.add(
                Collectible(
                    id = "loot_crystal_${System.nanoTime()}",
                    pos = Vector2D(enemy.pos.x + enemy.width * 0.5f, enemy.pos.y - 12f),
                    type = CollectibleType.MANA_CRYSTAL,
                    value = 50
                )
            )
            if (enemy.isBoss) {
                collectibles.add(
                    Collectible(
                        id = "loot_potion_${System.nanoTime()}",
                        pos = Vector2D(enemy.pos.x + enemy.width * 0.5f + 16f, enemy.pos.y - 12f),
                        type = CollectibleType.HEALTH_POTION,
                        value = 50
                    )
                )
            }
        }
    }
}
