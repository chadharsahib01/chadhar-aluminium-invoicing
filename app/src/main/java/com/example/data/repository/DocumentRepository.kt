package com.example.data.repository

import com.example.data.dao.DocumentDao
import com.example.data.entity.DocumentEntity
import com.example.data.entity.DocumentItemEntity
import com.example.data.entity.DocumentWithItems
import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {
    val allDocuments: Flow<List<DocumentWithItems>> = documentDao.getAllDocumentsWithItems()

    fun searchDocuments(query: String): Flow<List<DocumentWithItems>> {
        return documentDao.searchDocuments(query)
    }

    suspend fun getDocumentWithItemsById(id: Long): DocumentWithItems? {
        return documentDao.getDocumentWithItemsById(id)
    }

    suspend fun saveDocumentWithItems(document: DocumentEntity, items: List<DocumentItemEntity>): Long {
        return documentDao.saveDocumentWithItems(document, items)
    }

    suspend fun updateDocument(document: DocumentEntity) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(document: DocumentEntity) {
        documentDao.deleteDocument(document)
    }

    suspend fun deleteDocumentById(id: Long) {
        documentDao.deleteDocumentById(id)
    }
}
