package de.wpaul.fritztempviewer


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.wpaul.fritztempcommons.Config
import kotlinx.android.synthetic.main.fragment_config.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class ConfigFragment : Fragment() {

    private lateinit var config: Config

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSave.setOnClickListener { onBtnSaveClick() }
        btnCancel.setOnClickListener { onBtnResetClick() }

        fetchConfig()
    }

    private fun fetchConfig() = launch(UI) {
        withContext(CommonPool) {
            config = App.instance.loggerClient.getConfig()
        }

        etInterval.setText(config.interval)
        etSensor.setText(config.sensor)
    }


    private fun onBtnResetClick() {
        fetchConfig()
    }

    private fun onBtnSaveClick() {
        config.sensor = etSensor.text.toString()
        config.interval = etInterval.text.toString()
        launch(UI) {
            App.instance.loggerClient.setConfig(config)
            fetchConfig()
        }
    }
}