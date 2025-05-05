package com.example.contactdedoppelganger.data.repo

import com.example.contactdedoppelganger.domain.model.ContactDomain

/** Интерфейс репозитория для работы с контактами. */
interface ContactRepository {

    /** Список всех контактов с устройства. */
    suspend fun getAllContacts(): List<ContactDomain>
}