package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.FriendsListAdapter
import com.inspirecoding.supershopper.databinding.FragmentFriendsBinding
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

private const val TAG = "FriendsFragment"
class FriendsFragment : Fragment()
{
    private lateinit var binding: FragmentFriendsBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private lateinit var friendsListAdapter: FriendsListAdapter

    private var currentUser = User()
    private var loadedFriends = 0

    private val _layoutManager = LinearLayoutManager(context)

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_friends, container, false)

        // To refresh the list of friends
        // Otherwise the list will be empty
        firebaseViewModel.clearLastResultOfFriends()

        _layoutManager.orientation = RecyclerView.VERTICAL

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        firebaseViewModel.spinner.observe(viewLifecycleOwner) {show ->
            binding.spinnerLoadingFriends.visibility = if (show) View.VISIBLE else View.GONE
        }

        firebaseViewModel.currentUserLD.observe(viewLifecycleOwner) { _currentUser ->
            currentUser = _currentUser
            Log.d(TAG, "6_ ${_currentUser}")
            firebaseViewModel.getFriendsFromFirestore(currentUser.id).observe(viewLifecycleOwner) { _listOfFriends ->
                Log.d(TAG, "7_ ${_listOfFriends}")
                loadedFriends += _listOfFriends.size
                friendsListAdapter.addFriends(_listOfFriends)
            }
        }

        binding.rvListOfFriends.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                Log.d(TAG, "4_ ${loadedFriends}")
                Log.d(TAG, "5_ ${currentUser.numberOfFriends}")
                if(loadedFriends != currentUser.numberOfFriends)
                {
                    if(!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE)
                    {
                        firebaseViewModel.getFriendsFromFirestore(currentUser.id).observe(viewLifecycleOwner) { _listOfFriends ->
                            Log.d(TAG, "2_ ${_listOfFriends}")
                            loadedFriends += _listOfFriends.size
                            friendsListAdapter.addFriends(_listOfFriends)
                        }
                    }
                }
            }
        })
    }

    private fun initRecyclerView()
    {
        context?.let { context ->
            friendsListAdapter = FriendsListAdapter(context, firebaseViewModel)
            binding.rvListOfFriends.apply {
                layoutManager = _layoutManager
                adapter = friendsListAdapter
            }
        }
    }











}
