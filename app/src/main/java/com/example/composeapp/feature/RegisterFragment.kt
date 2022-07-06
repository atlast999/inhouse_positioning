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
import com.example.composeapp.databinding.RegisterFragmentBinding
import com.example.composeapp.model.ApDao
import com.example.composeapp.model.ApPositionInfo
import com.example.composeapp.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {

    companion object {
        const val ACTION_REGISTER = "ACTION_REGISTER"
        const val ACTION_EDIT = "ACTION_EDIT"
    }

    private lateinit var binding: RegisterFragmentBinding
    private lateinit var apDao: ApDao

    private val navArgs by navArgs<RegisterFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        apDao = AppDatabase.getInstance(requireContext()).apDao()
        binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (navArgs.action) {
            ACTION_REGISTER -> setUpForRegistering()
            ACTION_EDIT -> setUpForEditing()
        }

    }

    private fun setUpForRegistering() {
        val ap = navArgs.scannedAp!!
        binding.apply {
            edtName.setText(ap.name)
            edtMac.setText(ap.uid)
            edtPro.setText(ap.rssi.toString())
            btnRegister.text = "Register AP"
            btnRegister.setOnClickListener {
                ApPositionInfo(
                    uid = ap.uid,
                    name = edtName.text.toString(),
                    ro = edtRo.text.toString().toInt(),
                    pro = edtPro.text.toString().toInt(),
                    xAxis = edtX.text.toString().toDouble(),
                    yAxis = edtY.text.toString().toDouble(),
                    zAxis = edtZ.text.toString().toDouble(),
                ).let {
                    insertAp(it) {
                        toast(requireContext(), "Register ${it.name} into database")
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    private fun setUpForEditing() {
        val ap = navArgs.registeredAp!!
        binding.apply {
            edtName.setText(ap.name)
            edtMac.setText(ap.uid)
            edtRo.setText(ap.ro.toString())
            edtPro.setText(ap.pro.toString())
            edtX.setText(ap.xAxis.toString())
            edtY.setText(ap.yAxis.toString())
            edtZ.setText(ap.zAxis.toString())
            btnRegister.text = "Update AP"
            btnRegister.setOnClickListener {
                ApPositionInfo(
                    uid = ap.uid,
                    name = edtName.text.toString(),
                    ro = edtRo.text.toString().toInt(),
                    pro = edtPro.text.toString().toInt(),
                    xAxis = edtX.text.toString().toDouble(),
                    yAxis = edtY.text.toString().toDouble(),
                    zAxis = edtZ.text.toString().toDouble(),
                ).let {
                    editAp(it) {
                        toast(requireContext(), "Update ${it.name} in database")
                        findNavController().navigateUp()
                    }
                }
            }
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                deleteAp(ap) {
                    toast(requireContext(), "Delete ${ap.name} in database")
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun insertAp(ap: ApPositionInfo, callback: () -> Unit) {
        lifecycleScope.launch {
            apDao.insertAll(ap)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    private fun editAp(ap: ApPositionInfo, callback: () -> Unit) {
        lifecycleScope.launch {
            apDao.updateAp(ap)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }

    private fun deleteAp(ap: ApPositionInfo, callback: () -> Unit) {
        lifecycleScope.launch {
            apDao.delete(ap)
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
}