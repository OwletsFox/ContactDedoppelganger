package com.example.contactdedoppelganger.domain.usecase

import com.example.contactdedoppelganger.domain.model.ContactDomain
import com.example.contactdedoppelganger.data.repo.ContactRepository

/**
 * Use Case для получения контактов.
 */
class GetContactsUseCase(
    private val repository: ContactRepository
) {
    /**
     * Возвращает список контактов.
     */
    suspend operator fun invoke(): List<ContactDomain> =
        repository.getAllContacts()
}