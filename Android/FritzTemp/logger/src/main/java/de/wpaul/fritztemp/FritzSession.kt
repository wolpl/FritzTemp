package de.wpaul.fritztemp

import android.util.Log
import de.wpaul.fritztempcommons.getXmlValue
import de.wpaul.fritztempcommons.toHexString
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest

class FritzSession {
    companion object {
        private const val EmptySid = "0000000000000000"
        private const val TAG = "FritzSession"
    }

    private var sid: String

    init {
        sid = getSessionId()
        if (sid == EmptySid) Log.w(TAG, "Could not get valid SID")
        println("New SID: $sid")
    }

    private fun getSessionId(username: String = App.instance!!.username, password: String = App.instance!!.password): String {
        val url = "http://fritz.box/login_sid.lua"
        with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            var doc = httpGet(url)
            sid = getXmlValue(doc, "SID")
            if (sid == "0000000000000000") {
                val challenge = getXmlValue(doc, "Challenge")
                val response = createResponse(challenge, password)
                doc = httpGet("http://fritz.box/login_sid.lua?username=$username&response=$response")
                sid = getXmlValue(doc, "SID")
            }
            return sid
        }
    }

    private fun createResponse(challenge: String, password: String): String {
        val text = "$challenge-$password".toByteArray(Charset.forName("utf-16le"))
        val digest = MessageDigest.getInstance("md5").digest(text)
        return challenge + "-" + digest.toHexString().toLowerCase()
    }

    private fun httpGet(url: String, isFirstTry: Boolean = true): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return when {
            response.isSuccessful -> response.body()!!.string()
            isFirstTry -> {
                getSessionId()
                httpGet(url, false)
            }
            else -> throw RuntimeException("Cannot perform HTTP request!")
        }
    }

    private fun request(ain: String, switchcmd: String): String = httpGet("http://fritz.box/webservices/homeautoswitch.lua?ain=$ain&switchcmd=$switchcmd&sid=$sid")

    fun getTemperature(ain: String = "119600690696"): Float {
        return request(ain, "gettemperature").toFloat() / 10
    }

}
