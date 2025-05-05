package com.example.contactdedoppelganger.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** DTO контакта (сервис)
 * @param id уникальный идентификатор
 * @param name имя-фамилия
 * @param phoneNumbers номера контакта
 * */
@Parcelize
data class ContactParcelable(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>
) : Parcelable
