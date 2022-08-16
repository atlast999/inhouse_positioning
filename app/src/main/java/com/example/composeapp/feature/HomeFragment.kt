package com.example.composeapp.feature

import android.annotation.SuppressLint
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
import com.example.composeapp.app.AppDatabase
import com.example.composeapp.databinding.HomeFragmentBinding
import com.example.composeapp.model.AccessPoint
import com.example.composeapp.model.ApDao
import com.example.composeapp.model.ApPositionInfo
import com.example.composeapp.model.SampleDao
import com.example.composeapp.toast
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver
import com.lemmingapex.trilateration.TrilaterationFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer
import kotlin.math.pow
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    enum class LocatingState {
        BY_POWER,
        BY_SAMPLE,
        NONE,
    }

    private lateinit var binding: HomeFragmentBinding
    private val locatedFlow = MutableStateFlow(LocatingState.NONE)
    private val previousAps = mutableMapOf<String, ApPositionInfo>()
    private val currentAps = mutableMapOf<String, ApPositionInfo>()
    private lateinit var apDao: ApDao
    private lateinit var sampleDao: SampleDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        AppDatabase.getInstance(requireContext()).let {
            sampleDao = it.sampleDao()
            apDao = it.apDao()
        }

        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity
        binding.apply {
            btnLocatePower.setOnClickListener {
                locatedFlow.value = LocatingState.BY_POWER
                mainActivity.startScanning()
            }
            btnSetupPower.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.toSetUp(SetupFragment.SETUP_POWER))
            }

            btnLocateSample.setOnClickListener {
                locatedFlow.value = LocatingState.BY_SAMPLE
                mainActivity.startScanning()
            }
            btnSetupSample.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.toSetUp(SetupFragment.SETUP_SAMPLE))
            }
            btnSocket.setOnClickListener {
                findNavController().navigate(HomeFragmentDirections.toSocket())
            }
        }


        mainActivity.scannedApFlow.observe(viewLifecycleOwner) {
            it?.let {
                when (locatedFlow.value) {
                    LocatingState.BY_POWER -> handleScannedPower(it)
                    LocatingState.BY_SAMPLE -> collectSignals(it)
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(
                state = Lifecycle.State.STARTED,
            ) {
                locatedFlow.collectLatest {
                    binding.progressBar.visibility = when (it) {
                        LocatingState.NONE -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            }
        }
    }

    private fun handleScannedPower(aps: List<AccessPoint>) {
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

    private var scannedCount = 0
    private val mapOfSample = mutableMapOf<String, MutableList<AccessPoint>>()
    private fun collectSignals(signals: List<AccessPoint>) {
        if (scannedCount < 5) {
            signals.forEach {
                val listOfSample = mapOfSample[it.uid]
                if (listOfSample == null) {
                    mapOfSample[it.uid] = mutableListOf(it)
                } else {
                    listOfSample.add(it)
                }
            }
            scannedCount++
            binding.tvResultPower.text = scannedCount.toString()
            locatedFlow.value = LocatingState.NONE
        } else {
            mapOfSample.map {
                val key = it.value.first()
                val value = it.value.sumOf { it.rssi }.toDouble().div(it.value.size)
                key to value
            }.sortedBy { -it.second }
                .take(6)
                .map {
                    it.first.apply {
                        rssi = it.second.roundToInt()
                    }
                }.let { list ->
                    handleScannedSample(list)
                }
            scannedCount = 0
        }
    }

    private fun handleScannedSample(signals: List<AccessPoint>) {
        lifecycleScope.launch {
            val samples = sampleDao.getAll()
            samples.associateWith {
                it.calculateEclipseDistance(signals)
            }.minByOrNull { it.value }?.let {
                withContext(Dispatchers.Main) {
                    locatedFlow.value = LocatingState.NONE
                    binding.tvResultSample.text = "${it.key.name} - ${it.value}"
                }
            }
        }
    }

}