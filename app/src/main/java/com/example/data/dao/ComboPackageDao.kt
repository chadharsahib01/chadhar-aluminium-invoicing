package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ComboPackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComboPackageDao {
    @Query("SELECT * FROM combo_packages ORDER BY id DESC")
    fun getAllPackages(): Flow<List<ComboPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: ComboPackageEntity): Long

    @Query("DELETE FROM combo_packages WHERE id = :id")
    suspend fun deletePackageById(id: Long)

    @Query("UPDATE combo_packages SET name = :newName WHERE id = :id")
    suspend fun renamePackage(id: Long, newName: String)
}
