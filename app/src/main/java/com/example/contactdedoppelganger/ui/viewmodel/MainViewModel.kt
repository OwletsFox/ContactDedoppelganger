package com.example.contactdedoppelganger.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactdedoppelganger.domain.model.ContactDomain
import com.example.contactdedoppelganger.domain.usecase.GetContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase
) : ViewModel() {

    private val _contacts = MutableLiveData<List<ContactDomain>>()
    val contacts: LiveData<List<ContactDomain>> = _contacts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadContacts() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val list = getContactsUseCase()
                _contacts.value = list
            } catch (t: Throwable) {
                _error.value = t.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onDeleteDuplicatesClicked() {
        /**
         * TODO: Реализовать удаление дубликатов
         */
    }
}
