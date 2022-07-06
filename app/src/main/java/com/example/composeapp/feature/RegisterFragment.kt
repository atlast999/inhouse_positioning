package com.example.composeapp.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.composeapp.MainActivity
import com.example.composeapp.databinding.RegisterFragmentBinding
import com.example.composeapp.model.AccessPoint

class RegisterFragment : Fragment() {

    private lateinit var binding: RegisterFragmentBinding

    private val navArgs by navArgs<RegisterFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            tvName.text = navArgs.name
            tvMac.text = navArgs.mac
            edtPro.setText(navArgs.rssi.toString())
        }

        binding.btnRegister.setOnClickListener {
            (requireActivity() as MainActivity).registerNewAP(
                AccessPoint(
                    name = navArgs.name,
                    mac = navArgs.mac,
                    ro = binding.edtRo.text.toString().toInt(),
                    pro = binding.edtPro.text.toString().toInt(),
                    xAxis = binding.edtX.text.toString().toDouble(),
                    yAxis = binding.edtY.text.toString().toDouble(),
                    zAxis = binding.edtZ.text.toString().toDouble(),
                )
            )
            findNavController().navigateUp()
        }
    }
}