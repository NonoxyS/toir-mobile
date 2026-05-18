package ru.mirea.toir.feature.checklist.impl.domain

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
        val items = listOf(
            DomainChecklistItem.NumberItem(
                id = "i1",
                title = "Pump pressure",
                description = null,
                isRequired = true,
                requiresPhoto = false,
                resultId = null,
                photoCount = 0,
                value = null,
                min = null,
                max = null,
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
    fun `SetValidationOutOfRangeError sets isOutOfRangeError and clears others`() {
        val withErrors = initial.copy(isValidationError = true, isPhotoValidationError = true)

        val result = with(reducer) {
            withErrors.reduce(ChecklistStoreFactory.Message.SetValidationOutOfRangeError)
        }

        assertTrue(result.isOutOfRangeError)
        assertFalse(result.isValidationError)
        assertFalse(result.isPhotoValidationError)
    }

    @Test
    fun `ClearValidationError clears all validation flags including isOutOfRangeError`() {
        val withErrors = initial.copy(
            isValidationError = true,
            isPhotoValidationError = true,
            isOutOfRangeError = true,
        )

        val result = with(reducer) {
            withErrors.reduce(ChecklistStoreFactory.Message.ClearValidationError)
        }

        assertFalse(result.isValidationError)
        assertFalse(result.isPhotoValidationError)
        assertFalse(result.isOutOfRangeError)
    }

    @Test
    fun `SetCompleted marks state as completed`() {
        val result = with(reducer) { initial.reduce(ChecklistStoreFactory.Message.SetCompleted) }

        assertTrue(result.isCompleted)
    }

    @Test
    fun `SetNumberInvalid adds raw input to invalidNumberInputs by itemId`() {
        val result = with(reducer) {
            initial.reduce(ChecklistStoreFactory.Message.SetNumberInvalid("i1", ","))
        }

        assertEquals(mapOf("i1" to ","), result.invalidNumberInputs)
    }

    @Test
    fun `SetNumberInvalid overrides existing raw for same itemId`() {
        val withInvalid = initial.copy(invalidNumberInputs = mapOf("i1" to ","))

        val result = with(reducer) {
            withInvalid.reduce(ChecklistStoreFactory.Message.SetNumberInvalid("i1", "1."))
        }

        assertEquals(mapOf("i1" to "1."), result.invalidNumberInputs)
    }

    @Test
    fun `SetNumberInvalid keeps other invalid entries`() {
        val withInvalid = initial.copy(invalidNumberInputs = mapOf("i1" to ","))

        val result = with(reducer) {
            withInvalid.reduce(ChecklistStoreFactory.Message.SetNumberInvalid("i2", "abc"))
        }

        assertEquals(mapOf("i1" to ",", "i2" to "abc"), result.invalidNumberInputs)
    }

    @Test
    fun `ClearNumberInvalid removes only the given itemId`() {
        val withInvalid = initial.copy(invalidNumberInputs = mapOf("i1" to ",", "i2" to "abc"))

        val result = with(reducer) {
            withInvalid.reduce(ChecklistStoreFactory.Message.ClearNumberInvalid("i1"))
        }

        assertEquals(mapOf("i2" to "abc"), result.invalidNumberInputs)
    }

    @Test
    fun `ClearNumberInvalid is no-op for unknown itemId`() {
        val withInvalid = initial.copy(invalidNumberInputs = mapOf("i1" to ","))

        val result = with(reducer) {
            withInvalid.reduce(ChecklistStoreFactory.Message.ClearNumberInvalid("unknown"))
        }

        assertEquals(mapOf("i1" to ","), result.invalidNumberInputs)
    }

    @Test
    fun `SetValidationInvalidNumberError sets flag and clears others`() {
        val withErrors = initial.copy(
            isValidationError = true,
            isPhotoValidationError = true,
            isOutOfRangeError = true,
        )

        val result = with(reducer) {
            withErrors.reduce(ChecklistStoreFactory.Message.SetValidationInvalidNumberError)
        }

        assertTrue(result.isInvalidNumberError)
        assertFalse(result.isValidationError)
        assertFalse(result.isPhotoValidationError)
        assertFalse(result.isOutOfRangeError)
    }

    @Test
    fun `ClearValidationError also clears isInvalidNumberError`() {
        val withErrors = initial.copy(
            isValidationError = true,
            isPhotoValidationError = true,
            isOutOfRangeError = true,
            isInvalidNumberError = true,
        )

        val result = with(reducer) {
            withErrors.reduce(ChecklistStoreFactory.Message.ClearValidationError)
        }

        assertFalse(result.isInvalidNumberError)
        assertFalse(result.isValidationError)
        assertFalse(result.isPhotoValidationError)
        assertFalse(result.isOutOfRangeError)
    }
}
