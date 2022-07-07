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
import com.example.composeapp.databinding.SetupFragmentBinding
import com.example.composeapp.feature.adapter.ScannedAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupFragment : Fragment() {
    private lateinit var binding: SetupFragmentBinding
    private val blockingState = MutableStateFlow(false)
    private val adapter = ScannedAdapter().apply {
        itemClickListener = {
            findNavController().navigate(
                SetupFragmentDirections.toRegister(
                    it,
                    null,
                    RegisterFragment.ACTION_REGISTER
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

    }
}