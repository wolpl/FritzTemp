package de.wpaul.fritztempcommons


import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class DateConverterTest {

    private lateinit var c: DateConverter
    private lateinit var d1: Date

    @Before
    fun setUp() {
        c = DateConverter()
        d1 = Date()
    }

    @Test
    fun `return non empty or blank string`() {
        val s1 = c.toString(d1)
        assert(!s1.isEmpty() && !s1.isBlank())
    }

    @Test
    fun `equals back conversion string`() {
        val s1 = c.toString(d1)
        val d2 = c.toDate(s1)
        assertEquals(d1, d2)
    }
}