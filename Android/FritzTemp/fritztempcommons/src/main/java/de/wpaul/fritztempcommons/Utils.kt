package de.wpaul.fritztempcommons

import android.os.AsyncTask
import java.util.*

fun runAsyncAction(action: () -> Unit): AsyncTask<Void, Void, Void?>? {
    return object : AsyncTask<Void, Void, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            action()
            return null
        }
    }.execute()
}


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