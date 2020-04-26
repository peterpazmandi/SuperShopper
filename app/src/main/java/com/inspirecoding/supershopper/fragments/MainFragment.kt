package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.OpenBoughtItemsAdapter
import com.inspirecoding.supershopper.adapter.ShoppingListAdapter
import com.inspirecoding.supershopper.databinding.FragmentMainBinding
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.MainFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "MainFragment"
class MainFragment : Fragment()
{
    private lateinit var binding: FragmentMainBinding

    private val firebaseViewModel: FirebaseViewModel by inject()
    private val mainFragmentViewModel by navGraphViewModels<MainFragmentViewModel>(R.id.navigation_graph)

    private lateinit var shoppingListAdapter: ShoppingListAdapter
    private lateinit var openItemsAdapter: OpenBoughtItemsAdapter
    private lateinit var boughtItemsAdapter: OpenBoughtItemsAdapter

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)

        context?.let { context ->
            shoppingListAdapter = ShoppingListAdapter(context, firebaseViewModel)
            binding.rvShoppingLists.apply {
                adapter = shoppingListAdapter
            }

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

        mainFragmentViewModel.openItemsLD.observe(viewLifecycleOwner) { listOfOpenItems ->
            firebaseViewModel.viewModelScope.launch (Dispatchers.Main) {
                openItemsAdapter.addAllItem(listOfOpenItems)
            }
        }
        mainFragmentViewModel.boughtItemsLD.observe(viewLifecycleOwner) { listOfBoughtItems ->
            firebaseViewModel.viewModelScope.launch (Dispatchers.Main) {
                boughtItemsAdapter.addAllItem(listOfBoughtItems)
            }
        }

        firebaseViewModel.toast.observe(viewLifecycleOwner) { message ->
//            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.fabCreateNewList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createNewListFragment)
        }

        firebaseViewModel.getCurrentUserShoppingListsRealTime().observe(viewLifecycleOwner) {listOfShoppingLists ->
            Log.d(TAG, "${listOfShoppingLists.size}")
            for(key in listOfShoppingLists.keys)
            {
                when(key.type)
                {
                    DocumentChange.Type.ADDED -> {
                        Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                        val listOfToDos = listOfShoppingLists.get(key)
                        listOfToDos?.let { shoppingListItem ->
                            shoppingListAdapter.addShoppingListItem(shoppingListItem)
                        }
                    }

                    DocumentChange.Type.MODIFIED -> {
                        Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                        val listOfToDos = listOfShoppingLists.get(key)
                        listOfToDos?.let { shoppingList ->
                            val position = shoppingListAdapter.getPositionOfShoppingListItem(shoppingList)
                            shoppingListAdapter.updateShoppingListItem(position, shoppingList)

                            if(shoppingList.id  == mainFragmentViewModel.selectedShoppingList.id)
                            {
                                openShoppingList(shoppingList)
                            }
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                        val listOfToDos = listOfShoppingLists.get(key)
                        listOfToDos?.let { shoppingList ->
                            val position = shoppingListAdapter.getPositionOfShoppingListItem(shoppingList)
                            shoppingListAdapter.removeShoppingListItem(position)

                            if(shoppingList.id  == mainFragmentViewModel.selectedShoppingList.id)
                            {
                                openShoppingList(shoppingList)
                            }
                        }
                    }
                }
            }

            if(shoppingListAdapter.itemCount > 0)
            {
                openShoppingList(shoppingListAdapter.getShoppingListItemFromPosition(0))
            }
        }

        shoppingListAdapter.setOnItemClickListener(object : ShoppingListAdapter.OnItemClickListener
        {
            override fun onItemSelected(shoppingList: ShoppingList)
            {
                openShoppingList(shoppingList)
            }
        })

        openItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                Log.d(TAG, "$listItem, $isChecked")
                listItem.isBought = isChecked
                val indexOfItem = mainFragmentViewModel.selectedShoppingList.listOfItems.indexOf(listItem)
                mainFragmentViewModel.selectedShoppingList.listOfItems[indexOfItem] = listItem
                firebaseViewModel.updateShoppingList(mainFragmentViewModel.selectedShoppingList, this@MainFragment)
            }
        })
        boughtItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                Log.d(TAG, "$listItem, $isChecked")
                listItem.isBought = isChecked
                val indexOfItem = mainFragmentViewModel.selectedShoppingList.listOfItems.indexOf(listItem)
                mainFragmentViewModel.selectedShoppingList.listOfItems[indexOfItem] = listItem
                firebaseViewModel.updateShoppingList(mainFragmentViewModel.selectedShoppingList, this@MainFragment)
            }
        })
    }

    private fun openShoppingList(shoppingList: ShoppingList)
    {
        mainFragmentViewModel.selectedShoppingList = shoppingList

        binding.tvShoppingListName.text = shoppingList.name
        updateSelectedShoppingListItems(shoppingList.listOfItems)
    }
    private fun updateSelectedShoppingListItems(listOfItems: MutableList<ListItem>)
    {
        val openItems = listOfItems.filter { !it.isBought }
        mainFragmentViewModel.updateOpenItems(openItems.toMutableList())

        val boughtItems = listOfItems.filter { it.isBought }
        mainFragmentViewModel.updateBoughtItems(boughtItems.toMutableList())
    }
}
