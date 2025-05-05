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
    private val latch = CountDownLatch(1)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d("ContactRepo", "Service connected: $name")
            service = IContactService.Stub.asInterface(binder)
            latch.countDown()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
        }
    }

    private fun ensureBound() {
        if (service != null) return
        val intent = Intent(context, ContactService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        latch.await(5, TimeUnit.SECONDS)
    }

    override suspend fun getAllContacts(): List<ContactDomain> = withContext(ioDispatcher) {
        try {
            ensureBound()
            val contactDtoList = service?.getAllContacts() ?: emptyList()
            contactDtoList.map { it.toDomain() }
        } finally {
            /** Отписка в целях экономии */
            context.unbindService(connection)
        }
    }
}
