package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.OpenedShoppingListAdapter
import com.inspirecoding.supershopper.databinding.FragmentShoppingListBinding
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.viewmodels.ShoppingListFragmentViewModel
import org.koin.android.ext.android.bind


private const val TAG = "ShoppingListFragment"
class ShoppingListFragment : Fragment()
{
    private lateinit var binding: FragmentShoppingListBinding
    private lateinit var openedShoppingListAdapter: OpenedShoppingListAdapter
    private val shoppingListFragmentViewModel by navGraphViewModels<ShoppingListFragmentViewModel>(R.id.navigation_graph)

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

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        setHasOptionsMenu(true)

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
        Log.d(TAG, "shoppingList: $shoppingList")
        shoppingListFragmentViewModel.openedShoppingList = shoppingList
        Log.d(TAG, "openedShoppingList: ${shoppingListFragmentViewModel.openedShoppingList}")

        binding.tlItemsDetails.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener
        {
            override fun onTabReselected(tabItem: TabLayout.Tab?) {}
            override fun onTabUnselected(tabItem: TabLayout.Tab?) {}
            override fun onTabSelected(tabItem: TabLayout.Tab) {
                binding.vpItemDetails.currentItem = tabItem.position
            }
        })
    }

    private fun navigateToCreateNewList(view: View, shoppingList: ShoppingList? = null)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = ShoppingListFragmentDirections.actionShoppingListFragmentToAddNewItemDialog(shoppingList)
        navController.navigate(action)
    }
    private fun navigateToDeleteListDialog(view: View, shoppingList: ShoppingList)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = ShoppingListFragmentDirections.actionShoppingListFragmentToDeleteDialog(shoppingList)
        navController.navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_edit_delete_shoppinglist, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId)
        {
            R.id.item_delete -> {
                navigateToDeleteListDialog(binding.root, shoppingListFragmentViewModel.openedShoppingList)
                true
            }
            R.id.item_edit -> {
                navigateToCreateNewList(binding.root, shoppingListFragmentViewModel.openedShoppingList)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
