package com.example.game.entities

import com.example.game.effects.StatusEffect
import com.example.game.effects.StatusEffectManager
import com.example.game.effects.StatusEffectType
import com.example.game.model.RectF2D
import com.example.game.model.Vector2D
import kotlin.math.abs

enum class PlayerState {
    IDLE,
    RUN,
    JUMP,
    FALL,
    CLIMB_LADDER,
    ATTACK_1,
    ATTACK_2,
    ATTACK_AIR,
    HURT,
    DEAD
}

class Player(
    startX: Float = 100f,
    startY: Float = 200f
) {
    val pos = Vector2D(startX, startY)
    val vel = Vector2D(0f, 0f)

    val width = 28f
    val height = 46f

    var facingRight = true
    var state = PlayerState.IDLE
    var stateTimer = 0f

    // Health & Progression
    var level = 1
    var xp = 0
    var xpToNextLevel = 100
    var bonusMaxHealth = 0
    var baseMaxHealth = 100
    var maxHealth: Int
        get() = baseMaxHealth + bonusMaxHealth
        set(value) {
            baseMaxHealth = value - bonusMaxHealth
        }
    var health = 100
    var bonusDamage = 0
    var invulnerableTimer = 0f
    val isInvulnerable: Boolean get() = invulnerableTimer > 0f

    // Inventory & Equipment
    var potions = 2
    var swiftnessPotions = 2
    var citadelKeysCount = 0
    val hasCitadelKey: Boolean get() = citadelKeysCount >= 3
    var hasAncientRelic = false
    var hasHeartFragment1 = false
    var hasHeartFragment2 = false
    var hasHeartFragment3 = false
    var hasMasterKey = false
    var hasBrassCompass = false
    var hasMapParchment = false
    var runicOreCount = 0
    var hasGuardBadge = false
    var equippedWeapon = "Steel Broadsword"
    var equippedArmor = "Adventurer Tunic"
    var equippedAccessory = "Warrior Ring"
    val unlockedAreas = mutableSetOf("The Whispering Woods")

    // Exploration & Secrets Tracking
    var secretsFoundCount = 0
    val solvedPuzzles = mutableSetOf<String>()
    val openedChests = mutableSetOf<String>()
    val defeatedBosses = mutableSetOf<String>()
    val collectedSecretRelics = mutableSetOf<String>()

    // Quest & Story tracking
    var activeQuestId = 1
    var activeQuestProgress = 0
    val completedQuestIds = mutableSetOf<Int>()
    val readInscriptions = mutableSetOf<String>()
    val acceptedSideQuestIds = mutableSetOf<String>()
    val completedSideQuestIds = mutableSetOf<String>()
    var questSlimesDefeated = 0
    var questCrystalsCollected = 0
    var questChestsOpened = 0
    var questBossDefeated = false

    // Status Effect System
    val statusEffects = StatusEffectManager()
    val effectiveMoveSpeed: Float get() = moveSpeed * statusEffects.speedMultiplier
    val effectiveClimbSpeed: Float get() = climbSpeed * statusEffects.speedMultiplier
    val effectiveDamageMultiplier: Float get() = statusEffects.damageMultiplier

    // Movement tuning
    var moveSpeed = 220f
    val jumpImpulse = -430f
    val climbSpeed = 160f
    val gravity = 980f

    // Platforming feel helpers
    var isGrounded = false
    var isOnLadder = false
    var isClimbing = false
    var coyoteTimer = 0f
    var jumpBufferTimer = 0f
    var dropThroughTimer = 0f
    var isTouchingWallLeft = false
    var isTouchingWallRight = false
    var isWallSliding = false
    var onIce = false

    // Combat
    var attackTimer = 0f
    val attackDuration = 0.28f
    val attackTotalFrames = 4 // Frame 0: Windup, Frame 1: Forward Slash, Frame 2: Peak Apex (Hitbox active), Frame 3: Recovery
    val attackPeakFrame = 2
    var attackFrame = 0
    val isAttackHitboxActive: Boolean
        get() = isAttacking() && attackFrame == attackPeakFrame
    var comboStep = 0
    var comboResetTimer = 0f
    var hasHitTargetThisSwing = false

    // Collision boxes
    val bounds = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)
    val attackHitbox = RectF2D()

    // Stats
    var coins = 0
    var crystals = 0
    var score = 0
    var enemiesDefeated = 0

    // Respawn point
    val respawnPos = Vector2D(startX, startY)

    fun addXP(amount: Int): Boolean {
        xp += amount
        if (xp >= xpToNextLevel) {
            xp -= xpToNextLevel
            level++
            xpToNextLevel = (xpToNextLevel * 1.5f).toInt()
            bonusMaxHealth += 15
            bonusDamage += 5
            health = maxHealth // Full recovery upon leveling up
            return true
        }
        return false
    }

    fun addXp(amount: Int): Boolean = addXP(amount)

    fun usePotion(): Boolean {
        if (potions > 0 && health < maxHealth && state != PlayerState.DEAD) {
            potions--
            heal(45)
            return true
        }
        return false
    }

    fun useSwiftnessPotion(): Boolean {
        if (swiftnessPotions > 0 && state != PlayerState.DEAD) {
            swiftnessPotions--
            applyStatusEffect(StatusEffect.speedBuff(duration = 9.0f, speedMultiplier = 1.45f))
            return true
        }
        return false
    }

    fun applyStatusEffect(effect: StatusEffect): Boolean = statusEffects.applyEffect(effect)

    fun removeStatusEffect(type: StatusEffectType): Boolean = statusEffects.removeEffect(type)

    fun hasStatusEffect(type: StatusEffectType): Boolean = statusEffects.hasEffect(type)

    fun getStatusEffect(type: StatusEffectType): StatusEffect? = statusEffects.getEffect(type)

    fun clearStatusEffects() = statusEffects.clearAll()

    fun takeStatusDamage(amount: Int): Boolean {
        if (state == PlayerState.DEAD) return false
        health -= amount
        if (health <= 0) {
            health = 0
            state = PlayerState.DEAD
            vel.set(0f, -150f)
            return true
        }
        return false
    }

    fun update(dt: Float) {
        stateTimer += dt

        if (invulnerableTimer > 0f) {
            invulnerableTimer -= dt
        }

        if (comboResetTimer > 0f) {
            comboResetTimer -= dt
            if (comboResetTimer <= 0f) {
                comboStep = 0
            }
        }

        if (jumpBufferTimer > 0f) {
            jumpBufferTimer -= dt
        }

        if (dropThroughTimer > 0f) {
            dropThroughTimer -= dt
        }

        if (isGrounded) {
            coyoteTimer = 0.12f
        } else {
            coyoteTimer -= dt
        }

        // Attack state handling
        if (isAttacking()) {
            attackTimer -= dt
            val attackProgress = (1f - (attackTimer / attackDuration)).coerceIn(0f, 1f)
            attackFrame = (attackProgress * attackTotalFrames).toInt().coerceIn(0, attackTotalFrames - 1)

            // Hitbox is strictly activated at the peak of the swinging animation frame (attackPeakFrame)
            if (isAttackHitboxActive) {
                updateAttackHitbox()
            } else {
                attackHitbox.set(0f, 0f, 0f, 0f)
            }

            if (attackTimer <= 0f) {
                // Return to normal
                attackFrame = 0
                attackHitbox.set(0f, 0f, 0f, 0f)
                if (isGrounded) {
                    state = if (abs(vel.x) > 10f) PlayerState.RUN else PlayerState.IDLE
                } else {
                    state = if (vel.y < 0f) PlayerState.JUMP else PlayerState.FALL
                }
            }
        } else if (state == PlayerState.HURT) {
            if (stateTimer > 0.35f) {
                state = if (isGrounded) PlayerState.IDLE else PlayerState.FALL
            }
        } else if (state != PlayerState.DEAD) {
            // Update movement state
            if (isClimbing) {
                state = PlayerState.CLIMB_LADDER
            } else if (isGrounded) {
                state = if (abs(vel.x) > 15f) PlayerState.RUN else PlayerState.IDLE
            } else {
                state = if (vel.y < 0f) PlayerState.JUMP else PlayerState.FALL
            }
        }

        updateBounds()
    }

    fun updateBounds() {
        bounds.set(pos.x, pos.y, pos.x + width, pos.y + height)
    }

    private fun updateAttackHitbox() {
        val reach = 40f
        val topOffset = 6f
        val attackHeight = 36f
        if (facingRight) {
            attackHitbox.set(pos.x + width * 0.5f, pos.y + topOffset, pos.x + width + reach, pos.y + topOffset + attackHeight)
        } else {
            attackHitbox.set(pos.x - reach, pos.y + topOffset, pos.x + width * 0.5f, pos.y + topOffset + attackHeight)
        }
    }

    fun isAttacking(): Boolean {
        return state == PlayerState.ATTACK_1 || state == PlayerState.ATTACK_2 || state == PlayerState.ATTACK_AIR
    }

    fun attack(): Boolean = triggerAttack()

    fun triggerAttack(): Boolean {
        if (state == PlayerState.DEAD || state == PlayerState.HURT || isClimbing) return false
        if (isAttacking() && attackTimer > 0.1f) return false

        hasHitTargetThisSwing = false
        attackTimer = attackDuration
        stateTimer = 0f

        if (!isGrounded) {
            state = PlayerState.ATTACK_AIR
        } else {
            comboStep = if (comboStep == 1) 2 else 1
            comboResetTimer = 0.55f
            state = if (comboStep == 2) PlayerState.ATTACK_2 else PlayerState.ATTACK_1
        }

        updateAttackHitbox()
        return true
    }

    fun jump(inputY: Float = 0f): Boolean = triggerJump(inputY)

    fun triggerJump(inputY: Float = 0f): Boolean {
        if (state == PlayerState.DEAD || state == PlayerState.HURT) return false

        // If on ladder, dismount
        if (isClimbing) {
            isClimbing = false
            vel.y = jumpImpulse * 0.85f
            state = PlayerState.JUMP
            return true
        }

        // Drop-through one-way platform if pressing down + jump
        if (inputY > 0.35f && isGrounded) {
            dropThroughTimer = 0.3f
            isGrounded = false
            pos.y += 4f // small nudge downward past the platform threshold
            return false
        }

        if (isGrounded || coyoteTimer > 0f) {
            vel.y = jumpImpulse
            isGrounded = false
            coyoteTimer = 0f
            jumpBufferTimer = 0f
            state = PlayerState.JUMP
            return true
        } else {
            // Buffer jump for when landing soon
            jumpBufferTimer = 0.16f
            return false
        }
    }

    /**
     * Variable jump height: cut upward velocity when jump button is released early.
     */
    fun onJumpCut() {
        if (vel.y < -120f) {
            vel.y *= 0.5f
        }
    }

    fun takeDamage(amount: Int, knockbackX: Float): Boolean {
        if (isInvulnerable || state == PlayerState.DEAD) return false

        health -= amount
        invulnerableTimer = 1.2f
        isClimbing = false
        stateTimer = 0f

        if (health <= 0) {
            health = 0
            state = PlayerState.DEAD
            vel.set(0f, -150f)
        } else {
            state = PlayerState.HURT
            vel.set(knockbackX, -220f)
            isGrounded = false
        }
        return true
    }

    fun heal(amount: Int) {
        health = (health + amount).coerceAtMost(maxHealth)
    }

    fun respawn() = respawnAtCheckpoint()

    fun respawnAtCheckpoint() {
        pos.set(respawnPos)
        vel.set(0f, 0f)
        health = maxHealth
        state = PlayerState.IDLE
        stateTimer = 0f
        invulnerableTimer = 1.5f
        isGrounded = false
        isClimbing = false
        statusEffects.clearAll()
        updateBounds()
    }

    fun setCheckpoint(x: Float, y: Float) {
        respawnPos.set(x, y)
    }
}
