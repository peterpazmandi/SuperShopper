package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentSensitiveDataBinding
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.LoginRegisterFragmentViewModel
import org.koin.android.ext.android.inject

class SensitiveDataFragment : Fragment()
{
    private lateinit var binding: FragmentSensitiveDataBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val loginRegisterFragmentViewModel by navGraphViewModels<LoginRegisterFragmentViewModel>(R.id.navigation_graph)

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_sensitive_data, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val safeArgs: SensitiveDataFragmentArgs by navArgs()
        val source = safeArgs.source

        firebaseViewModel.toast.observe(viewLifecycleOwner) { message ->
            if(message.first)
            {
                Toast.makeText(context, message.second, Toast.LENGTH_SHORT).show()
            }
        }
        firebaseViewModel.securityCheck.observe(viewLifecycleOwner) { result ->
            if(result.first)
            {
                findNavController().navigate(R.id.action_sensitiveDataFragment_to_profileFragment)
            }
        }

        binding.btnSignOut.setOnClickListener { button ->
            firebaseViewModel.securityCheckDone(source)
            firebaseViewModel.logOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
//                firebaseViewModel.reAuthenticateUser(
//                    binding.tietLoginEmail.text.toString(),
//                    binding.tietLoginPassword.text.toString(),
//                    source
//                )
        }
    }
}
