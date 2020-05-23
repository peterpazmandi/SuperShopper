package com.inspirecoding.supershopper.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.snackbar.Snackbar
import com.inspirecoding.supershopper.MyApp
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentProfileBinding
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.LoginRegisterFragmentViewModel
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import org.koin.android.ext.android.inject

private const val TAG = "ProfileFragment"
class ProfileFragment : Fragment()
{
    private val PROFILE_IMAGE_REQUEST_CODE = 0
    private val PASSWORD = "password"
    private val EMAIL = "email"

    private lateinit var binding: FragmentProfileBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val loginRegisterFragmentViewModel by navGraphViewModels<LoginRegisterFragmentViewModel>(R.id.navigation_graph)
    private lateinit var currentUser: User

    private var isImageCropperRunning = false

    private var snackBar: Snackbar? = null

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
        firebaseViewModel.securityCheck.observe(viewLifecycleOwner) { securityCheck ->
            if (securityCheck.first && securityCheck.second.isNotEmpty())
            {
                if(securityCheck.second.equals(PASSWORD))
                {
                    findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
                    firebaseViewModel.clearSecurityCheck()
                }
                else if (securityCheck.second.equals(EMAIL))
                {
                    findNavController().navigate(R.id.action_profileFragment_to_changeEmailFragment)
                    firebaseViewModel.clearSecurityCheck()
                }
            }
        }

        binding.btnSaveName.setOnClickListener {
            val nameIsValid = loginRegisterFragmentViewModel.validateName(binding.tilName, binding.etName)
            if(nameIsValid)
            {
                currentUser.name = binding.etName.text.toString()
                firebaseViewModel.updateNameOfUserInFirestore(currentUser)

                firebaseViewModel.getAllFriendsAsFriend(currentUser.id).observe(viewLifecycleOwner) { _listOfFriends ->
                    for(friend in _listOfFriends)
                    {
                        firebaseViewModel.updateFriendName(friend.id, currentUser.name)
                    }
                }
            }
        }
        binding.fabChangeProfilePicture.setOnClickListener {
            startImageCropper()
        }
        binding.btnChangePassword.setOnClickListener { _view ->
            firebaseViewModel.startSecurityCheck(PASSWORD)
            navigateToSensitiveDataFragment(_view, PASSWORD)
        }
        binding.btnChangeEmail.setOnClickListener { _view ->
            firebaseViewModel.startSecurityCheck(EMAIL)
            navigateToSensitiveDataFragment(_view, EMAIL)
        }
        binding.btnSignOut.setOnClickListener {
            firebaseViewModel.logOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }



    private fun startImageCropper()
    {
        isImageCropperRunning = true
        val profileImageIntent: Intent = CropImage.activity()
            .setActivityTitle(MyApp.applicationContext().getString(R.string.profile_photo))
            .setAspectRatio(1,1)
            .getIntent(MyApp.applicationContext())
        startActivityForResult(profileImageIntent, PROFILE_IMAGE_REQUEST_CODE)
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
                    .error(R.drawable.profilepicture_blank)
                    .placeholder(R.drawable.profilepicture_blank)
                    .into(imageView)
            }
        }
    }

    private fun navigateToSensitiveDataFragment(view: View, source: String)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = ProfileFragmentDirections.actionProfileFragmentToSensitiveDataFragment(source)
        navController.navigate(action)
    }

    override fun onResume()
    {
        super.onResume()

        snackBar?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        CropImage.getActivityResult(data)?.let { activityResult ->
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            val imageUri: Uri = result.uri

            binding.civProfilePicture.setImageURI(imageUri)

            if(result.uri.path != null && firebaseViewModel.currentUserLD.value != null && isImageCropperRunning)
            {
                isImageCropperRunning = false
                currentUser.profilePicture = (result.uri.path as String)
                firebaseViewModel.updateProfilePictureOfUserInFirestore(currentUser)
            }
        }
    }







}
