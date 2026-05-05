package ru.mirea.toir.feature.checklist.impl.domain

import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore.State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChecklistReducerTest {

    private val reducer = ChecklistReducer()
    private val initial = State(equipmentResultId = "result-1")

    @Test
    fun `SetLoading sets isLoading and clears errors`() {
        val withErrors = initial.copy(
            isError = true,
            isValidationError = true,
            isPhotoValidationError = true,
        )

        val result = with(reducer) { withErrors.reduce(ChecklistStoreFactory.Message.SetLoading) }

        assertTrue(result.isLoading)
        assertFalse(result.isError)
        assertFalse(result.isValidationError)
        assertFalse(result.isPhotoValidationError)
    }

    @Test
    fun `SetError marks state as error and stops loading`() {
        val loading = initial.copy(isLoading = true)

        val result = with(reducer) { loading.reduce(ChecklistStoreFactory.Message.SetError) }

        assertTrue(result.isError)
        assertFalse(result.isLoading)
    }

    @Test
    fun `SetItems replaces items list and stops loading`() {
        val items = persistentListOf(
            DomainChecklistItem(
                id = "i1",
                title = "Pump pressure",
                description = null,
                answerType = DomainAnswerType.Number,
                isRequired = true,
                requiresPhoto = false,
                resultId = null,
                valueBoolean = null,
                valueNumber = null,
                valueText = null,
                valueSelect = null,
                isConfirmed = false,
                photoCount = 0,
                numericMin = null,
                numericMax = null,
            ),
        )

        val result = with(reducer) {
            initial.copy(isLoading = true).reduce(ChecklistStoreFactory.Message.SetItems(items))
        }

        assertEquals(1, result.items.size)
        assertFalse(result.isLoading)
    }

    @Test
    fun `SetValidationRequiredError sets isValidationError`() {
        val result = with(reducer) {
            initial.reduce(ChecklistStoreFactory.Message.SetValidationRequiredError)
        }

        assertTrue(result.isValidationError)
        assertFalse(result.isPhotoValidationError)
    }

    @Test
    fun `SetValidationPhotoError sets isPhotoValidationError`() {
        val result = with(reducer) {
            initial.reduce(ChecklistStoreFactory.Message.SetValidationPhotoError)
        }

        assertFalse(result.isValidationError)
        assertTrue(result.isPhotoValidationError)
    }

    @Test
    fun `ClearValidationError clears both validation flags`() {
        val withErrors = initial.copy(
            isValidationError = true,
            isPhotoValidationError = true,
        )

        val result = with(reducer) {
            withErrors.reduce(ChecklistStoreFactory.Message.ClearValidationError)
        }

        assertFalse(result.isValidationError)
        assertFalse(result.isPhotoValidationError)
    }

    @Test
    fun `SetCompleted marks state as completed`() {
        val result = with(reducer) { initial.reduce(ChecklistStoreFactory.Message.SetCompleted) }

        assertTrue(result.isCompleted)
    }
}
