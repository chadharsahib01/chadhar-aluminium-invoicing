package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CatalogItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalog_items ORDER BY name ASC")
    fun getAllCatalogItems(): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCatalogItems(query: String): Flow<List<CatalogItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItem(item: CatalogItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCatalogItems(items: List<CatalogItemEntity>)

    @Update
    suspend fun updateCatalogItem(item: CatalogItemEntity)

    @Delete
    suspend fun deleteCatalogItem(item: CatalogItemEntity)

    @Query("SELECT COUNT(*) FROM catalog_items")
    suspend fun getCatalogCount(): Int
}
