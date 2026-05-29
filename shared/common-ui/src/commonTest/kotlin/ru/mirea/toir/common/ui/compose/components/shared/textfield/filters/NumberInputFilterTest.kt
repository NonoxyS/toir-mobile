package ru.mirea.toir.common.ui.compose.components.shared.textfield.filters

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberInputFilterTest {

    private val filter = NumberInputFilter

    @Test
    fun `empty string returns empty string`() {
        assertEquals("", filter.apply(""))
    }

    @Test
    fun `digits only pass through`() {
        assertEquals("123", filter.apply("123"))
    }

    @Test
    fun `comma separator is preserved`() {
        assertEquals("1,5", filter.apply("1,5"))
    }

    @Test
    fun `dot separator is preserved`() {
        assertEquals("1.5", filter.apply("1.5"))
    }

    @Test
    fun `spaces are removed`() {
        assertEquals("123", filter.apply("1 2 3"))
    }

    @Test
    fun `second separator is dropped`() {
        assertEquals("1.55", filter.apply("1.5.5"))
    }

    @Test
    fun `negative number is preserved`() {
        assertEquals("-1.5", filter.apply("-1.5"))
    }

    @Test
    fun `minus not at start is removed`() {
        assertEquals("12", filter.apply("1-2"))
    }

    @Test
    fun `letters are removed`() {
        assertEquals("123", filter.apply("abc123"))
    }

    @Test
    fun `non-breaking space is removed`() {
        assertEquals("12", filter.apply("1 2"))
    }
}
