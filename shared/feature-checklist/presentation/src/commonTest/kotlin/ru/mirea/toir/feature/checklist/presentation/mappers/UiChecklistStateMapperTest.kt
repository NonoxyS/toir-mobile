package ru.mirea.toir.feature.checklist.presentation.mappers

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiChecklistStateMapperTest {

    private val mapper = UiChecklistStateMapperImpl()

    private fun item(
        id: String = "i1",
        type: DomainAnswerType = DomainAnswerType.Boolean,
        isRequired: Boolean = true,
        valueBoolean: Boolean? = null,
        valueNumber: Double? = null,
        valueText: String? = null,
        valueSelect: String? = null,
        isConfirmed: Boolean = false,
        numericMin: Double? = null,
        numericMax: Double? = null,
    ) = DomainChecklistItem(
        id = id,
        title = "Q",
        description = null,
        answerType = type,
        isRequired = isRequired,
        requiresPhoto = false,
        resultId = null,
        valueBoolean = valueBoolean,
        valueNumber = valueNumber,
        valueText = valueText,
        valueSelect = valueSelect,
        isConfirmed = isConfirmed,
        photoCount = 0,
        numericMin = numericMin,
        numericMax = numericMax,
    )

    @Test
    fun `showValidationError is false when isValidationError is false`() {
        val state = ChecklistStore.State(
            isValidationError = false,
            items = persistentListOf(item(isRequired = true, valueBoolean = null)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is true for required Boolean with null value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(isRequired = true, valueBoolean = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is false for required Boolean with non-null value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(isRequired = true, valueBoolean = false)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is false for optional unanswered item`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(isRequired = false, valueBoolean = null)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Number without value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Number, valueNumber = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError false for required Number with value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Number, valueNumber = 42.0)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Text with blank value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Text, valueText = "   ")),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Select with null option`() {
        val select = DomainAnswerType.Select(persistentListOf("a", "b"))
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = select, valueSelect = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Confirm not confirmed`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Confirm, isConfirmed = false)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError false for required Confirm when confirmed`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(type = DomainAnswerType.Confirm, isConfirmed = true)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `isNumberOutOfRange is false when valueNumber is within range`() {
        val state = ChecklistStore.State(
            items = persistentListOf(
                item(type = DomainAnswerType.Number, valueNumber = 50.0, numericMin = 0.0, numericMax = 100.0),
            ),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].isNumberOutOfRange)
    }

    @Test
    fun `isNumberOutOfRange is true when valueNumber exceeds max`() {
        val state = ChecklistStore.State(
            items = persistentListOf(
                item(type = DomainAnswerType.Number, valueNumber = 150.0, numericMax = 100.0),
            ),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].isNumberOutOfRange)
    }

    @Test
    fun `isNumberOutOfRange is true when valueNumber below min`() {
        val state = ChecklistStore.State(
            items = persistentListOf(
                item(type = DomainAnswerType.Number, valueNumber = -1.0, numericMin = 0.0),
            ),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].isNumberOutOfRange)
    }

    @Test
    fun `isNumberOutOfRange uses numberDraft when present`() {
        val state = ChecklistStore.State(
            items = persistentListOf(
                item(type = DomainAnswerType.Number, valueNumber = 50.0, numericMax = 100.0),
            ),
            numberDrafts = persistentMapOf("i1" to "9999"),
        )
        val ui = mapper.map(state)
        assertEquals("9999", ui.items[0].valueNumber)
        assertTrue(ui.items[0].isNumberOutOfRange)
    }

    @Test
    fun `isNumberOutOfRange is false for non-Number answer types`() {
        val state = ChecklistStore.State(
            items = persistentListOf(item(type = DomainAnswerType.Text, valueText = "999")),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].isNumberOutOfRange)
    }

    @Test
    fun `mapper preserves item count`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(id = "i1"), item(id = "i2")),
        )
        val ui = mapper.map(state)
        assertEquals(2, ui.items.size)
    }
}
