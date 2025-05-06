package com.example.contactdedoppelganger.domain.usecase

import com.example.contactdedoppelganger.data.repo.ContactRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

/**
 * Тесты для RemoveDuplicateContactsUseCase
 *
 * Техники тест-дизайна: Классы эквивалентности / Прогнозирование ошибок
 */
@RunWith(MockitoJUnitRunner::class)
class RemoveDuplicateContactsUseCaseTest {

    @Mock
    lateinit var repository: ContactRepository

    private lateinit var useCase: RemoveDuplicateContactsUseCase

    @Before
    fun setUp() {
        useCase = RemoveDuplicateContactsUseCase(repository)
    }

    /**
     * Сценарий успеха:
     * - репозиторий возвращает количество удалённых контактов
     * - use case возвращает то же число
     */
    @Test
    fun `invoke returns count from repository on success`() = runTest {
        val expectedCount = 5
        `when`(repository.removeDuplicateContacts()).thenReturn(expectedCount)

        val result = useCase()

        assertEquals(expectedCount, result)
        verify(repository, times(1)).removeDuplicateContacts()
    }

    /**
     * Сценарий ошибки:
     * - репозиторий бросает исключение,
     * - use case должен пробросить это же исключение.
     */
    @Test
    fun `invoke throws when repository throws exception`() = runTest {
        `when`(repository.removeDuplicateContacts()).thenThrow(RuntimeException("Remove error"))

        try {
            useCase()
            /** Если попал сюда, значит исключение не было брошено, а тест провален */
            fail("Expected RuntimeException was not thrown")
        } catch (e: RuntimeException) {
            assertEquals("Remove error", e.message)
        }

        verify(repository, times(1)).removeDuplicateContacts()
    }
}
