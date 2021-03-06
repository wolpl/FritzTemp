package de.wpaul.fritztempviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.wpaul.fritztempcommons.ServerUri
import kotlinx.android.synthetic.main.activity_set_uri.*
import kotlinx.coroutines.*

class SetUriActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SetUriActivity"
    }

    private var loggerClient = LoggerClient(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_uri)
        UriEditText.setText(loggerClient.uri)
    }

    fun onTestButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        val uriString = UriEditText.text.toString()
        testUri(uriString)
    }

    fun onSaveButtonClick(@Suppress("UNUSED_PARAMETER") v: View) {
        loggerClient.uri = ServerUri(UriEditText.text.toString()).minimizedBase
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun testUri(uriString: String) {
        val uri = ServerUri(uriString)
        GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            try {
                val response = loggerClient.getStatusRaw(uri.full)
                withContext(Dispatchers.Main) {
                    currentValueView.text = response
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while trying to get status", e)
            }
        }
    }
}
