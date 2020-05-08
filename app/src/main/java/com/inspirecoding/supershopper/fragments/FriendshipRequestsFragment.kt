package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentFriendshipRequestsBinding

class FriendshipRequestsFragment : Fragment()
{
    private lateinit var binding: FragmentFriendshipRequestsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_friendship_requests, container, false)
        return binding.root
    }
}
