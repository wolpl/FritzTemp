package de.wpaul.fritztempviewer


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import de.wpaul.fritztempcommons.Config
import kotlinx.android.synthetic.main.fragment_config.*
import kotlinx.coroutines.*

class ConfigFragment : Fragment() {

    private lateinit var config: Config
    private lateinit var viewModel: ReportingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run { ViewModelProviders.of(this).get(ReportingViewModel::class.java) } ?: throw Exception("Invalid activity!")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSave.setOnClickListener { onBtnSaveClick() }
        btnCancel.setOnClickListener { onBtnResetClick() }

        fetchConfig()
    }

    private fun fetchConfig() = GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
        withContext(Dispatchers.Default) {
            config = viewModel.getConfig()
        }

        etInterval.setText(config.interval.toString())
        etSensor.setText(config.sensor)
    }


    private fun onBtnResetClick() {
        fetchConfig()
    }

    private fun onBtnSaveClick() {
        config.sensor = etSensor.text.toString()
        config.interval = etInterval.text.toString().toLong()
        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            viewModel.setConfig(config)
            fetchConfig()
        }
    }
}
