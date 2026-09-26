package com.example.game.entities

import androidx.compose.ui.graphics.Color
import com.example.game.model.Vector2D

enum class ParticleType {
    DUST,
    SWORD_SPARK,
    SLASH_SPARK,
    COIN_SPARKLE,
    BLOOD_SPARK,
    BLOOD_SPLOTCH,
    DEATH_POOF,
    CRYSTAL_GLOW,
    FIRE_EMBER,
    DAMAGE_TEXT,
    POISON_BUBBLE,
    SPEED_STREAK,
    HEAL_SPARKLE,
    FROST_SPARK
}

data class Particle(
    val pos: Vector2D,
    val velocity: Vector2D,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val size: Float,
    val type: ParticleType,
    val text: String = "",
    val isCritical: Boolean = false
) {
    val progress: Float get() = (1f - (life / maxLife)).coerceIn(0f, 1f)
    val alpha: Float get() = (life / maxLife).coerceIn(0f, 1f)

    fun update(dt: Float): Boolean {
        pos.x += velocity.x * dt
        pos.y += velocity.y * dt

        if (type == ParticleType.DAMAGE_TEXT) {
            velocity.y += 40f * dt // gentle deceleration
        } else if (type == ParticleType.DUST || type == ParticleType.SWORD_SPARK || type == ParticleType.BLOOD_SPARK) {
            velocity.y += 200f * dt // gravity
            velocity.x *= 0.95f
        } else if (type == ParticleType.FIRE_EMBER || type == ParticleType.POISON_BUBBLE) {
            velocity.y -= 40f * dt // rise upward
            velocity.x *= 0.92f
        } else if (type == ParticleType.HEAL_SPARKLE) {
            velocity.y -= 50f * dt // ascend smoothly
        } else if (type == ParticleType.FROST_SPARK) {
            velocity.y += 35f * dt // gentle snow fall
            velocity.x *= 0.90f
        } else if (type == ParticleType.SPEED_STREAK) {
            velocity.x *= 0.85f
            velocity.y *= 0.85f
        }

        life -= dt
        return life > 0f
    }
}
