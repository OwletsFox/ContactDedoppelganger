package com.example.contactdedoppelganger.ui.model

import com.example.contactdedoppelganger.domain.model.ContactDomain

/**
 * Модель представления списка контактов (UI).
 *
 * @param title Первый символ из имени (?: "#")
 * @param items Список контактов по символу выше
 */
data class ContactSection(
    val title: String,
    val items: List<ContactDomain>
)