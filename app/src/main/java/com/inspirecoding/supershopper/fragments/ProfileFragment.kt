package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.navGraphViewModels

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentProfileBinding
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.LoginRegisterFragmentViewModel
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject

private const val TAG = "ProfileFragment"
class ProfileFragment : Fragment()
{
    private lateinit var binding: FragmentProfileBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val loginRegisterFragmentViewModel by navGraphViewModels<LoginRegisterFragmentViewModel>(R.id.navigation_graph)
    private lateinit var currentUser: User

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_profile, container, false)

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        firebaseViewModel.currentUserLD.observe(viewLifecycleOwner) { _currentUser ->
            currentUser = _currentUser
            populateForm(currentUser)
        }
        firebaseViewModel.toast.observe(viewLifecycleOwner) { message ->
            if(message.first)
            {
                Toast.makeText(context, message.second, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveName.setOnClickListener {
            val nameIsValid = loginRegisterFragmentViewModel.validateName(binding.tilName, binding.etName)
            if(nameIsValid)
            {
                currentUser.name = binding.etName.text.toString()
                firebaseViewModel.updateNameOFUserInFirestore(currentUser)
            }
        }
    }


    private fun populateForm(user: User)
    {
        setProfilePictures(user, binding.civProfilePicture)

        binding.etName.setText(user.name)
    }
    private fun setProfilePictures(user: User?, imageView: ImageView)
    {
        user?.let {
            Log.d(TAG, "$user _2")
            if(user.profilePicture.isNotEmpty())
            {
                Log.d(TAG, "$user _3")
                Picasso
                    .get()
                    .load(user.profilePicture)
                    .placeholder(R.drawable.ic_person)
                    .into(imageView)
            }
        }
    }
}
