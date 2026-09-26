package com.example.game.inventory.data

import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {
    val allItems: Flow<List<InventoryItemEntity>> = inventoryDao.getAllItems()
    val itemCount: Flow<Int> = inventoryDao.getItemCount()
    val totalQuantity: Flow<Int?> = inventoryDao.getTotalItemQuantity()

    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>> {
        return if (category == "ALL" || category.isEmpty()) {
            inventoryDao.getAllItems()
        } else {
            inventoryDao.getItemsByCategory(category)
        }
    }

    suspend fun getItemByKey(key: String): InventoryItemEntity? {
        return inventoryDao.getItemByKey(key)
    }

    suspend fun getItemById(id: Long): InventoryItemEntity? {
        return inventoryDao.getItemById(id)
    }

    suspend fun addItem(
        itemKey: String,
        name: String,
        category: String,
        description: String,
        quantity: Int = 1,
        rarity: String = "COMMON",
        value: Int = 10,
        isUsable: Boolean = false,
        iconType: String = itemKey
    ): Long {
        val existing = inventoryDao.getItemByKey(itemKey)
        return if (existing != null) {
            val updated = existing.copy(
                quantity = existing.quantity + quantity,
                acquiredTimestamp = System.currentTimeMillis()
            )
            inventoryDao.updateItem(updated)
            existing.id
        } else {
            val newItem = InventoryItemEntity(
                itemKey = itemKey,
                name = name,
                category = category,
                description = description,
                quantity = quantity,
                rarity = rarity,
                value = value,
                isUsable = isUsable,
                iconType = iconType,
                acquiredTimestamp = System.currentTimeMillis()
            )
            inventoryDao.insertItem(newItem)
        }
    }

    suspend fun consumeOrUseItem(itemKey: String, amount: Int = 1): Boolean {
        val existing = inventoryDao.getItemByKey(itemKey) ?: return false
        return if (existing.quantity > amount) {
            val updated = existing.copy(quantity = existing.quantity - amount)
            inventoryDao.updateItem(updated)
            true
        } else {
            inventoryDao.deleteById(existing.id)
            true
        }
    }

    suspend fun deleteItem(id: Long) {
        inventoryDao.deleteById(id)
    }

    suspend fun deleteByKey(key: String) {
        inventoryDao.deleteByKey(key)
    }

    suspend fun clearInventory() {
        inventoryDao.clearAll()
    }

    suspend fun seedStartingInventoryIfEmpty() {
        val existingHealth = inventoryDao.getItemByKey("potion_health")
        if (existingHealth == null) {
            addItem(
                itemKey = "potion_health",
                name = "Life Elixir",
                category = "CONSUMABLE",
                description = "Standard apothecary draught brewing herbal elderberries and spring water. Restores 45 HP.",
                quantity = 2,
                rarity = "COMMON",
                value = 25,
                isUsable = true,
                iconType = "potion_health"
            )
            addItem(
                itemKey = "potion_swiftness",
                name = "Swiftness Draught",
                category = "CONSUMABLE",
                description = "Distilled feather-light essence accelerating movement speed and agility by +45% for 9 seconds.",
                quantity = 2,
                rarity = "UNCOMMON",
                value = 40,
                isUsable = true,
                iconType = "potion_speed"
            )
            addItem(
                itemKey = "sword_broadsword",
                name = "Steel Broadsword",
                category = "EQUIPMENT",
                description = "Reliable dual-edged forged blade carried by the frontier scouts of Aether.",
                quantity = 1,
                rarity = "COMMON",
                value = 60,
                isUsable = false,
                iconType = "sword"
            )
            addItem(
                itemKey = "armor_adventurer",
                name = "Adventurer Tunic",
                category = "EQUIPMENT",
                description = "Reinforced leather tunic woven with enchanted protective fibers.",
                quantity = 1,
                rarity = "COMMON",
                value = 50,
                isUsable = false,
                iconType = "armor"
            )
        }
    }
}
