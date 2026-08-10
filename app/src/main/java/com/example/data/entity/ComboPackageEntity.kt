package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "combo_packages")
data class ComboPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val itemsJson: String // Serialized JSON of List<DocumentItemEntity>
)
