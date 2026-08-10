package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.DocumentEntity
import com.example.data.entity.DocumentItemEntity
import com.example.data.entity.DocumentWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Transaction
    @Query("SELECT * FROM documents ORDER BY id DESC")
    fun getAllDocumentsWithItems(): Flow<List<DocumentWithItems>>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentWithItemsById(id: Long): DocumentWithItems?

    @Transaction
    @Query("SELECT * FROM documents WHERE clientName LIKE '%' || :query || '%' OR documentNumber LIKE '%' || :query || '%' OR date LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchDocuments(query: String): Flow<List<DocumentWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentItems(items: List<DocumentItemEntity>)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("DELETE FROM document_items WHERE documentId = :documentId")
    suspend fun deleteItemsForDocument(documentId: Long)

    @Transaction
    suspend fun saveDocumentWithItems(document: DocumentEntity, items: List<DocumentItemEntity>): Long {
        val docId = insertDocument(document)
        deleteItemsForDocument(docId)
        val itemsWithDocId = items.map { it.copy(documentId = docId) }
        insertDocumentItems(itemsWithDocId)
        return docId
    }

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("SELECT MAX(id) FROM documents")
    suspend fun getMaxDocumentId(): Long?
}
