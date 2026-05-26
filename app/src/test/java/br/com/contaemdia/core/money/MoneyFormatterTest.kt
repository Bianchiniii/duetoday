package br.com.contaemdia.core.money

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun parsesBrazilianDecimalInputAsCents() {
        assertEquals(1200L, "12,00".toCentsOrNull())
        assertEquals(120050L, "1200,50".toCentsOrNull())
    }

    @Test
    fun formatsCentsForEditingWithComma() {
        assertEquals("12,00", 1200L.toDecimalInput())
    }

    @Test
    fun filtersMoneyInputToDigitsAndSingleDecimalComma() {
        assertEquals("123,45", "R$ 123,456abc".toMoneyInput())
        assertEquals("1200", "1.200".toMoneyInput())
    }

    @Test
    fun rejectsEmptyMoneyInput() {
        assertNull("abc".toCentsOrNull())
    }
}
