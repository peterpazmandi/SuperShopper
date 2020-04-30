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
                openItemsAdapter.addAllItem(listOfOpenItems)
            }
        }
        shoppingListFragmentViewModel.boughtItemsLD.observe(viewLifecycleOwner) { listOfBoughtItems ->
            firebaseViewModel.viewModelScope.launch (Dispatchers.Main) {
                boughtItemsAdapter.addAllItem(listOfBoughtItems)
            }
        }

        firebaseViewModel.getShoppingListRealTime(shoppingListFragmentViewModel.openedShoppingList.id).observe(viewLifecycleOwner) { listOfShoppingLists ->
            Log.d(TAG, "$listOfShoppingLists")
            for(key in listOfShoppingLists.keys)
            {
                val listOfToDos = listOfShoppingLists[key]
                listOfToDos?.let { shoppingListItem ->
                    updateSelectedShoppingListItems(shoppingListItem.listOfItems)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)


        openItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                Log.d(TAG, "$listItem, $isChecked")
                val indexOfItem = shoppingListFragmentViewModel.openedShoppingList.listOfItems.indexOf(listItem)
                listItem.isBought = isChecked
                shoppingListFragmentViewModel.openedShoppingList.listOfItems[indexOfItem] = listItem
                firebaseViewModel.updateShoppingList(shoppingListFragmentViewModel.openedShoppingList, this@OpenBoughtFragment)
            }
        })
        boughtItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                val indexOfItem = shoppingListFragmentViewModel.openedShoppingList.listOfItems.indexOf(listItem)
                listItem.isBought = isChecked
                Log.d(TAG, "$indexOfItem, ${shoppingListFragmentViewModel.openedShoppingList}, $listItem")
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
