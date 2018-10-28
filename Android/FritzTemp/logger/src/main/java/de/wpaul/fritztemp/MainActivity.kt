package de.wpaul.fritztemp

import android.app.Activity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import com.google.android.things.device.TimeManager
import de.wpaul.fritztempcommons.MeasurementsDB

class MainActivity : Activity() {

    companion object {
        private const val TAG = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Allow network operations on main thread
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //Ensure correct time
        TimeManager.getInstance().setTimeZone("Europe/Berlin")
        TimeManager.getInstance().setTimeFormat(TimeManager.FORMAT_24)
        TimeManager.getInstance().setAutoTimeEnabled(true)

        val config = Config(filesDir.absolutePath)

        //Start temperature logger
        val tempLogger = TemperatureLogger(config, MeasurementsDB.create(this, "Temperatures.db"))
        Log.i(TAG, "Started temperature logger")

        //Start server
        Server(tempLogger)
        Log.i(TAG, "Started server")


    }
}

