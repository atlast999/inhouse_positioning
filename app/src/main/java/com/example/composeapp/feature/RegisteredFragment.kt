package com.example.composeapp.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.composeapp.MainActivity
import com.example.composeapp.databinding.RegisteredFragmentBinding

class RegisteredFragment : Fragment() {

    private lateinit var binding: RegisteredFragmentBinding
    private val adapter = CustomAdapter(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisteredFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        (requireActivity() as MainActivity).getListAp().values.toList().let {
            adapter.applyNewData(it)
        }
    }
}