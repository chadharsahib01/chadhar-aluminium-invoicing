package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CatalogDao
import com.example.data.dao.ClientDao
import com.example.data.dao.ComboPackageDao
import com.example.data.dao.DocumentDao
import com.example.data.entity.CatalogItemEntity
import com.example.data.entity.ClientEntity
import com.example.data.entity.ComboPackageEntity
import com.example.data.entity.DocumentEntity
import com.example.data.entity.DocumentItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DocumentEntity::class,
        DocumentItemEntity::class,
        CatalogItemEntity::class,
        ClientEntity::class,
        ComboPackageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun catalogDao(): CatalogDao
    abstract fun clientDao(): ClientDao
    abstract fun comboPackageDao(): ComboPackageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chadhar_aluminium_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCatalog(database.catalogDao())
                    }
                }
            }
        }

        private suspend fun populateDefaultCatalog(catalogDao: CatalogDao) {
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
