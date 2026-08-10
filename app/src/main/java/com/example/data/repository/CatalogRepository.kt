package com.example.data.repository

import com.example.data.dao.CatalogDao
import com.example.data.entity.CatalogItemEntity
import kotlinx.coroutines.flow.Flow

class CatalogRepository(private val catalogDao: CatalogDao) {
    val allCatalogItems: Flow<List<CatalogItemEntity>> = catalogDao.getAllCatalogItems()

    fun searchCatalogItems(query: String): Flow<List<CatalogItemEntity>> {
        return catalogDao.searchCatalogItems(query)
    }

    suspend fun insertCatalogItem(item: CatalogItemEntity): Long {
        return catalogDao.insertCatalogItem(item)
    }

    suspend fun updateCatalogItem(item: CatalogItemEntity) {
        catalogDao.updateCatalogItem(item)
    }

    suspend fun deleteCatalogItem(item: CatalogItemEntity) {
        catalogDao.deleteCatalogItem(item)
    }

    suspend fun checkAndSeedCatalog() {
        if (catalogDao.getCatalogCount() == 0) {
            val defaultItems = listOf(
                CatalogItemEntity(name = "ACP Sheet (Aluminum Composite Panel) — 3mm", defaultUnit = "sq.ft", defaultRate = 350.0),
                CatalogItemEntity(name = "ACP Sheet — 4mm", defaultUnit = "sq.ft", defaultRate = 450.0),
                CatalogItemEntity(name = "ACP Sheet — 6mm", defaultUnit = "sq.ft", defaultRate = 650.0),
                CatalogItemEntity(name = "Aluminum Frame / Section (L-shape)", defaultUnit = "running ft", defaultRate = 180.0),
                CatalogItemEntity(name = "Aluminum Frame / Section (Box type)", defaultUnit = "running ft", defaultRate = 250.0),
                CatalogItemEntity(name = "Aluminum Angle", defaultUnit = "running ft", defaultRate = 150.0),
                CatalogItemEntity(name = "Aluminum Channel", defaultUnit = "running ft", defaultRate = 160.0),
                CatalogItemEntity(name = "Cladding Brackets/Clamps", defaultUnit = "piece", defaultRate = 45.0),
                CatalogItemEntity(name = "Self-Tapping Screws", defaultUnit = "box", defaultRate = 400.0),
                CatalogItemEntity(name = "Rivets", defaultUnit = "box", defaultRate = 350.0),
                CatalogItemEntity(name = "Silicone Sealant", defaultUnit = "piece", defaultRate = 650.0),
                CatalogItemEntity(name = "Weather Sealant", defaultUnit = "piece", defaultRate = 750.0),
                CatalogItemEntity(name = "Double-Sided Foam Tape", defaultUnit = "roll", defaultRate = 250.0),
                CatalogItemEntity(name = "Rockwool / Thermal Insulation Sheet", defaultUnit = "sq.ft", defaultRate = 120.0),
                CatalogItemEntity(name = "Glass Panel (if composite work)", defaultUnit = "sq.ft", defaultRate = 500.0),
                CatalogItemEntity(name = "Primer/Paint", defaultUnit = "liter", defaultRate = 850.0),
                CatalogItemEntity(name = "Fixing Labor Charges", defaultUnit = "sq.ft", defaultRate = 80.0),
                CatalogItemEntity(name = "Scaffolding Charges", defaultUnit = "lump sum", defaultRate = 5000.0),
                CatalogItemEntity(name = "Transportation Charges", defaultUnit = "lump sum", defaultRate = 3000.0),
                CatalogItemEntity(name = "Site Measurement/Survey Charges", defaultUnit = "lump sum", defaultRate = 1500.0),
                CatalogItemEntity(name = "Design/Consultancy Charges", defaultUnit = "lump sum", defaultRate = 2000.0)
            )
            catalogDao.insertAllCatalogItems(defaultItems)
        }
    }
}
