package com.example.game.inventory

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.game.inventory.data.InventoryDao
import com.example.game.inventory.data.InventoryDatabase
import com.example.game.inventory.data.InventoryItemEntity
import com.example.game.inventory.data.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InventoryDatabaseTest {

    private lateinit var db: InventoryDatabase
    private lateinit var dao: InventoryDao
    private lateinit var repository: InventoryRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.inventoryDao()
        repository = InventoryRepository(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveItem() = runBlocking {
        val item = InventoryItemEntity(
            itemKey = "potion_health",
            name = "Life Elixir",
            category = "CONSUMABLE",
            description = "Restores 45 HP.",
            quantity = 2,
            rarity = "COMMON",
            value = 20,
            isUsable = true
        )
        val id = dao.insertItem(item)
        assertTrue(id > 0)

        val retrieved = dao.getItemByKey("potion_health")
        assertNotNull(retrieved)
        assertEquals("Life Elixir", retrieved?.name)
        assertEquals(2, retrieved?.quantity)
        assertEquals(true, retrieved?.isUsable)
    }

    @Test
    fun testRepositoryAddAndStacking() = runBlocking {
        // Add first potion
        repository.addItem(
            itemKey = "potion_health",
            name = "Life Elixir",
            category = "CONSUMABLE",
            description = "Restores 45 HP.",
            quantity = 2,
            rarity = "COMMON",
            value = 20,
            isUsable = true
        )

        // Add 3 more potions with same key
        repository.addItem(
            itemKey = "potion_health",
            name = "Life Elixir",
            category = "CONSUMABLE",
            description = "Restores 45 HP.",
            quantity = 3,
            rarity = "COMMON",
            value = 20,
            isUsable = true
        )

        val items = repository.allItems.first()
        assertEquals(1, items.size)
        assertEquals(5, items[0].quantity)

        val totalUnits = repository.totalQuantity.first()
        assertEquals(5, totalUnits)
    }

    @Test
    fun testConsumeAndDepleteItem() = runBlocking {
        repository.addItem(
            itemKey = "crystal_mana",
            name = "Aether Crystal",
            category = "MATERIAL",
            description = "Mana crystal.",
            quantity = 3
        )

        // Consume 1
        val used1 = repository.consumeOrUseItem("crystal_mana", 1)
        assertTrue(used1)
        var item = repository.getItemByKey("crystal_mana")
        assertEquals(2, item?.quantity)

        // Consume 2 more (total 3) -> should delete row
        val used2 = repository.consumeOrUseItem("crystal_mana", 2)
        assertTrue(used2)
        item = repository.getItemByKey("crystal_mana")
        assertNull("Item should be deleted once quantity reaches zero", item)

        val all = repository.allItems.first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun testFilterByCategory() = runBlocking {
        repository.addItem(
            itemKey = "potion_health",
            name = "Life Elixir",
            category = "CONSUMABLE",
            description = "Heals 45 HP.",
            quantity = 2
        )
        repository.addItem(
            itemKey = "key_citadel_1",
            name = "Citadel Key #1",
            category = "KEY_ITEM",
            description = "Gate key.",
            quantity = 1
        )
        repository.addItem(
            itemKey = "mat_runic_ore",
            name = "Runic Mithril Ore",
            category = "MATERIAL",
            description = "Forging material.",
            quantity = 3
        )

        val consumables = repository.getItemsByCategory("CONSUMABLE").first()
        assertEquals(1, consumables.size)
        assertEquals("potion_health", consumables[0].itemKey)

        val keyItems = repository.getItemsByCategory("KEY_ITEM").first()
        assertEquals(1, keyItems.size)
        assertEquals("key_citadel_1", keyItems[0].itemKey)

        val all = repository.getItemsByCategory("ALL").first()
        assertEquals(3, all.size)
    }
}
