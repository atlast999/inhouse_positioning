package com.example.composeapp.feature

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.composeapp.MainActivity
import com.example.composeapp.databinding.SetupFragmentBinding
import com.example.composeapp.model.AccessPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.pow

class SetupFragment : Fragment() {
    private lateinit var binding: SetupFragmentBinding
    private val blockingState = MutableStateFlow(false)
    private val adapter = CustomAdapter(true).apply {
        itemClickListener = {
            findNavController().navigate(
                SetupFragmentDirections.toRegister(
                    it.name,
                    it.mac,
                    it.rssi,
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SetupFragmentBinding.inflate(inflater, container, false)
        binding.recyclerView.adapter = adapter
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnView.setOnClickListener {
            findNavController().navigate(SetupFragmentDirections.toRegisteredList())
        }

        val wifiManager = requireActivity().getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess(wifiManager)
                }
            }
        }

        IntentFilter().run {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            requireActivity().registerReceiver(wifiScanReceiver, this)

        }
        binding.btnScan.setOnClickListener {
            blockingState.value = true
            wifiManager.startScan()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(
                state = Lifecycle.State.STARTED,
            ) {
                blockingState.collectLatest {
                    binding.progressBar.visibility = if (it) {
                        View.VISIBLE
                    } else View.GONE

                }
            }
        }

    }

    private fun getDistanceRssi(rssi: Int): Double {
        val r0 = 4
        val pr0 = -42.6
        val n = 2.4
        return (pr0 - rssi.toDouble()).div(10 * n).let {
            10.0.pow(it) * r0
        }
    }


    private fun scanSuccess(wifiManager: WifiManager) {
        val results = wifiManager.scanResults
        results.map {
            AccessPoint(
                name = it.SSID,
                mac = it.BSSID,
                rssi = it.level,
            )
        }.let {
            adapter.applyNewData(it)
            (requireActivity() as MainActivity).samples[binding.edtRoom.text.toString()] = it
        }

        blockingState.value = false
    }
}