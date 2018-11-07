package de.wpaul.fritztempcommons


import com.beust.klaxon.Klaxon
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class StatusTest {

    @Test
    fun `serialize and deserialize`() {
        val status = Status("OK", "2348983053", 300000, 1552, LocalDateTime.now(), "22.5")
        val json = Klaxon().toJsonString(status)
        println("Json String: $json")
        val copy = Klaxon().parse<Status>(json)
        assertEquals(status, copy)
    }
}