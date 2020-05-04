package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.navGraphViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentChangeEmailBinding
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.LoginRegisterFragmentViewModel
import org.koin.android.ext.android.inject

class ChangeEmailFragment : BottomSheetDialogFragment()
{
    private lateinit var binding: FragmentChangeEmailBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val loginRegisterFragmentViewModel by navGraphViewModels<LoginRegisterFragmentViewModel>(R.id.navigation_graph)

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_change_email, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.btnChangeEmail.setOnClickListener {
            val emailIsValid = loginRegisterFragmentViewModel.validateEmail(binding.tilNewEmail, binding.tietNewEmail)

            if(emailIsValid)
            {
                firebaseViewModel.updateEmail(binding.tietNewEmail.text.toString())
                dismiss()
            }
        }
    }
}
