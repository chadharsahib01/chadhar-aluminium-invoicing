package com.example.data.repository

import com.example.data.dao.ClientDao
import com.example.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {
    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()

    fun searchClients(query: String): Flow<List<ClientEntity>> {
        return clientDao.searchClients(query)
    }

    suspend fun getClientByName(name: String): ClientEntity? {
        return clientDao.getClientByName(name)
    }

    suspend fun saveOrUpdateClient(name: String, phone: String, address: String) {
        if (name.isBlank()) return
        val existing = clientDao.getClientByName(name)
        if (existing == null) {
            clientDao.insertClient(ClientEntity(name = name.trim(), phone = phone.trim(), address = address.trim()))
        } else {
            val updated = existing.copy(
                phone = if (phone.isNotBlank()) phone.trim() else existing.phone,
                address = if (address.isNotBlank()) address.trim() else existing.address
            )
            clientDao.updateClient(updated)
        }
    }

    suspend fun deleteClientById(id: Long) {
        clientDao.deleteClientById(id)
    }
}
