package com.example.monthnameandnotes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun getMonthName_returnsUkrainianMonthNameForValidNumber() {
        assertEquals("Січень", getMonthName(1))
        assertEquals("Грудень", getMonthName(12))
    }

    @Test
    fun getMonthName_returnsNullForInvalidNumber() {
        assertNull(getMonthName(0))
        assertNull(getMonthName(13))
        assertNull(getMonthName(null))
    }
}
