package com.example.contactdedoppelganger.domain.usecase

import com.example.contactdedoppelganger.data.repo.ContactRepository
import com.example.contactdedoppelganger.domain.model.ContactDomain
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

/**
 * Тесты для GetContactsUseCase
 *
 * Техники тест-дизайна: Классы эквивалентности / Прогнозирование ошибок
 */
@RunWith(MockitoJUnitRunner::class)
class GetContactsUseCaseTest {

    @Mock
    lateinit var repository: ContactRepository

    private lateinit var useCase: GetContactsUseCase

    /** Dummy-контакты */
    private val dummyContacts = listOf(
        ContactDomain(id = "1", name = "John Dow", phoneNumbers = listOf("+71234567890")),
        ContactDomain(id = "2", name = "Jane Dow Dodow", phoneNumbers = listOf("+70987654321", "6051111"))
    )

    @Before
    fun setUp() {
        useCase = GetContactsUseCase(repository)
    }

    /**
     * Сценарий успеха:
     * - репозиторий возвращает список контактов
     * - use case отдает тот же список.
     */
    @Test
    fun `invoke returns list from repository on success`() = runTest {
        `when`(repository.getAllContacts()).thenReturn(dummyContacts)

        val result = useCase()

        assertEquals(dummyContacts, result)
        verify(repository, times(1)).getAllContacts()
        verifyNoMoreInteractions(repository)
    }

    /**
     * Сценарий ошибки:
     * - репозиторий бросает исключение
     * - use case должен пробросить это же исключение
     */
    @Test
    fun `invoke throws when repository throws exception`() = runTest {
        `when`(repository.getAllContacts()).thenThrow(RuntimeException("Test error"))

        try {
            useCase()
            org.junit.Assert.fail("Expected RuntimeException was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Test error", e.message)
        }

        verify(repository, times(1)).getAllContacts()
    }
}
