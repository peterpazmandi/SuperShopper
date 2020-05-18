package com.inspirecoding.supershopper.fragments

import android.content.Context
import android.graphics.drawable.AnimationDrawable
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
import com.inspirecoding.supershopper.adapter.FriendsListAdapter
import com.inspirecoding.supershopper.databinding.FragmentFriendsBinding
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
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

    private lateinit var peopleLoadingAnimation: AnimationDrawable

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_friends, container, false)

        /** To refresh the list of friends **/
        /** Otherwise the list will be empty **/
        firebaseViewModel.clearLastResultOfFriends()

        _layoutManager.orientation = RecyclerView.VERTICAL

        /** Init cart loading animation **/
        binding.ivPeopleLoading.setBackgroundResource(R.drawable.anim_person_loading)
        peopleLoadingAnimation = binding.ivPeopleLoading.background as AnimationDrawable

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        firebaseViewModel.spinner.observe(viewLifecycleOwner) {show ->
            /** Start cart loading animation **/
            if (show)
            {
                startPeopleLoadingAnimation()
                /** Hide empty cart screen and RecyclerView while loading **/
                showHideEmptyPeople(null)
            }
            else
            {
                stopPeopleLoadingAnimation()
                /** Hide empty cart screen and RecyclerView while loading **/
                showHideEmptyPeople(friendsListAdapter.itemCount)
            }
        }

        firebaseViewModel.currentUserLD.observe(viewLifecycleOwner) { _currentUser ->
            currentUser = _currentUser
            firebaseViewModel.getListOfFriendsAsOwner(currentUser.id).observe(viewLifecycleOwner) { _listOfFriends ->
                loadedFriends += _listOfFriends.size
                friendsListAdapter.addFriends(_listOfFriends)
                showHideEmptyPeople(friendsListAdapter.itemCount)
            }
        }

        binding.rvListOfFriends.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
            {
                if(loadedFriends != currentUser.numberOfFriends)
                {
                    if(!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE)
                    {
                        firebaseViewModel.getListOfFriendsAsOwner(currentUser.id).observe(viewLifecycleOwner) { _listOfFriends ->
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
            friendsListAdapter = FriendsListAdapter(context)
            binding.rvListOfFriends.apply {
                layoutManager = _layoutManager
                adapter = friendsListAdapter
            }
        }
    }

    private fun startPeopleLoadingAnimation()
    {
        binding.ivPeopleLoading.visibility = View.VISIBLE
        peopleLoadingAnimation.start()
    }
    private fun stopPeopleLoadingAnimation()
    {
        binding.ivPeopleLoading.visibility = View.GONE
        peopleLoadingAnimation.stop()
    }
    private fun showHideEmptyPeople(shoppingListsCount: Int?)
    {
        if(shoppingListsCount != null)
        {
            if(shoppingListsCount > 0)
            {
                binding.llEmptyPerson.visibility = View.INVISIBLE
            }
            else
            {
                binding.llEmptyPerson.visibility = View.VISIBLE
            }
        }
        else
        {
            binding.llEmptyPerson.visibility = View.INVISIBLE
        }
    }








}
