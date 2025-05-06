package com.example.contactdedoppelganger.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.contactdedoppelganger.data.repo.ContactRepository
import com.example.contactdedoppelganger.domain.model.ContactDomain
import com.example.contactdedoppelganger.domain.usecase.GetContactsUseCase
import com.example.contactdedoppelganger.domain.usecase.RemoveDuplicateContactsUseCase
import com.example.contactdedoppelganger.ui.model.ContactSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Rule для подмены Dispatchers.Main на TestDispatcher (решение из коробки)
 *
 * Нужен чтобы корутины из ViewModel работали в тестах синхронно
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Тесты для MainViewModel.
 *
 * Техники тест-дизайна: Классы эквивалентности / Обработка ошибок
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    /** Rule для InstantLiveData обновлений */
    @get:Rule
    val instantTaskRule: TestRule = InstantTaskExecutorRule()

    /** Rule для замены Main-диспетчера */
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var repository: ContactRepository

    private lateinit var getContactsUseCase: GetContactsUseCase
    private lateinit var removeDuplicatesUseCase: RemoveDuplicateContactsUseCase
    private lateinit var viewModel: MainViewModel

    /** Dummy-контакты */
    private val dummyContacts = listOf(
        ContactDomain(id = "1", name = "John Dow", phoneNumbers = listOf("+71234567890")),
        ContactDomain(id = "2", name = "Jane Dow Dodow", phoneNumbers = listOf("+70987654321", "6051111")),
        ContactDomain(id = "3", name = "", phoneNumbers = listOf())
    )

    @Before
    fun setUp() {
        getContactsUseCase = GetContactsUseCase(repository)
        removeDuplicatesUseCase = RemoveDuplicateContactsUseCase(repository)
        viewModel = MainViewModel(getContactsUseCase, removeDuplicatesUseCase)
    }

    /**
     * Сценарий успеха загрузки контактов:
     * - Репозиторий возвращает список dummyContacts
     * - ViewModel выставляет isLoading=false, error=null
     * - contacts == dummyContacts
     * - sections = сгруппированные по первому символу
     */
    @Test
    fun `loadContacts success updates contacts, sections, isLoading and error`() = runTest {
        `when`(repository.getAllContacts()).thenReturn(dummyContacts)

        viewModel.loadContacts()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value!!)
        assertNull(viewModel.error.value)
        assertEquals(dummyContacts, viewModel.contacts.value)

        val expectedSections = listOf(
            ContactSection("#", listOf(dummyContacts[2])),
            ContactSection("J", listOf(dummyContacts[1], dummyContacts[0]))
        )
        assertEquals(expectedSections, viewModel.sections.value)

        verify(repository, times(1)).getAllContacts()
    }

    /**
     * Сценарий ошибки при загрузке:
     * - Репозиторий выбрасывает RuntimeException("load failed")
     * - ViewModel выставляет error="load failed" и isLoading=false
     * - contacts и sections остаются пустыми
     */
    @Test
    fun `loadContacts failure sets error and clears loading`() = runTest {
        `when`(repository.getAllContacts()).thenThrow(RuntimeException("load failed"))

        viewModel.loadContacts()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value!!)
        assertEquals("load failed", viewModel.error.value)
        assertTrue(viewModel.contacts.value.isNullOrEmpty())
        assertTrue(viewModel.sections.value!!.isEmpty())

        verify(repository).getAllContacts()
    }

    /**
     * Сценарий успешного удаления дубликатов:
     * - removeDuplicateContacts() возвращает 2
     * - После удаления ViewModel вызывает загрузку, contacts обновляется
     * - removedCount == 2, isLoading=false
     */
    @Test
    fun `onDeleteDuplicatesClicked success updates removedCount and reloads contacts`() = runTest {
        `when`(repository.removeDuplicateContacts()).thenReturn(2)
        `when`(repository.getAllContacts()).thenReturn(dummyContacts)

        viewModel.onDeleteDuplicatesClicked()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value!!)
        assertEquals(2, viewModel.removedCount.value)
        assertEquals(dummyContacts, viewModel.contacts.value)
        assertFalse(viewModel.sections.value!!.isEmpty())

        verify(repository, times(1)).removeDuplicateContacts()
        verify(repository, times(1)).getAllContacts()
    }

    /**
     * Сценарий ошибки при удалении дубликатов:
     * - removeDuplicateContacts() бросает RuntimeException("remove failed").
     * - ViewModel выставляет error="remove failed", isLoading=false.
     * - contacts и sections остаются пустыми, getAllContacts не вызывается.
     */
    @Test
    fun `onDeleteDuplicatesClicked failure sets error and keeps contacts empty`() = runTest {
        `when`(repository.removeDuplicateContacts()).thenThrow(RuntimeException("remove failed"))

        viewModel.onDeleteDuplicatesClicked()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value!!)
        assertEquals("remove failed", viewModel.error.value)
        assertTrue(viewModel.contacts.value.isNullOrEmpty())
        assertTrue(viewModel.sections.value!!.isEmpty())

        verify(repository).removeDuplicateContacts()
        verify(repository, never()).getAllContacts()
    }

    /**
     * Сценарий отрицательного результата при удалении дубликатов
     * (Невозможен без вмешательства в логику репозиторного слоя):
     * - removeDuplicateContacts() возвращает -1.
     * - ViewModel выставляет removedCount = -1 и перезагружает список контактов.
     */
    @Test
    fun `onDeleteDuplicatesClicked negative count updates removedCount and reloads contacts`() = runTest {
        /** При желании попасть можно... */
        `when`(repository.removeDuplicateContacts()).thenReturn(-1)
        `when`(repository.getAllContacts()).thenReturn(dummyContacts)

        viewModel.onDeleteDuplicatesClicked()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value!!)
        assertEquals(-1, viewModel.removedCount.value)
        assertEquals(dummyContacts, viewModel.contacts.value)
        assertFalse(viewModel.sections.value!!.isEmpty())

        verify(repository, times(1)).removeDuplicateContacts()
        verify(repository, times(1)).getAllContacts()
    }

}
