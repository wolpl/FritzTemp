package de.wpaul.fritztempcommons


import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class DateTimeConverterTest {

    private lateinit var c: DateTimeConverter
    private lateinit var d1: LocalDateTime

    @Before
    fun setUp() {
        c = DateTimeConverter()
        d1 = LocalDateTime.now()
    }

    @Test
    fun `return non empty or blank string`() {
        val s1 = c.toString(d1)
        assert(!s1.isEmpty() && !s1.isBlank())
    }

    @Test
    fun `equals back conversion string`() {
        val s1 = c.toString(d1)
        val d2 = c.toDateTime(s1)
        assertEquals(d1, d2)
    }

    @Test
    fun `format correctly`() {
        d1 = LocalDateTime.of(2000, 3, 2, 10, 20, 39, 123000000)
        assertEquals("2000-03-02 10:20:39.123", c.toString(d1))
    }
}