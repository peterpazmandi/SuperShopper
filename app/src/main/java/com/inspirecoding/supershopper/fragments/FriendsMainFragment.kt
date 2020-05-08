package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.FriendsRequestsViewPagerAdapter
import com.inspirecoding.supershopper.databinding.FragmentFriendsMainBinding

class FriendsMainFragment : Fragment()
{
    private lateinit var binding: FragmentFriendsMainBinding
    private lateinit var friendsRequestsViewPagerAdapter: FriendsRequestsViewPagerAdapter

    private val TAB_ITEMS_COUNT = 2


    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_friends_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        setHasOptionsMenu(true)

        binding.tlFriendsRequests.tabGravity = TabLayout.GRAVITY_FILL
        binding.tlFriendsRequests.addTab(binding.tlFriendsRequests.newTab().setText(getString(R.string.items)))
        binding.tlFriendsRequests.addTab(binding.tlFriendsRequests.newTab().setText(getString(R.string.details)))

        friendsRequestsViewPagerAdapter = FriendsRequestsViewPagerAdapter((activity as AppCompatActivity), TAB_ITEMS_COUNT)
        binding.vpFriendsRequests.adapter = friendsRequestsViewPagerAdapter

        TabLayoutMediator(binding.tlFriendsRequests, binding.vpFriendsRequests) { tab, position ->
            when(position)
            {
                0 -> tab.text = getString(R.string.friends)
                1 -> tab.text = getString(R.string.requests)
            }
        }.attach()

        binding.tlFriendsRequests.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener
        {
            override fun onTabReselected(tabItem: TabLayout.Tab?) {}
            override fun onTabUnselected(tabItem: TabLayout.Tab?) {}
            override fun onTabSelected(tabItem: TabLayout.Tab) {
                binding.vpFriendsRequests.currentItem = tabItem.position
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_search, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId)
        {
            R.id.item_search -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
