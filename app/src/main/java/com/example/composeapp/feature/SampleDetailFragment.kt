package com.example.composeapp.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.composeapp.app.AppDatabase
import com.example.composeapp.databinding.SampleDetailFragmentBinding
import com.example.composeapp.feature.adapter.ScannedAdapter
import com.example.composeapp.model.PositionSample
import com.example.composeapp.model.SampleDao
import com.example.composeapp.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SampleDetailFragment : Fragment() {
    private val navArgs: SampleDetailFragmentArgs by navArgs()
    private lateinit var binding: SampleDetailFragmentBinding
    private lateinit var sampleDao: SampleDao
    private val adapter = ScannedAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sampleDao = AppDatabase.getInstance(requireContext()).sampleDao()
        binding = SampleDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recAps.adapter = adapter
        navArgs.sample.let { sample ->
            binding.tvRegisteredSample.text = "Signals at ${sample.name}"
            adapter.applyNewData(sample.listAp())
            binding.btnDelete.setOnClickListener {
                deleteSample(sample) {
                    toast(requireContext(), "Delete ${sample.name} in database")
                    findNavController().navigateUp()
                }
            }
        }

    }

    private fun deleteSample(sample: PositionSample, callback: () -> Unit) {
        lifecycleScope.launch {
            sampleDao.delete(sample)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
}