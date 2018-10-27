package de.wpaul.fritztempviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import de.wpaul.fritztempcommons.ServerUri
import de.wpaul.fritztempcommons.runAsyncAction
import kotlinx.android.synthetic.main.activity_set_uri.*

class SetUriActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SetUriActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_uri)
        UriEditText.setText(App.instance.loggerClient.uri)
    }

    fun onTestButtonClick(v: View) {
        val uriString = UriEditText.text.toString()
        testUri(uriString)
    }

    fun onSaveButtonClick(v: View) {
        App.instance.loggerClient.uri = ServerUri(UriEditText.text.toString()).minimizedBase
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun testUri(uriString: String) {
        val uri = ServerUri(uriString)
        runAsyncAction {
            try {
                val response = App.instance.loggerClient.getStatusRaw(uri.full)
                runOnUiThread {
                    currentValueView.text = response
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error while trying to get status", e)
            }
        }
    }
}
