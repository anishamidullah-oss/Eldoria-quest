package com.example.game.entities

import com.example.game.model.RectF2D
import com.example.game.model.Vector2D
import kotlin.math.abs
import kotlin.math.sin

enum class EnemyType {
    MOSS_SLIME,
    BUSH_BEETLE,
    GRASSWALKER,
    CAVE_CRAWLER,
    CAVE_BAT,
    SHADOW_GOBLIN,
    STONE_GIANT,
    SKELETON_KNIGHT,
    RUIN_COLOSSUS,
    DREADFANG_WOLF,
    FROST_WURM,
    SHADOW_LICH
}

enum class EnemyAIState {
    PATROL,
    ALERT,
    CHASE,
    ATTACK,
    REPOSITION,
    STAGGERED,
    HURT,
    DEAD
}

data class Enemy(
    val id: String,
    val type: EnemyType,
    val startX: Float,
    val startY: Float,
    val patrolMinX: Float = startX - 120f,
    val patrolMaxX: Float = startX + 120f
) {
    val pos = Vector2D(startX, startY)
    val vel = Vector2D(0f, 0f)

    var displayName = "Enemy"
    var level = 1
    var width = 28f
    var height = 28f
    var maxHealth = 30
    var health = 30
    var attackDamage = 15
    var moveSpeed = 70f
    var chaseSpeed = 120f
    var isFlying = false
    var isBoss = false

    var facingRight = true
    var aiState = EnemyAIState.PATROL
    var stateTimer = 0f
    var hurtTimer = 0f
    var attackCooldown = 0f
    var alertTimer = 0f
    var bossAttackPattern = 0 // 0: Slam, 1: Dash Cleave, 2: Jump Stomp
    var isEnraged = false

    // AI parameters
    var detectionRange = 180f
    var attackRange = 36f
    var isGrounded = false
    var animTimer = (Math.random() * 5.0).toFloat()

    val bounds = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)
    val attackHitbox = RectF2D()

    init {
        when (type) {
            EnemyType.MOSS_SLIME -> {
                displayName = "Moss Slime"
                level = 1
                width = 28f
                height = 22f
                maxHealth = 25
                health = maxHealth
                attackDamage = 10
                moveSpeed = 50f
                chaseSpeed = 85f
                detectionRange = 150f
                attackRange = 30f
                isFlying = false
            }
            EnemyType.GRASSWALKER -> {
                displayName = "Grasswalker"
                level = 1
                width = 30f
                height = 20f
                maxHealth = 30
                health = maxHealth
                attackDamage = 12
                moveSpeed = 65f
                chaseSpeed = 110f
                detectionRange = 180f
                attackRange = 32f
                isFlying = false
            }
            EnemyType.BUSH_BEETLE -> {
                displayName = "Bush Beetle"
                level = 2
                width = 34f
                height = 24f
                maxHealth = 40
                health = maxHealth
                attackDamage = 15
                moveSpeed = 60f
                chaseSpeed = 100f
                detectionRange = 190f
                attackRange = 34f
                isFlying = false
            }
            EnemyType.CAVE_CRAWLER -> {
                displayName = "Cave Beast"
                level = 2
                width = 36f
                height = 28f
                maxHealth = 50
                health = maxHealth
                attackDamage = 16
                moveSpeed = 80f
                chaseSpeed = 140f
                detectionRange = 210f
                attackRange = 42f
                isFlying = false
            }
            EnemyType.CAVE_BAT -> {
                displayName = "Cave Bat"
                level = 1
                width = 26f
                height = 22f
                maxHealth = 18
                health = maxHealth
                attackDamage = 10
                moveSpeed = 60f
                chaseSpeed = 110f
                detectionRange = 220f
                attackRange = 28f
                isFlying = true
            }
            EnemyType.SHADOW_GOBLIN -> {
                displayName = "Shadow Goblin"
                level = 2
                width = 30f
                height = 42f
                maxHealth = 45
                health = maxHealth
                attackDamage = 18
                moveSpeed = 75f
                chaseSpeed = 135f
                detectionRange = 200f
                attackRange = 40f
                isFlying = false
            }
            EnemyType.STONE_GIANT -> {
                displayName = "Stone Giant"
                level = 3
                width = 44f
                height = 52f
                maxHealth = 80
                health = maxHealth
                attackDamage = 22
                moveSpeed = 45f
                chaseSpeed = 75f
                detectionRange = 220f
                attackRange = 45f
                isFlying = false
            }
            EnemyType.SKELETON_KNIGHT -> {
                displayName = "Skeleton Knight"
                level = 4
                width = 38f
                height = 54f
                maxHealth = 95
                health = maxHealth
                attackDamage = 24
                moveSpeed = 60f
                chaseSpeed = 105f
                detectionRange = 230f
                attackRange = 48f
                isFlying = false
            }
            EnemyType.RUIN_COLOSSUS -> {
                displayName = "Gorgaroth the Ruin Colossus"
                level = 6
                width = 62f
                height = 76f
                maxHealth = 280
                health = maxHealth
                attackDamage = 30
                moveSpeed = 50f
                chaseSpeed = 90f
                detectionRange = 320f
                attackRange = 65f
                isFlying = false
                isBoss = true
            }
            EnemyType.DREADFANG_WOLF -> {
                displayName = "Dreadfang Shadow Wolf"
                level = 4
                width = 46f
                height = 32f
                maxHealth = 160
                health = maxHealth
                attackDamage = 22
                moveSpeed = 95f
                chaseSpeed = 160f
                detectionRange = 260f
                attackRange = 46f
                isFlying = false
                isBoss = true
            }
            EnemyType.FROST_WURM -> {
                displayName = "Glacial Frost Wurm"
                level = 5
                width = 54f
                height = 42f
                maxHealth = 210
                health = maxHealth
                attackDamage = 26
                moveSpeed = 70f
                chaseSpeed = 125f
                detectionRange = 280f
                attackRange = 55f
                isFlying = false
                isBoss = true
            }
            EnemyType.SHADOW_LICH -> {
                displayName = "Malakor the Shadow Lich"
                level = 6
                width = 48f
                height = 58f
                maxHealth = 240
                health = maxHealth
                attackDamage = 28
                moveSpeed = 65f
                chaseSpeed = 110f
                detectionRange = 300f
                attackRange = 60f
                isFlying = true
                isBoss = true
            }
        }
        updateBounds()
    }

    fun update(dt: Float, player: Player) {
        stateTimer += dt
        animTimer += dt

        if (hurtTimer > 0f) {
            hurtTimer -= dt
            if (hurtTimer <= 0f && aiState == EnemyAIState.HURT) {
                aiState = EnemyAIState.CHASE
            }
        }

        if (attackCooldown > 0f) {
            attackCooldown -= dt
        }

        if (aiState == EnemyAIState.DEAD) {
            return
        }

        val distToPlayer = pos.distanceTo(player.pos)
        val dirToPlayerX = player.pos.x - pos.x

        // Boss Enrage Check
        if (isBoss && health < maxHealth * 0.5f) {
            isEnraged = true
        }

        // AI State transitions
        when (aiState) {
            EnemyAIState.PATROL -> {
                if (distToPlayer < detectionRange && player.state != PlayerState.DEAD) {
                    aiState = EnemyAIState.ALERT
                    alertTimer = if (isBoss) 0.2f else 0.35f
                    stateTimer = 0f
                    vel.x = 0f
                } else {
                    // Normal patrol
                    if (facingRight) {
                        vel.x = moveSpeed
                        if (pos.x >= patrolMaxX) {
                            facingRight = false
                        }
                    } else {
                        vel.x = -moveSpeed
                        if (pos.x <= patrolMinX) {
                            facingRight = true
                        }
                    }
                    if (isFlying) {
                        vel.y = sin(animTimer * 3f) * 20f
                    }
                }
            }
            EnemyAIState.ALERT -> {
                alertTimer -= dt
                facingRight = dirToPlayerX > 0
                if (alertTimer <= 0f) {
                    aiState = EnemyAIState.CHASE
                }
            }
            EnemyAIState.CHASE -> {
                if (distToPlayer > detectionRange * 1.6f || player.state == PlayerState.DEAD) {
                    aiState = EnemyAIState.PATROL
                } else {
                    facingRight = dirToPlayerX > 0
                    val speed = if (isEnraged) chaseSpeed * 1.35f else chaseSpeed
                    vel.x = if (facingRight) speed else -speed

                    if (isFlying) {
                        val dirY = player.pos.y - pos.y
                        vel.y = (dirY * 1.5f).coerceIn(-120f, 120f)
                    } else if (type == EnemyType.MOSS_SLIME) {
                        if (isGrounded && stateTimer > 0.8f) {
                            vel.y = -220f
                            isGrounded = false
                            stateTimer = 0f
                        }
                    } else if (type == EnemyType.CAVE_CRAWLER) {
                        // Cave beast pounce jump
                        if (isGrounded && distToPlayer < 90f && stateTimer > 0.9f) {
                            vel.y = -240f
                            vel.x = if (facingRight) 200f else -200f
                            isGrounded = false
                            stateTimer = 0f
                        }
                    } else if (type == EnemyType.SHADOW_GOBLIN) {
                        if (isGrounded && (pos.x in patrolMinX..patrolMaxX) && stateTimer > 1.2f) {
                            vel.y = -280f
                            isGrounded = false
                            stateTimer = 0f
                        }
                    }

                    if (distToPlayer <= attackRange && attackCooldown <= 0f) {
                        aiState = EnemyAIState.ATTACK
                        stateTimer = 0f
                        vel.x = 0f
                        if (isBoss) {
                            bossAttackPattern = (bossAttackPattern + 1) % 3
                        }
                    }
                }
            }
            EnemyAIState.ATTACK -> {
                if (isBoss) {
                    // Boss Attack execution
                    when (bossAttackPattern) {
                        0 -> { // Heavy Ground Slam (Staggers on finish)
                            if (stateTimer < 0.45f) {
                                vel.x = 0f // Wind-up
                            } else if (stateTimer < 0.70f) {
                                updateAttackHitbox()
                            } else {
                                attackHitbox.set(0f, 0f, 0f, 0f)
                                aiState = EnemyAIState.STAGGERED
                                stateTimer = 0f
                            }
                        }
                        1 -> { // Dash Cleave
                            if (stateTimer < 0.25f) {
                                vel.x = 0f
                            } else if (stateTimer < 0.60f) {
                                vel.x = if (facingRight) 160f else -160f
                                updateAttackHitbox()
                            } else {
                                attackHitbox.set(0f, 0f, 0f, 0f)
                                attackCooldown = if (isEnraged) 0.6f else 1.2f
                                aiState = EnemyAIState.CHASE
                            }
                        }
                        else -> { // Jump Stomp
                            if (stateTimer < 0.2f && isGrounded) {
                                vel.y = -350f
                                vel.x = if (facingRight) 120f else -120f
                                isGrounded = false
                            } else if (stateTimer in 0.35f..0.7f) {
                                updateAttackHitbox()
                            } else if (stateTimer > 0.8f) {
                                attackHitbox.set(0f, 0f, 0f, 0f)
                                attackCooldown = if (isEnraged) 0.8f else 1.5f
                                aiState = EnemyAIState.CHASE
                            }
                        }
                    }
                } else {
                    // Standard Enemy Attack
                    if (stateTimer < 0.25f) {
                        vel.x = 0f
                    } else if (stateTimer < 0.45f) {
                        updateAttackHitbox()
                    } else {
                        attackHitbox.set(0f, 0f, 0f, 0f)
                        if (type == EnemyType.SHADOW_GOBLIN || type == EnemyType.CAVE_CRAWLER) {
                            aiState = EnemyAIState.REPOSITION
                            stateTimer = 0f
                        } else {
                            attackCooldown = 0.8f
                            aiState = EnemyAIState.CHASE
                        }
                    }
                }
            }
            EnemyAIState.REPOSITION -> {
                // Quick tactical retreat to keep player guessing
                if (stateTimer < 0.35f) {
                    vel.x = if (facingRight) -90f else 90f
                } else {
                    attackCooldown = 0.7f
                    aiState = EnemyAIState.CHASE
                }
            }
            EnemyAIState.STAGGERED -> {
                // Boss vulnerable window
                vel.x = 0f
                if (stateTimer > 1.2f) {
                    attackCooldown = if (isEnraged) 0.5f else 1.2f
                    aiState = EnemyAIState.CHASE
                }
            }
            EnemyAIState.HURT -> {
                // Knockback decay
            }
            EnemyAIState.DEAD -> {}
        }

        updateBounds()
    }

    private fun updateAttackHitbox() {
        val reach = attackRange + 10f
        val topOffset = 4f
        val attackHeight = height - 8f
        if (facingRight) {
            attackHitbox.set(pos.x + width * 0.4f, pos.y + topOffset, pos.x + width + reach, pos.y + topOffset + attackHeight)
        } else {
            attackHitbox.set(pos.x - reach, pos.y + topOffset, pos.x + width * 0.6f, pos.y + topOffset + attackHeight)
        }
    }

    fun takeDamage(damage: Int, knockbackX: Float): Boolean {
        if (aiState == EnemyAIState.DEAD) return false

        val finalDamage = if (aiState == EnemyAIState.STAGGERED) (damage * 1.5f).toInt() else damage
        health -= finalDamage
        hurtTimer = 0.3f

        if (isBoss) {
            // Boss has high poise and reduced knockback
            if (aiState != EnemyAIState.STAGGERED) {
                aiState = EnemyAIState.HURT
                vel.set(knockbackX * 0.25f, -60f)
            }
        } else {
            aiState = EnemyAIState.HURT
            vel.set(knockbackX, if (isFlying) -80f else -180f)
            isGrounded = false
        }

        if (health <= 0) {
            health = 0
            aiState = EnemyAIState.DEAD
            return true // Died
        }
        return false // Still alive
    }

    fun updateBounds() {
        bounds.set(pos.x, pos.y, pos.x + width, pos.y + height)
    }
}
