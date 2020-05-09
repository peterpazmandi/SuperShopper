package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.FriendsListAdapter
import com.inspirecoding.supershopper.adapter.UsersListAdapter
import com.inspirecoding.supershopper.databinding.FragmentSearchFriendBinding
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import org.koin.android.ext.android.inject

class SearchFriendFragment : Fragment()
{
    private lateinit var binding: FragmentSearchFriendBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private lateinit var usersListAdapter: UsersListAdapter

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_search_friend, container, false)
        firebaseViewModel.hideSpinner()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

//        initRecyclerView()

        binding.etSearch.addTextChangedListener { _searchText ->
            _searchText?.let { __searchText ->
                if (__searchText.length > 3)
                {
                    firebaseViewModel.getListOfFilteredUsersFromFirestore(__searchText.toString(), 15)
                }
            }
        }

        firebaseViewModel.usersListLD.observe(viewLifecycleOwner) { _listOfFriends ->
            initRecyclerView()
            usersListAdapter.addUsers(_listOfFriends.toMutableList())
        }
        firebaseViewModel.spinner.observe(viewLifecycleOwner) {show ->
            binding.pbLoading.visibility = if (show) View.VISIBLE else View.GONE
        }
    }


    private fun initRecyclerView()
    {
        context?.let { context ->
            usersListAdapter = UsersListAdapter(context, firebaseViewModel)
            binding.rvListOfFriends.apply {
                adapter = usersListAdapter
            }
        }
    }
}
