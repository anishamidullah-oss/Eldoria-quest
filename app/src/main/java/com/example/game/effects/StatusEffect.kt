package com.example.game.effects

import androidx.compose.ui.graphics.Color

enum class StatusEffectType(
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val isDebuff: Boolean
) {
    POISON(
        displayName = "Poison",
        description = "Taking toxic damage over time. Movement speed slowed by 15%.",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF1B5E20),
        isDebuff = true
    ),
    BURNING(
        displayName = "Burning",
        description = "Engulfed in flames! Taking fast fire damage over time.",
        primaryColor = Color(0xFFFF3D00),
        secondaryColor = Color(0xFFFFD600),
        isDebuff = true
    ),
    SPEED_BUFF(
        displayName = "Swift Stride",
        description = "Agile wind momentum! Movement speed increased by +45%.",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF76FF03),
        isDebuff = false
    ),
    REGENERATION(
        displayName = "Regeneration",
        description = "Restoring health continuously over time.",
        primaryColor = Color(0xFF69F0AE),
        secondaryColor = Color(0xFF00B0FF),
        isDebuff = false
    ),
    FROST_CHILL(
        displayName = "Frost Chill",
        description = "Chilled to the bone. Movement speed slowed by 35%.",
        primaryColor = Color(0xFF80D8FF),
        secondaryColor = Color(0xFF0288D1),
        isDebuff = true
    ),
    STRENGTH_BUFF(
        displayName = "Berserk Might",
        description = "Ferocious melee power! Sword attack damage increased by +50%.",
        primaryColor = Color(0xFFFF1744),
        secondaryColor = Color(0xFFFF8A80),
        isDebuff = false
    )
}

data class StatusEffect(
    val type: StatusEffectType,
    var duration: Float,
    val maxDuration: Float = duration,
    val tickInterval: Float = 1.0f,
    val tickValue: Int = 3,
    val speedMultiplier: Float = 1.0f,
    val damageMultiplier: Float = 1.0f
) {
    var tickTimer: Float = 0f

    val progress: Float
        get() = (duration / maxDuration).coerceIn(0f, 1f)

    val progressPercent: Float
        get() = progress

    val durationSeconds: Float
        get() = duration

    val isExpired: Boolean
        get() = duration <= 0f

    companion object {
        fun poison(
            duration: Float = 6.0f,
            tickInterval: Float = 1.0f,
            tickDamage: Int = 3,
            speedMultiplier: Float = 0.85f
        ): StatusEffect = StatusEffect(
            type = StatusEffectType.POISON,
            duration = duration,
            maxDuration = duration,
            tickInterval = tickInterval,
            tickValue = tickDamage,
            speedMultiplier = speedMultiplier,
            damageMultiplier = 1.0f
        )

        fun burning(
            duration: Float = 5.0f,
            tickInterval: Float = 0.65f,
            tickDamage: Int = 4,
            speedMultiplier: Float = 1.0f
        ): StatusEffect = StatusEffect(
            type = StatusEffectType.BURNING,
            duration = duration,
            maxDuration = duration,
            tickInterval = tickInterval,
            tickValue = tickDamage,
            speedMultiplier = speedMultiplier,
            damageMultiplier = 1.0f
        )

        fun speedBuff(
            duration: Float = 8.0f,
            speedMultiplier: Float = 1.45f
        ): StatusEffect = StatusEffect(
            type = StatusEffectType.SPEED_BUFF,
            duration = duration,
            maxDuration = duration,
            tickInterval = 999f,
            tickValue = 0,
            speedMultiplier = speedMultiplier,
            damageMultiplier = 1.0f
        )

        fun regeneration(
            duration: Float = 6.0f,
            tickInterval: Float = 1.2f,
            healPerTick: Int = 4
        ): StatusEffect = StatusEffect(
            type = StatusEffectType.REGENERATION,
            duration = duration,
            maxDuration = duration,
            tickInterval = tickInterval,
            tickValue = healPerTick,
            speedMultiplier = 1.0f,
            damageMultiplier = 1.0f
        )

        fun frostChill(
            duration: Float = 4.5f,
            speedMultiplier: Float = 0.65f
        ): StatusEffect = StatusEffect(
            type = StatusEffectType.FROST_CHILL,
            duration = duration,
            maxDuration = duration,
            tickInterval = 999f,
            tickValue = 0,
            speedMultiplier = speedMultiplier,
            damageMultiplier = 1.0f
        )

        fun strengthBuff(
            duration: Float = 7.0f,
            damageMultiplier: Float = 1.5f
        ): StatusEffect = StatusEffect(
            type = StatusEffectType.STRENGTH_BUFF,
            duration = duration,
            maxDuration = duration,
            tickInterval = 999f,
            tickValue = 0,
            speedMultiplier = 1.0f,
            damageMultiplier = damageMultiplier
        )
    }
}
