package com.inspirecoding.supershopper.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.inspirecoding.supershopper.fragments.FriendsFragment
import com.inspirecoding.supershopper.fragments.FriendshipRequestsFragment

class FriendsRequestsViewPagerAdapter (activity: AppCompatActivity, private var itemCount: Int): FragmentStateAdapter(activity)
{
    override fun getItemCount() = itemCount

    override fun createFragment(position: Int): Fragment
    {
        return when(position)
        {
            0 -> FriendsFragment()
            1 -> FriendshipRequestsFragment()
            else -> FriendsFragment()
        }
    }
}