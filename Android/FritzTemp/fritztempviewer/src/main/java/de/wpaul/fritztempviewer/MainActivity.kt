package de.wpaul.fritztempviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.transaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        setContentFragment(StatusFragment())

        nav_view.setNavigationItemSelectedListener(this)

        if (App.instance.loggerClient.uri == null) launchSetUriActivity()

        title = App.instance.loggerClient.uri
    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
            val status = App.instance.loggerClient.getStatus()
            val entries = status.logEntries
            runOnUiThread {
                val infoText = nav_view.getHeaderView(0).findViewById<TextView>(R.id.navHeaderMainAdditionalInfo)
                infoText.text = getString(R.string.nav_header_additional_info).format(entries)
            }
        }
    }

    private fun launchSetUriActivity() {
        val intent = Intent(this, SetUriActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                launchSetUriActivity()
                true
            }
            R.id.action_refresh_charts -> {
                GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                    App.instance.loggerClient.refreshLog()
                    //TODO: refresh charts
                }
                true
            }
            R.id.action_save_database -> {
                val target = getExternalFilesDir(null).resolve("FritzTempDB.sqlite")
                Log.i(TAG, "Saving backup to ${target.absolutePath}")
                getDatabasePath(App.instance.loggerClient.dbName).copyTo(target, true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_status -> {
                setContentFragment(StatusFragment())
            }
            R.id.nav_raw_chart -> {
                //if (fragment_container.targetFragment !is ChartRawFragment)
                setContentFragment(ChartRawFragment())
            }
            R.id.nav_min_max_chart -> {
                //if (fragment_container.targetFragment !is ChartMinMaxFragment)
                setContentFragment(ChartMinMaxFragment())
            }
            R.id.nav_config -> {
                setContentFragment(ConfigFragment())
            }
            else -> Log.w(TAG, "Unknown navigation item selected: ${item.title}")
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setContentFragment(f: androidx.fragment.app.Fragment) {
        supportFragmentManager.transaction {
            replace(R.id.fragment_container, f)
            //addToBackStack(null)
        }
    }
}
