package com.inspirecoding.supershopper.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.inspirecoding.supershopper.fragments.LoginFragmentDirections

private const val TAG = "LoginRegisterFragmentViewModel"
class LoginRegisterFragmentViewModel: ViewModel()
{
    fun validateName(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText): Boolean
    {
        Log.d(TAG, "validateName")

        val name = textInputEditText.text.toString().trim()

        return if(name.length < 6)
        {
            textInputLayout.error = "Use at least 5 characters"
            false
        }
        else
        {
            textInputLayout.error = null
            true
        }
    }
    fun validateEmail(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText): Boolean
    {
        Log.d(TAG, "validateEmail")

        val email = textInputEditText.text.toString().trim()

        return if(!email.contains("@") && !email.contains("."))
        {
            textInputLayout.error = "Enter a valid email"
            false
        }
        else if (email.length < 6)
        {
            textInputLayout.error = "Use at least 5 characters"
            false
        }
        else
        {
            textInputLayout.error = null
            true
        }
    }
    fun validatePassword(textInputLayout: TextInputLayout, textInputEditText: TextInputEditText): Boolean
    {
        Log.d(TAG, "validatePassword")

        val password = textInputEditText.text.toString().trim()

        return if(password.length < 6)
        {
            textInputLayout.error = "Use at least 5 characters"
            false
        }
        else
        {
            textInputLayout.error = null
            true
        }
    }
}