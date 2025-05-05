package com.example.contactdedoppelganger.data.mapper

import com.example.contactdedoppelganger.data.dto.ContactParcelable
import com.example.contactdedoppelganger.domain.model.ContactDomain

/**
 * Маппер контактов DTO -> Domain
 * Для делегирования слоёв, используется на репозиторном уровне
 *
 * @return ContactDomain
 */
fun ContactParcelable.toDomain(): ContactDomain {
    return ContactDomain(
        id = this.id,
        name = this.name,
        phoneNumbers = this.phoneNumbers
    )
}
