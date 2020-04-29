package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.OpenedShoppingListAdapter
import com.inspirecoding.supershopper.databinding.FragmentShoppingListBinding
import com.inspirecoding.supershopper.model.ShoppingList


private const val TAG = "ShoppingListFragment"
class ShoppingListFragment : Fragment()
{
    private lateinit var binding: FragmentShoppingListBinding
    private lateinit var openedShoppingListAdapter: OpenedShoppingListAdapter

    private val TAB_ITEMS_COUNT = 2

    private lateinit var shoppingList: ShoppingList

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_shopping_list, container, false)
        Log.d(TAG, "onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        binding.tlItemsDetails.tabGravity = TabLayout.GRAVITY_FILL
        binding.tlItemsDetails.addTab(binding.tlItemsDetails.newTab().setText(getString(R.string.items)))
        binding.tlItemsDetails.addTab(binding.tlItemsDetails.newTab().setText(getString(R.string.details)))

        openedShoppingListAdapter = OpenedShoppingListAdapter((activity as AppCompatActivity), TAB_ITEMS_COUNT)
        binding.vpItemDetails.adapter = openedShoppingListAdapter

        TabLayoutMediator(binding.tlItemsDetails, binding.vpItemDetails) { tab, position ->
            when(position)
            {
                0 -> tab.text = getString(R.string.items)
                1 -> tab.text = getString(R.string.details)
            }
        }.attach()

        val safeArgs: ShoppingListFragmentArgs by navArgs()
        shoppingList = safeArgs.shoppingList
        (activity as AppCompatActivity).supportActionBar?.title = shoppingList.name

        binding.tlItemsDetails.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener
        {
            override fun onTabReselected(tabItem: TabLayout.Tab?) {}
            override fun onTabUnselected(tabItem: TabLayout.Tab?) {}
            override fun onTabSelected(tabItem: TabLayout.Tab) {
                binding.vpItemDetails.currentItem = tabItem.position
            }
        })

        Log.d(TAG, "${binding.tlItemsDetails.tabCount}")
    }

    override fun onResume()
    {
        super.onResume()
        Log.d(TAG, "onResume")

        addPagerFragments()
        binding.vpItemDetails.adapter = openedShoppingListAdapter
    }

    private fun addPagerFragments()
    {
        openedShoppingListAdapter.addFragments(DetailsFragment())
        openedShoppingListAdapter.addFragments(OpenBoughtFragment())
    }
}
