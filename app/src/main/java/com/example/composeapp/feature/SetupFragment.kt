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
import androidx.navigation.fragment.navArgs
import com.example.composeapp.MainActivity
import com.example.composeapp.app.AppDatabase
import com.example.composeapp.databinding.SetupFragmentBinding
import com.example.composeapp.feature.adapter.ScannedAdapter
import com.example.composeapp.model.PositionSample
import com.example.composeapp.model.SampleDao
import com.example.composeapp.toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupFragment : Fragment() {

    companion object {
        const val SETUP_POWER = "SETUP_POWER"
        const val SETUP_SAMPLE = "SETUP_SAMPLE"
    }

    private lateinit var sampleDao: SampleDao
    private val navArgs: SetupFragmentArgs by navArgs()
    private lateinit var binding: SetupFragmentBinding
    private val blockingState = MutableStateFlow(false)
    private val adapter = ScannedAdapter().apply {
        itemClickListener = {
            when (navArgs.setupType) {
                SETUP_POWER -> {
                    findNavController().navigate(
                        SetupFragmentDirections.toRegister(
                            it,
                            null,
                            RegisterFragment.ACTION_REGISTER
                        )
                    )
                }
                else -> {}
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sampleDao = AppDatabase.getInstance(requireContext()).sampleDao()
        binding = SetupFragmentBinding.inflate(inflater, container, false)
        binding.recyclerView.adapter = adapter
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity

        binding.btnScan.setOnClickListener {
            blockingState.value = true
            mainActivity.startScanning()
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
        mainActivity.scannedApFlow.observe(viewLifecycleOwner) {
            adapter.applyNewData(it)
            blockingState.value = false
        }

        when (navArgs.setupType) {
            SETUP_POWER -> setupPower()
            SETUP_SAMPLE -> setupSample()
        }

    }

    private fun setupPower() {
        binding.btnViewRegistered.setOnClickListener {
            findNavController().navigate(SetupFragmentDirections.toRegisteredList())
        }
    }

    private fun setupSample() {
        binding.apply {
            btnSaveSample.setOnClickListener {
                PositionSample.createSample(
                    uid = System.currentTimeMillis(),
                    sampleName = edtSample.text.toString(),
                    aps = adapter.getData()
                        .sortedBy { -it.rssi }
                        .take(6),
                ).let {
                    saveSample(it) {
                        toast(requireContext(), "Save sample ${it.name} into database")
                    }
                }
            }
            btnViewRegistered.setOnClickListener {
                findNavController().navigate(SetupFragmentDirections.toRegisteredSamples())
            }
            layoutSample.visibility = View.VISIBLE
        }
    }

    private fun saveSample(sample: PositionSample, callback: () -> Unit) {
        lifecycleScope.launch {
            sampleDao.insertAll(sample)
            callback()
        }
    }
}