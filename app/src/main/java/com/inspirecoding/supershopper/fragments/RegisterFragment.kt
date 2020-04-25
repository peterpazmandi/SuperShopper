package com.inspirecoding.supershopper.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.inspirecoding.supershopper.MainActivity

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentRegisterBinding
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.LoginRegisterFragmentViewModel
import org.koin.android.ext.android.inject

private const val TAG = "RegisterFragment"
class RegisterFragment : Fragment()
{
    private lateinit var binding: FragmentRegisterBinding
    private val loginRegisterFragmentViewModel by navGraphViewModels<LoginRegisterFragmentViewModel>(R.id.navigation_graph)
    private val firebaseViewModel: FirebaseViewModel by inject()

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_register, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.tietRegisterName.addTextChangedListener {
            binding.tietRegisterName.error = null
        }
        binding.tietRegisterEmail.addTextChangedListener {
            binding.tietRegisterEmail.error = null
        }
        binding.tietRegisterPassword.addTextChangedListener {
            binding.tilRegisterPassword.error = null
        }

        binding.tvRegisterLoginnow.setOnClickListener { view ->
            navigateToLoginFragment(view)
        }

        binding.btnRegisterLogin.setOnClickListener {
            val nameIsValid = loginRegisterFragmentViewModel.validateName(binding.tilRegisterName, binding.tietRegisterName)
            val emailIsValid = loginRegisterFragmentViewModel.validateEmail(binding.tilRegisterEmail, binding.tietRegisterEmail)
            val passwordIsValid = loginRegisterFragmentViewModel.validatePassword(binding.tilRegisterPassword, binding.tietRegisterPassword)

            if(nameIsValid && emailIsValid && passwordIsValid)
            {
                firebaseViewModel.registerUserFromAuthWithEmailAndPassword(
                    binding.tietRegisterName.text.toString(),
                    binding.tietRegisterEmail.text.toString(),
                    binding.tietRegisterPassword.text.toString(),
                    this
                )
            }
        }

        binding.ivRegisterFacebook.setOnClickListener {
            (activity as MainActivity).let { activity ->
                firebaseViewModel.signInWithFacebook(activity, this)
            }
        }

        binding.ivRegisterGoogle.setOnClickListener {
            (activity as MainActivity).let { activity ->
                firebaseViewModel.signInWithGoogle(activity)
            }
        }

        firebaseViewModel.toast.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        })
        firebaseViewModel.spinner.observe(viewLifecycleOwner, Observer { show ->
            binding.spinnerRegister.visibility = if (show) View.VISIBLE else View.GONE
        })
    }

    fun navigateToLoginFragment(view: View)
    {
        Log.d(TAG, "navigateToRegisterFragment")

        val navController: NavController = Navigation.findNavController(view)
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        navController.navigate(action)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        firebaseViewModel.onActivityResult(
            requestCode,
            resultCode,
            data,
            this)
    }
}
