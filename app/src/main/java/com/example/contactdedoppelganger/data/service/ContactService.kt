package com.example.contactdedoppelganger.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import com.example.contactdedoppelganger.IContactService
import com.example.contactdedoppelganger.data.dto.ContactParcelable

/** Реализация сервиса IContactService */
class ContactService : Service() {

    private val binder = object : IContactService.Stub() {
        override fun getAllContacts(): MutableList<ContactParcelable> {
            val contentResolver = applicationContext.contentResolver
            val result = mutableListOf<ContactParcelable>()

            /** Запрос курсора всех контактов (ID + DISPLAY_NAME) */
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                ),
                null, null, null
            ) ?: return result

            cursor.use { c ->
                val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                while (c.moveToNext()) {
                    val id = c.getString(idIndex)
                    val name = c.getString(nameIndex) ?: ""

                    /** Запрос номеров каждого контакта */
                    val phones = mutableListOf<String>()
                    val pCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )

                    pCursor?.use { pc ->
                        val numIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        while (pc.moveToNext()) {
                            pc.getString(numIndex)?.let { phones += it }
                        }
                    }

                    result += ContactParcelable(id, name, phones)
                }
            }

            return result
        }
    }

    override fun onBind(intent: Intent): IBinder = binder
}

