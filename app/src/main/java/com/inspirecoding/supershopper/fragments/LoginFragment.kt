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
import com.inspirecoding.supershopper.databinding.FragmentLoginBinding
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.LoginRegisterFragmentViewModel
import org.koin.android.ext.android.inject

private const val TAG = "LoginFragment"
class LoginFragment : Fragment()
{
    private lateinit var binding: FragmentLoginBinding
    private val loginRegisterFragmentViewModel by navGraphViewModels<LoginRegisterFragmentViewModel>(R.id.navigation_graph)
    private val firebaseViewModel: FirebaseViewModel by inject()

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.hide()
    }


    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_login, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.tietLoginEmail.addTextChangedListener {
            binding.tilLoginEmail.error = null
        }
        binding.tietLoginPassword.addTextChangedListener {
            binding.tilLoginPassword.error = null
        }

        binding.btnLoginLogin.setOnClickListener { button ->
            val emailIsValid = loginRegisterFragmentViewModel.validateEmail(binding.tilLoginEmail, binding.tietLoginEmail)
            val passwordIsValid = loginRegisterFragmentViewModel.validatePassword(binding.tilLoginPassword, binding.tietLoginPassword)

            if(emailIsValid && passwordIsValid)
            {
                firebaseViewModel.logInUserFromAuthWithEmailAndPassword(
                    binding.tietLoginEmail.text.toString(),
                    binding.tietLoginPassword.text.toString(),
                    this
                )
            }
        }

        binding.tvLoginFacebook.setOnClickListener {
            (activity as MainActivity).let { activity ->
                firebaseViewModel.signInWithFacebook(activity, this)
            }
        }

        binding.tvLoginGoogle.setOnClickListener {
            (activity as MainActivity).let { activity ->
                firebaseViewModel.signInWithGoogle(activity)
            }
        }

        binding.tvLoginRegisternow.setOnClickListener { button ->
            navigateToRegisterFragment(button)
        }

        firebaseViewModel.toast.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        })
        firebaseViewModel.spinner.observe(viewLifecycleOwner, Observer { show ->
            binding.spinnerLogin.visibility = if (show) View.VISIBLE else View.GONE
        })
    }

    private fun navigateToMainFragment(view: View)
    {
        Log.d(TAG, "navigateToMainFragment")

        val navController: NavController = Navigation.findNavController(view)
        val action = LoginFragmentDirections.actionLoginFragmentToMainFragment()
        navController.navigate(action)
    }
    fun navigateToRegisterFragment(view: View)
    {
        Log.d(TAG, "navigateToRegisterFragment")

        val navController: NavController = Navigation.findNavController(view)
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        navController.navigate(action)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "data: $data")

        firebaseViewModel.onActivityResult(
            requestCode,
            resultCode,
            data,
            this)
    }
}
