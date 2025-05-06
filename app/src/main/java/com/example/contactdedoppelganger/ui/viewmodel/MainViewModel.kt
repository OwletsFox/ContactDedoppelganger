package com.example.contactdedoppelganger.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactdedoppelganger.domain.model.ContactDomain
import com.example.contactdedoppelganger.domain.usecase.GetContactsUseCase
import com.example.contactdedoppelganger.domain.usecase.RemoveDuplicateContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val removeDuplicateContactsUseCase: RemoveDuplicateContactsUseCase
) : ViewModel() {

    private val _contacts = MutableLiveData<List<ContactDomain>>()
    val contacts: LiveData<List<ContactDomain>> = _contacts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _removedCount = MutableLiveData<Int?>(null)
    val removedCount: LiveData<Int?> = _removedCount

    /**
     * Функция-Дженерик, чтобы сократить дублирование кода.
     */
    private fun <T> performAction(
        action: suspend () -> T,
        onSuccess: (T) -> Unit
    ) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val result = action()
                onSuccess(result)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Загрузка списка контактов */
    fun loadContacts() = performAction(
        action = { getContactsUseCase() },
        onSuccess = { list -> _contacts.value = list }
    )

    /** Удаление дубликатов и обновление списка */
    fun onDeleteDuplicatesClicked() = performAction(
        action = { removeDuplicateContactsUseCase() },
        onSuccess = { count ->
            _removedCount.value = count
            loadContacts()
        }
    )
}
