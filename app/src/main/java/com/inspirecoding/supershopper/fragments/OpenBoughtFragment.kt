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
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.MainFragmentViewModel
import com.inspirecoding.supershopper.viewmodels.ShoppingListFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


private const val TAG = "OpenBoughtFragment"
class OpenBoughtFragment : Fragment()
{
    private lateinit var binding: FragmentOpenBoughtBinding
    private val shoppingListFragmentViewModel by navGraphViewModels<ShoppingListFragmentViewModel>(R.id.navigation_graph)
    private val mainFragmentViewModel by navGraphViewModels<MainFragmentViewModel>(R.id.navigation_graph)
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

        firebaseViewModel.getShoppingListRealTime(shoppingListFragmentViewModel.openedShoppingList.shoppingListId).observe(viewLifecycleOwner) { _shoppingList ->
            shoppingListFragmentViewModel.openedShoppingList = _shoppingList
            Log.d(TAG, "$_shoppingList")
            updateSelectedShoppingListItems(_shoppingList.listOfItems)

            /** Update the local temporary full list of shopping lists **/
            updateTemporaryFullShoppingLists()

            setListEnabledRegardingStatus(_shoppingList.shoppingListStatus)
        }

        openItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                val indexOfItem = getIndexOfItem(listItem, shoppingListFragmentViewModel.openedShoppingList.listOfItems)
                shoppingListFragmentViewModel.openedShoppingList.listOfItems[indexOfItem].isBought = isChecked

                shoppingListFragmentViewModel.openedShoppingList.shoppingListStatus = setShoppingListStatus(shoppingListFragmentViewModel.openedShoppingList.listOfItems)
                firebaseViewModel.updateShoppingList(shoppingListFragmentViewModel.openedShoppingList)

                /** Update the local temporary full list of shopping lists **/
                updateTemporaryFullShoppingLists()
            }
        })
        boughtItemsAdapter.setOnItemClickListener(object : OpenBoughtItemsAdapter.OnItemClickListener {
            override fun onItemSelected(listItem: ListItem, isChecked: Boolean)
            {
                val indexOfItem = getIndexOfItem(listItem, shoppingListFragmentViewModel.openedShoppingList.listOfItems)
                shoppingListFragmentViewModel.openedShoppingList.listOfItems[indexOfItem].isBought = isChecked

                shoppingListFragmentViewModel.openedShoppingList.shoppingListStatus = setShoppingListStatus(shoppingListFragmentViewModel.openedShoppingList.listOfItems)
                firebaseViewModel.updateShoppingList(shoppingListFragmentViewModel.openedShoppingList)

                /** Update the local temporary full list of shopping lists **/
                updateTemporaryFullShoppingLists()
            }
        })
    }

    private fun updateTemporaryFullShoppingLists()
    {
        val positionToUpdate = mainFragmentViewModel.fullListOfShoppingLists.indexOfFirst {
            it.shoppingListId == (shoppingListFragmentViewModel.openedShoppingList).shoppingListId
        }
        if(positionToUpdate != -1)
        {
            mainFragmentViewModel.fullListOfShoppingLists[positionToUpdate] = shoppingListFragmentViewModel.openedShoppingList
        }
    }

    private fun getIndexOfItem(listItem: ListItem, listOfItems: MutableList<ListItem>) = listOfItems.indexOfFirst { it.id == listItem.id }

    private fun setShoppingListStatus(listOfItems: MutableList<ListItem>): ShoppingListStatus
    {
        return when(listOfItems.filter { !it.isBought }.count())
        {
            0 -> ShoppingListStatus.DONE
            else -> ShoppingListStatus.OPEN
        }
    }
    private fun setListEnabledRegardingStatus(shoppingListStatus: ShoppingListStatus)
    {
        when(shoppingListStatus)
        {
            ShoppingListStatus.OPEN -> {
                openItemsAdapter.setShoppingListStatus(ShoppingListStatus.OPEN)
                boughtItemsAdapter.setShoppingListStatus(ShoppingListStatus.OPEN)
            }
            ShoppingListStatus.DONE -> {
                openItemsAdapter.setShoppingListStatus(ShoppingListStatus.DONE)
                boughtItemsAdapter.setShoppingListStatus(ShoppingListStatus.DONE)
            }
            ShoppingListStatus.CLOSED -> {
                openItemsAdapter.setShoppingListStatus(ShoppingListStatus.CLOSED)
                boughtItemsAdapter.setShoppingListStatus(ShoppingListStatus.CLOSED)
            }
        }
    }
    private fun updateSelectedShoppingListItems(listOfItems: MutableList<ListItem>)
    {
        val openItems = listOfItems.filter { !it.isBought }
        shoppingListFragmentViewModel.updateOpenItems(openItems.toMutableList())

        val boughtItems = listOfItems.filter { it.isBought }
        shoppingListFragmentViewModel.updateBoughtItems(boughtItems.toMutableList())
    }
}
