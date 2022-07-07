package com.example.composeapp.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.composeapp.MainActivity
import com.example.composeapp.app.AppDatabase
import com.example.composeapp.databinding.HomeFragmentBinding
import com.example.composeapp.model.AccessPoint
import com.example.composeapp.model.ApDao
import com.example.composeapp.model.ApPositionInfo
import com.example.composeapp.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow

class HomeFragment : Fragment() {
    private lateinit var binding: HomeFragmentBinding
    private val locatedFlow = MutableStateFlow(false)
    private val previousAps = mutableMapOf<String, ApPositionInfo>()
    private val currentAps = mutableMapOf<String, ApPositionInfo>()
    private lateinit var apDao: ApDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        apDao = AppDatabase.getInstance(requireContext()).apDao()
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity
        binding.btnLocate.setOnClickListener {
            locatedFlow.value = false
            mainActivity.startScanning()
        }
        binding.btnSetup.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.toSetUp())
        }
        mainActivity.scannedApFlow.observe(viewLifecycleOwner) {
            it?.let {
                handleScannedAp(it)
            }
        }
    }

    private fun handleScannedAp(aps: List<AccessPoint>) {
        if (aps.isEmpty()) return
        lifecycleScope.launch {
            val scannedApsMap = aps.associateBy { it.uid }
            previousAps.clear()
            previousAps.putAll(currentAps)
            currentAps.clear()
            currentAps.putAll(
                apDao.getByUid(scannedApsMap.keys.toList())
                    .map {
                        it.apply {
                            rssi = scannedApsMap[it.uid]!!.rssi
                        }
                    }.associateBy {
                        it.uid
                    }
            )
            val needed = 4 - currentAps.size
            val previousCandidates =
                previousAps.values.filter { currentAps.containsKey(it.uid) }.sortedBy { -it.rssi }
            if (previousCandidates.size < needed) {
                withContext(Dispatchers.Main) {
                    toast(requireContext(), "Cannot find enough APs, try again!!!")
                }
                return@launch
            }
            for (i in 0 until needed) {
                val candidate = previousCandidates[i]
                currentAps[candidate.uid] = candidate
            }
            val distances = calculateDistance(currentAps.values.toList())

        }
    }

    private fun calculateDistance(aps: List<ApPositionInfo>): Array<Double> {
        return aps.map { ap ->
            (ap.pro - ap.rssi.toDouble()).div(10 * 2.4).let {
                10.0.pow(it) * ap.ro
            }
        }.toTypedArray()
    }

}