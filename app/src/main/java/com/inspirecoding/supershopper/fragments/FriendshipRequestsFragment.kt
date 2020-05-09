package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.ReceivedFriendRequestsAdapter
import com.inspirecoding.supershopper.databinding.FragmentFriendshipRequestsBinding
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import org.koin.android.ext.android.inject

private const val TAG = "FriendshipReqFragment"
class FriendshipRequestsFragment : Fragment()
{
    private lateinit var binding: FragmentFriendshipRequestsBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private lateinit var receivedFriendRequestsAdapter: ReceivedFriendRequestsAdapter

    private val _layoutManager = LinearLayoutManager(context)

    private var currentUser = User()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_friendship_requests, container, false)

        // To refresh the list of friends
        // Otherwise the list will be empty
        firebaseViewModel.clearLastResultOfFriendsRequests()

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
            firebaseViewModel.getReceiverFriendRequest(currentUser.id).observe(viewLifecycleOwner) { _listOfFriendRequests ->
                Log.d(TAG, "7_ ${_listOfFriendRequests}")
                receivedFriendRequestsAdapter.addRequests(_listOfFriendRequests)
            }
        }

        binding.rvListOfFriendsRequests.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                if(!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    firebaseViewModel.getReceiverFriendRequest(currentUser.id).observe(viewLifecycleOwner) { _listOfFriendRequests ->
                        Log.d(TAG, "2_ ${_listOfFriendRequests}")
                        receivedFriendRequestsAdapter.addRequests(_listOfFriendRequests)
                    }
                }
            }
        })
    }



    private fun initRecyclerView()
    {
        context?.let { context ->
            receivedFriendRequestsAdapter = ReceivedFriendRequestsAdapter(context)
            binding.rvListOfFriendsRequests.apply {
                layoutManager = _layoutManager
                adapter = receivedFriendRequestsAdapter
            }
        }
    }
}
