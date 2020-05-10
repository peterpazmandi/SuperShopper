package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.OpenBoughtItemsAdapter
import com.inspirecoding.supershopper.databinding.FragmentOpenBoughtBinding
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.ShoppingListFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


private const val TAG = "OpenBoughtFragment"
class OpenBoughtFragment : Fragment()
{
    private lateinit var binding: FragmentOpenBoughtBinding
    private val shoppingListFragmentViewModel by navGraphViewModels<ShoppingListFragmentViewModel>(R.id.navigation_graph)
    private val firebaseViewModel: FirebaseViewModel by inject()
    private lateinit var openItemsAdapter: OpenBoughtItemsAdapter
    private lateinit var boughtItemsAdapter: OpenBoughtItemsAdapter

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_open_bought, container, false)

        context?.let { context ->
            openItemsAdapter = OpenBoughtItemsAdapter(context)
            binding.rvOpenItemsList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = openItemsAdapter
            }
            boughtItemsAdapter = OpenBoughtItemsAdapter(context)
            binding.rvBoughtItemsList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = boughtItemsAdapter
            }
        }


        shoppingListFragmentViewModel.openItemsLD.observe(viewLifecycleOwner) { listOfOpenItems ->
            firebaseViewModel.viewModelScope.launch (Dispatchers.Main) {
                if(listOfOpenItems.count() == 0)
                {
                    binding.tvOpenItems.visibility = View.GONE
                }
                else
                {
                    binding.tvOpenItems.visibility = View.VISIBLE
                }
                openItemsAdapter.addAllItem(listOfOpenItems)
            }
        }
        shoppingListFragmentViewModel.boughtItemsLD.observe(viewLifecycleOwner) { listOfBoughtItems ->
            firebaseViewModel.viewModelScope.launch (Dispatchers.Main) {
                if(listOfBoughtItems.count() == 0)
                {
                    binding.tvBoughtItems.visibility = View.GONE
                }
                else
                {
                    binding.tvBoughtItems.visibility = View.VISIBLE
                }
                boughtItemsAdapter.addAllItem(listOfBoughtItems)
            }
        }
        Log.d(TAG, "1_ ${shoppingListFragmentViewModel.openedShoppingList}")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "opened shoppingList: ${shoppingListFragmentViewModel.openedShoppingList}")
        firebaseViewModel.getShoppingListRealTime(shoppingListFragmentViewModel.openedShoppingList.shoppingListId).observe(viewLifecycleOwner) { _shoppingList ->
            shoppingListFragmentViewModel.openedShoppingList = _shoppingList
            Log.d(TAG, "$_shoppingList")
            updateSelectedShoppingListItems(_shoppingList.listOfItems)
        }

        openItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                Log.d(TAG, "1_ ${shoppingListFragmentViewModel.openedShoppingList}, $listItem")
                val indexOfItem = shoppingListFragmentViewModel.openedShoppingList.listOfItems.indexOf(listItem)
                Log.d(TAG, "2_ $indexOfItem, ${shoppingListFragmentViewModel.openedShoppingList}, $listItem")
                listItem.isBought = isChecked
                shoppingListFragmentViewModel.openedShoppingList.listOfItems[indexOfItem] = listItem
                firebaseViewModel.updateShoppingList(shoppingListFragmentViewModel.openedShoppingList, this@OpenBoughtFragment)
            }
        })
        boughtItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                Log.d(TAG, "3_ ${shoppingListFragmentViewModel.openedShoppingList}, $listItem")
                val indexOfItem = shoppingListFragmentViewModel.openedShoppingList.listOfItems.indexOf(listItem)
                listItem.isBought = isChecked
                Log.d(TAG, "4_ $indexOfItem, ${shoppingListFragmentViewModel.openedShoppingList}, $listItem")
                shoppingListFragmentViewModel.openedShoppingList.listOfItems[indexOfItem] = listItem
                firebaseViewModel.updateShoppingList(shoppingListFragmentViewModel.openedShoppingList, this@OpenBoughtFragment)
            }
        })
    }


    private fun updateSelectedShoppingListItems(listOfItems: MutableList<ListItem>)
    {
        val openItems = listOfItems.filter { !it.isBought }
        shoppingListFragmentViewModel.updateOpenItems(openItems.toMutableList())

        val boughtItems = listOfItems.filter { it.isBought }
        shoppingListFragmentViewModel.updateBoughtItems(boughtItems.toMutableList())
    }
}
