package com.inspirecoding.supershopper.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.firebase.auth.FirebaseUser

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentSplashBinding
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.repository.SharedPreferencesViewModel
import com.inspirecoding.supershopper.viewmodels.FilterShoppingListViewModel
import com.inspirecoding.supershopper.viewmodels.SortShoppingListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "SplashScreen"
class SplashFragment : Fragment()
{
    private lateinit var binding: FragmentSplashBinding

    private val firebaseViewModel: FirebaseViewModel by inject()
    private val sharedPreferencesViewModel: SharedPreferencesViewModel by inject()
    private val filterShoppingListViewModel by navGraphViewModels<FilterShoppingListViewModel>(R.id.navigation_graph)

    private var currentFirebaseUser: FirebaseUser? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_splash, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        coroutineScope.launch {
            context?.let { _context ->
                sharedPreferencesViewModel.initFilterSharedPreferences(_context.getSharedPreferences("Filters", Context.MODE_PRIVATE))
                uploadFilters()
            }

            currentFirebaseUser = firebaseViewModel.checkUserLoggedIn()
            delay(2_000)
            if (currentFirebaseUser == null)
            {
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
            else
            {
                currentFirebaseUser?.let {firebaseUser ->
                    Log.i(TAG, firebaseUser.uid)
                    firebaseViewModel.setCurrentUser(firebaseUser.uid, this@SplashFragment)
                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                }
            }
        }
    }




    private fun uploadFilters()
    {
        /** Shopping list status **/
        sharedPreferencesViewModel.getStatusFilter()?.let {
            filterShoppingListViewModel.listOfShoppingListStatus = it
        }
        /** Name **/
        filterShoppingListViewModel.name = sharedPreferencesViewModel.getNameFilter()
        /** Friends share with **/
        sharedPreferencesViewModel.getFriendsFilter()?.let {
            filterShoppingListViewModel.listOfFriendsIds = it
        }
        /** Due date from + Due date to **/
        if (sharedPreferencesViewModel.getDueDateFromFilter() != null &&
            sharedPreferencesViewModel.getDueDateToFilter() != null)
        {
            filterShoppingListViewModel.fromToDueDate =
                Pair(sharedPreferencesViewModel.getDueDateFromFilter()!!, sharedPreferencesViewModel.getDueDateToFilter()!!)
        }
    }
}
