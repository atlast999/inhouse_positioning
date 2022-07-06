package com.example.composeapp.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.composeapp.app.AppDatabase
import com.example.composeapp.databinding.RegisteredFragmentBinding
import com.example.composeapp.feature.adapter.RegisteredAdapter
import com.example.composeapp.model.ApDao
import com.example.composeapp.model.ApPositionInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisteredFragment : Fragment() {

    private lateinit var binding: RegisteredFragmentBinding
    private val adapter = RegisteredAdapter().apply {
        itemClickListener = {
            findNavController().navigate(
                RegisteredFragmentDirections.toEdit(
                    null,
                    it,
                    RegisterFragment.ACTION_EDIT
                )
            )
        }
    }
    private lateinit var apDao: ApDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        apDao = AppDatabase.getInstance(requireContext()).apDao()
        binding = RegisteredFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        getAllAp {
            adapter.applyNewData(it)
        }
    }

    private fun getAllAp(callback: (List<ApPositionInfo>) -> Unit) {
        lifecycleScope.launch {
            val aps = apDao.getAll()
            withContext(Dispatchers.Main) {
                callback(aps)
            }
        }
    }
}