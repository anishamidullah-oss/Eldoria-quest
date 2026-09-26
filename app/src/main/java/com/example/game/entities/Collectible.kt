package com.example.game.entities

import com.example.game.model.RectF2D
import com.example.game.model.Vector2D
import kotlin.math.sin

enum class CollectibleType {
    COIN,
    MANA_CRYSTAL,
    HEALTH_POTION,
    CHEST,
    SECRET_CHEST,
    CHECKPOINT,
    ANCIENT_KEY,
    HEART_FRAGMENT,
    LORE_TABLET,
    BRASS_COMPASS,
    MAP_PARCHMENT,
    GUARD_BADGE,
    RUNIC_ORE,
    RELIC_CHALICE,
    VICTORY_PORTAL
}

data class Collectible(
    val id: String,
    val pos: Vector2D,
    val type: CollectibleType,
    val value: Int = 1,
    var isCollected: Boolean = false,
    var isActivated: Boolean = false,
    var width: Float = 24f,
    var height: Float = 24f,
    var basePosY: Float = pos.y,
    var animTimer: Float = (Math.random() * 5.0).toFloat(),
    var isOpened: Boolean = false,
    var extraData: String = ""
) {
    val bounds: RectF2D = RectF2D(pos.x, pos.y, pos.x + width, pos.y + height)

    init {
        when (type) {
            CollectibleType.COIN -> {
                width = 20f
                height = 20f
            }
            CollectibleType.MANA_CRYSTAL -> {
                width = 24f
                height = 28f
            }
            CollectibleType.HEALTH_POTION -> {
                width = 22f
                height = 26f
            }
            CollectibleType.CHEST, CollectibleType.SECRET_CHEST -> {
                width = 36f
                height = 30f
            }
            CollectibleType.CHECKPOINT -> {
                width = 32f
                height = 56f
            }
            CollectibleType.ANCIENT_KEY -> {
                width = 24f
                height = 32f
            }
            CollectibleType.HEART_FRAGMENT -> {
                width = 32f
                height = 32f
            }
            CollectibleType.LORE_TABLET -> {
                width = 28f
                height = 36f
            }
            CollectibleType.BRASS_COMPASS, CollectibleType.GUARD_BADGE, CollectibleType.RUNIC_ORE -> {
                width = 24f
                height = 24f
            }
            CollectibleType.MAP_PARCHMENT, CollectibleType.RELIC_CHALICE -> {
                width = 28f
                height = 28f
            }
            CollectibleType.VICTORY_PORTAL -> {
                width = 48f
                height = 72f
            }
        }
        updateBounds()
    }

    fun update(dt: Float) {
        animTimer += dt
        if (type == CollectibleType.COIN || type == CollectibleType.MANA_CRYSTAL ||
            type == CollectibleType.HEALTH_POTION || type == CollectibleType.ANCIENT_KEY ||
            type == CollectibleType.HEART_FRAGMENT || type == CollectibleType.BRASS_COMPASS ||
            type == CollectibleType.MAP_PARCHMENT || type == CollectibleType.GUARD_BADGE ||
            type == CollectibleType.RUNIC_ORE || type == CollectibleType.RELIC_CHALICE) {
            // Bobbing floating animation
            pos.y = basePosY + sin(animTimer * 4f) * 4f
            updateBounds()
        }
    }

    fun updateBounds() {
        bounds.set(pos.x, pos.y, pos.x + width, pos.y + height)
    }
}
