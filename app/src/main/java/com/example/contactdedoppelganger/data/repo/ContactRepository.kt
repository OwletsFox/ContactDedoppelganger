package com.example.contactdedoppelganger.data.repo

import com.example.contactdedoppelganger.domain.model.ContactDomain

/**
 * Интерфейс репозитория для работы с контактами.
 */
interface ContactRepository {

    /** Список всех контактов с устройства. */
    suspend fun getAllContacts(): List<ContactDomain>

    /**
     * Удаление дублирующихся контактов.
     * @return Int кол-во удалённых контактов.
     */
    suspend fun removeDuplicateContacts(): Int
}