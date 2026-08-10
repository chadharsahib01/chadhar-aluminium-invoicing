package com.example.data.repository

import com.example.data.dao.ComboPackageDao
import com.example.data.entity.ComboPackageEntity
import kotlinx.coroutines.flow.Flow

class ComboPackageRepository(private val comboPackageDao: ComboPackageDao) {
    val allPackages: Flow<List<ComboPackageEntity>> = comboPackageDao.getAllPackages()

    suspend fun savePackage(name: String, itemsJson: String): Long {
        return comboPackageDao.insertPackage(ComboPackageEntity(name = name, itemsJson = itemsJson))
    }

    suspend fun deletePackageById(id: Long) {
        comboPackageDao.deletePackageById(id)
    }

    suspend fun renamePackage(id: Long, newName: String) {
        comboPackageDao.renamePackage(id, newName)
    }
}
