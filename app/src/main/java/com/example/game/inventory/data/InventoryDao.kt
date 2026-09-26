package com.example.game.inventory.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM player_inventory ORDER BY acquiredTimestamp DESC")
    fun getAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM player_inventory WHERE category = :category ORDER BY acquiredTimestamp DESC")
    fun getItemsByCategory(category: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM player_inventory WHERE itemKey = :key LIMIT 1")
    suspend fun getItemByKey(key: String): InventoryItemEntity?

    @Query("SELECT * FROM player_inventory WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Delete
    suspend fun deleteItem(item: InventoryItemEntity)

    @Query("DELETE FROM player_inventory WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM player_inventory WHERE itemKey = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM player_inventory")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM player_inventory")
    fun getItemCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM player_inventory")
    fun getTotalItemQuantity(): Flow<Int?>
}
