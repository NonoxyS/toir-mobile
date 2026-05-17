package ru.mirea.toir.feature.checklist.presentation.mappers

import kotlinx.collections.immutable.persistentListOf
import ru.mirea.toir.feature.checklist.api.models.DomainAnswerType
import ru.mirea.toir.feature.checklist.api.models.DomainChecklistItem
import ru.mirea.toir.feature.checklist.api.store.ChecklistStore
import ru.mirea.toir.feature.checklist.presentation.models.UiChecklistItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Number-маппинг здесь не покрыт: mapper строит StringDesc через MR.strings.*,
// а на iosSimulatorArm64Test moko-bundle не инициализируется (FileFailedToInitializeException).
// Проверка Number-ветки (isOutOfRange, rangeHint, showValidationError для Number) — через ручной QA.

class UiChecklistStateMapperTest {

    private val mapper = UiChecklistStateMapperImpl()

    private fun item(
        id: String = "i1",
        type: DomainAnswerType = DomainAnswerType.Boolean,
        isRequired: Boolean = true,
        valueBoolean: Boolean? = null,
        valueText: String? = null,
        valueSelect: String? = null,
        isConfirmed: Boolean = false,
    ) = DomainChecklistItem(
        id = id,
        title = "Q",
        description = null,
        answerType = type,
        isRequired = isRequired,
        requiresPhoto = false,
        resultId = null,
        valueBoolean = valueBoolean,
        valueNumber = null,
        valueText = valueText,
        valueSelect = valueSelect,
        isConfirmed = isConfirmed,
        photoCount = 0,
        numericMin = null,
        numericMax = null,
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
    fun `mapper preserves item count`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = persistentListOf(item(id = "i1"), item(id = "i2")),
        )
        val ui = mapper.map(state)
        assertEquals(2, ui.items.size)
    }

    @Test
    fun `non-Number items are mapped to correct sealed subtypes`() {
        val state = ChecklistStore.State(
            items = persistentListOf(
                item(id = "b", type = DomainAnswerType.Boolean),
                item(id = "t", type = DomainAnswerType.Text),
                item(id = "s", type = DomainAnswerType.Select(persistentListOf("a"))),
                item(id = "c", type = DomainAnswerType.Confirm),
            ),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0] is UiChecklistItem.Boolean)
        assertTrue(ui.items[1] is UiChecklistItem.Text)
        assertTrue(ui.items[2] is UiChecklistItem.Select)
        assertTrue(ui.items[3] is UiChecklistItem.Confirm)
    }
}
