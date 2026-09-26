package com.example.game.effects

import kotlin.math.max

class StatusEffectManager {

    private val activeEffectsMap = mutableMapOf<StatusEffectType, StatusEffect>()

    val activeEffects: List<StatusEffect>
        get() = activeEffectsMap.values.toList()

    val count: Int
        get() = activeEffectsMap.size

    val hasAnyDebuff: Boolean
        get() = activeEffectsMap.values.any { it.type.isDebuff }

    val hasAnyBuff: Boolean
        get() = activeEffectsMap.values.any { !it.type.isDebuff }

    val speedMultiplier: Float
        get() {
            var mult = 1.0f
            for (effect in activeEffectsMap.values) {
                mult *= effect.speedMultiplier
            }
            return mult.coerceIn(0.2f, 2.5f)
        }

    val damageMultiplier: Float
        get() {
            var mult = 1.0f
            for (effect in activeEffectsMap.values) {
                mult *= effect.damageMultiplier
            }
            return mult.coerceIn(0.5f, 3.0f)
        }

    fun hasEffect(type: StatusEffectType): Boolean {
        return activeEffectsMap.containsKey(type)
    }

    fun getEffect(type: StatusEffectType): StatusEffect? {
        return activeEffectsMap[type]
    }

    fun applyEffect(effect: StatusEffect): Boolean {
        val existing = activeEffectsMap[effect.type]
        if (existing != null) {
            // Refresh duration to the greater of the remaining duration or the new duration
            existing.duration = max(existing.duration, effect.duration)
            existing.tickTimer = 0f
            return false // Refreshed
        } else {
            activeEffectsMap[effect.type] = effect
            return true // Newly applied
        }
    }

    fun removeEffect(type: StatusEffectType): Boolean {
        return activeEffectsMap.remove(type) != null
    }

    fun clearAll() {
        activeEffectsMap.clear()
    }

    fun clear() = clearAll()

    fun clearDebuffs() {
        val debuffs = activeEffectsMap.keys.filter { it.isDebuff }
        for (d in debuffs) {
            activeEffectsMap.remove(d)
        }
    }

    fun update(
        dt: Float,
        onTick: (StatusEffect, Int) -> Unit = { _, _ -> },
        onExpired: (StatusEffectType) -> Unit = {}
    ) {
        if (activeEffectsMap.isEmpty()) return

        val iterator = activeEffectsMap.values.iterator()
        while (iterator.hasNext()) {
            val effect = iterator.next()
            effect.duration -= dt

            if (effect.tickInterval < 900f) {
                effect.tickTimer += dt
                if (effect.tickTimer >= effect.tickInterval) {
                    effect.tickTimer -= effect.tickInterval
                    onTick(effect, effect.tickValue)
                }
            }

            if (effect.isExpired) {
                iterator.remove()
                onExpired(effect.type)
            }
        }
    }
}
