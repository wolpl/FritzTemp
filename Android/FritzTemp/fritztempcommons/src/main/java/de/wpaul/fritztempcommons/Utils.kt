package de.wpaul.fritztempcommons

import java.time.Instant
import java.time.LocalDateTime
import java.util.*

fun getXmlValue(document: String, tag: String): String = document.split("<$tag>")[1].split("</$tag>")[0]

private val hexArray = "0123456789ABCDEF".toCharArray()
fun ByteArray.toHexString(): String {
    val hexChars = CharArray(this.size * 2)
    for (j in this.indices) {
        val v = this[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v.ushr(4)]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

fun Date.plus(field: Int, amount: Int) = Calendar.getInstance().apply {
    time = this@plus
    this.add(field, amount)
}.time!!

fun Long.toLocalDateTime() = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), TimeZone.getDefault().toZoneId())!!