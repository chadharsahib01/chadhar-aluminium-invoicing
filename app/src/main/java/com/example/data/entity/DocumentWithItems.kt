package com.example.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DocumentWithItems(
    @Embedded val document: DocumentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "documentId"
    )
    val items: List<DocumentItemEntity>
)
