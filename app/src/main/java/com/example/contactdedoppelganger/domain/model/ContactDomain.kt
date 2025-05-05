package com.example.contactdedoppelganger.domain.model

/**
 * Доменная модель контакта (бизнес логика и UI).
 *
 * @param id уникальный идентификатор
 * @param name имя-фамилия
 * @param phoneNumbers номера контакта
 */
data class ContactDomain(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>
)