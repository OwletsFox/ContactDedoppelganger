package com.example.contactdedoppelganger.data.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import com.example.contactdedoppelganger.IContactService
import com.example.contactdedoppelganger.data.dto.ContactParcelable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Реализация сервиса IContactService */
@AndroidEntryPoint
class ContactService : Service() {

    /** Инъекция contentResolver */
    @Inject
    lateinit var resolver: ContentResolver

    private val binder = object : IContactService.Stub() {

        override fun getAllContacts(): MutableList<ContactParcelable> {
            val raw = loadRawContacts()
            return raw.map { (id, name, phones) ->
                ContactParcelable(id, name, phones)
            }.toMutableList()
        }

        override fun removeDuplicateContacts(): Int {
            Log.d("ContactService", "removeDuplicateContacts called")
            val raw = loadRawContacts()
            val groups = raw.groupBy { (_, name, phones) ->
                "$name|${phones.sorted().joinToString()}"
            }.filter { it.value.size > 1 }

            var deletedCount = 0
            groups.values.forEach { dupList ->
                // оставляем первый, удаляем остальные
                dupList.drop(1).forEach { (id, _, _) ->
                    deletedCount += deleteContactById(id)
                }
            }
            return deletedCount
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    /**
     * Уменьшение комплексности за счёт приватных функций
     */

    private fun loadRawContacts(): List<Triple<String, String, List<String>>> {
        val result = mutableListOf<Triple<String, String, List<String>>>()

        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
            ), null, null, null
        ) ?: return emptyList()

        cursor.use { c ->
            val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

            while (c.moveToNext()) {
                val id = c.getString(idIndex)
                val name = c.getString(nameIndex) ?: ""
                val phones = loadPhonesForContact(id)
                result += Triple(id, name, phones)
            }
        }

        return result
    }

    private fun loadPhonesForContact(contactId: String): List<String> {
        val phones = mutableListOf<String>()
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )?.use { pc ->
            val numIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (pc.moveToNext()) {
                pc.getString(numIndex)?.let { phones += it }
            }
        }
        return phones
    }

    private fun deleteContactById(id: String): Int {
        val uri = ContactsContract.Contacts.CONTENT_URI
            .buildUpon()
            .appendPath(id)
            .build()
        val rows = resolver.delete(uri, null, null)
        if (rows > 0) {
            Log.d("ContactService", "Deleted contact ID=\$id, rowsAffected=\$rows")
        }
        return rows
    }

}
