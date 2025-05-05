package com.example.contactdedoppelganger.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactdedoppelganger.databinding.ActivityMainBinding
import com.example.contactdedoppelganger.ui.adapter.ContactAdapter
import com.example.contactdedoppelganger.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CONTACT_PERMS = 1001
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val adapter = ContactAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = adapter

        binding.btnDelDuplicates.setOnClickListener {
            viewModel.onDeleteDuplicatesClicked()
        }
        /** Подписка на viewmodel */
        observeViewModel()
        /** Проверка прав ?: загрузка контактов*/
        checkPermissionsAndLoad()
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(this) { list ->
            adapter.update(list)
            binding.btnDelDuplicates.isEnabled = list.isNotEmpty()
        }
        viewModel.isLoading.observe(this) { loading ->
            binding.rvContacts.alpha = if (loading) 0.5f else 1f
        }
        viewModel.error.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionsAndLoad() {
        val readGranted =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val writeGranted =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (readGranted && writeGranted) {
            viewModel.loadContacts()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                REQUEST_CONTACT_PERMS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CONTACT_PERMS) {
            val granted = grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (granted) {
                viewModel.loadContacts()
            } else {
                Toast.makeText(
                    this,
                    "Без разрешения на доступ к контактам приложение не сможет работать",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
