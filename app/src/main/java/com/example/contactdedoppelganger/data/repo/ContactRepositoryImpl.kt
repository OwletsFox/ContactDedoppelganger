package com.example.contactdedoppelganger.data.repo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.contactdedoppelganger.domain.model.ContactDomain
import com.example.contactdedoppelganger.data.mapper.toDomain
import com.example.contactdedoppelganger.data.service.ContactService
import com.example.contactdedoppelganger.IContactService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Реашизация ContactRepository
 */

class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : ContactRepository {

    private var service: IContactService? = null
    private var isBound = false
    private val latch = CountDownLatch(1)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d("ContactRepo", "Service connected: $name")
            service = IContactService.Stub.asInterface(binder)
            isBound = true
            latch.countDown()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            isBound = false
        }
    }

    private fun ensureBound() {
        if (service != null) return
        val intent = Intent(context, ContactService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        latch.await(5, TimeUnit.SECONDS)
    }

    /**
     * Функция-Дженерик, для безопасного вызова методов и устранения дублирования кода.
     */
    private suspend fun <T> callService(
        action: IContactService.() -> T?
    ): T? = withContext(ioDispatcher) {
        try {
            ensureBound()
            service?.action()
        } finally {
            if (isBound) {
                context.unbindService(connection)
                isBound = false
            }
        }
    }

    override suspend fun getAllContacts(): List<ContactDomain> {
        val contactDtoList = callService { getAllContacts() } ?: emptyList()
        return contactDtoList.map { it.toDomain() }
    }

    override suspend fun removeDuplicateContacts(): Int {
        return callService { removeDuplicateContacts() } ?: 0
    }

}
