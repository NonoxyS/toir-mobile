package ru.mirea.toir.feature.checklist.presentation.mappers

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

    private fun boolItem(
        id: String = "i1",
        isRequired: Boolean = true,
        value: Boolean? = null,
    ) = DomainChecklistItem.Boolean(
        id = id,
        title = "Q",
        description = null,
        isRequired = isRequired,
        requiresPhoto = false,
        resultId = null,
        photoCount = 0,
        value = value,
    )

    private fun textItem(
        id: String = "i1",
        isRequired: Boolean = true,
        value: String? = null,
    ) = DomainChecklistItem.Text(
        id = id,
        title = "Q",
        description = null,
        isRequired = isRequired,
        requiresPhoto = false,
        resultId = null,
        photoCount = 0,
        value = value,
    )

    private fun selectItem(
        id: String = "i1",
        isRequired: Boolean = true,
        value: String? = null,
        options: List<String> = listOf("a", "b"),
    ) = DomainChecklistItem.Select(
        id = id,
        title = "Q",
        description = null,
        isRequired = isRequired,
        requiresPhoto = false,
        resultId = null,
        photoCount = 0,
        value = value,
        options = options,
    )

    private fun confirmItem(
        id: String = "i1",
        isRequired: Boolean = true,
        isConfirmed: Boolean = false,
    ) = DomainChecklistItem.Confirm(
        id = id,
        title = "Q",
        description = null,
        isRequired = isRequired,
        requiresPhoto = false,
        resultId = null,
        photoCount = 0,
        isConfirmed = isConfirmed,
    )

    @Test
    fun `showValidationError is false when isValidationError is false`() {
        val state = ChecklistStore.State(
            isValidationError = false,
            items = listOf(boolItem(isRequired = true, value = null)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is true for required Boolean with null value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(boolItem(isRequired = true, value = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is false for required Boolean with non-null value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(boolItem(isRequired = true, value = false)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError is false for optional unanswered item`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(boolItem(isRequired = false, value = null)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Text with blank value`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(textItem(value = "   ")),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Select with null option`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(selectItem(value = null)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError true for required Confirm not confirmed`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(confirmItem(isConfirmed = false)),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0].showValidationError)
    }

    @Test
    fun `showValidationError false for required Confirm when confirmed`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(confirmItem(isConfirmed = true)),
        )
        val ui = mapper.map(state)
        assertFalse(ui.items[0].showValidationError)
    }

    @Test
    fun `mapper preserves item count`() {
        val state = ChecklistStore.State(
            isValidationError = true,
            items = listOf(boolItem(id = "i1"), boolItem(id = "i2")),
        )
        val ui = mapper.map(state)
        assertEquals(2, ui.items.size)
    }

    @Test
    fun `non-Number items are mapped to correct sealed subtypes`() {
        val state = ChecklistStore.State(
            items = listOf(
                boolItem(id = "b"),
                textItem(id = "t"),
                selectItem(id = "s", options = listOf("a")),
                confirmItem(id = "c"),
            ),
        )
        val ui = mapper.map(state)
        assertTrue(ui.items[0] is UiChecklistItem.Boolean)
        assertTrue(ui.items[1] is UiChecklistItem.Text)
        assertTrue(ui.items[2] is UiChecklistItem.Select)
        assertTrue(ui.items[3] is UiChecklistItem.Confirm)
    }
}
