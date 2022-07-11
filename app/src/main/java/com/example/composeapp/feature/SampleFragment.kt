package com.example.composeapp.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.composeapp.app.AppDatabase
import com.example.composeapp.databinding.SampleFragmentBinding
import com.example.composeapp.feature.adapter.SampleAdapter
import com.example.composeapp.model.PositionSample
import com.example.composeapp.model.SampleDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SampleFragment : Fragment() {

    private lateinit var binding: SampleFragmentBinding
    private lateinit var sampleDao: SampleDao
    private val adapter = SampleAdapter().apply {
        itemClickListener = {
            findNavController().navigate(
                SampleFragmentDirections.toDetail(it)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sampleDao = AppDatabase.getInstance(requireContext()).sampleDao()
        binding = SampleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recSamples.adapter = adapter
        getAllSamples {
            adapter.applyNewData(it)
        }
    }

    private fun getAllSamples(callback: (List<PositionSample>) -> Unit) {
        lifecycleScope.launch {
            val aps = sampleDao.getAll()
            withContext(Dispatchers.Main) {
                callback(aps)
            }
        }
    }
}