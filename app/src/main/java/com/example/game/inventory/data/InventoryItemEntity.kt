package com.example.game.inventory.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemCategory {
    ALL,
    CONSUMABLE,
    KEY_ITEM,
    MATERIAL,
    RELIC,
    TREASURE,
    EQUIPMENT
}

enum class ItemRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

@Entity(tableName = "player_inventory")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemKey: String,
    val name: String,
    val category: String, // e.g. "CONSUMABLE", "KEY_ITEM", "MATERIAL", "RELIC", "TREASURE", "EQUIPMENT"
    val description: String,
    val quantity: Int = 1,
    val rarity: String = "COMMON", // "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY"
    val value: Int = 10,
    val isUsable: Boolean = false,
    val iconType: String = "item",
    val acquiredTimestamp: Long = System.currentTimeMillis()
)
