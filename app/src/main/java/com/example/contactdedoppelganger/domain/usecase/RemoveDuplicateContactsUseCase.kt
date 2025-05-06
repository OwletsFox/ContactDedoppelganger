package com.example.contactdedoppelganger.domain.usecase

import com.example.contactdedoppelganger.data.repo.ContactRepository
import javax.inject.Inject

/**
 * UseCase для удаление дублирующихся контактов
 */
class RemoveDuplicateContactsUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(): Int =
        repository.removeDuplicateContacts()
}