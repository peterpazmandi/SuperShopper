package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentCloseDeleteBinding
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.ShoppingListFragmentViewModel
import org.koin.android.ext.android.inject

class CloseDeleteFragment : Fragment()
{
    companion object {
        val CLOSED = "close"
        val DELETE = "delete"
    }

    private lateinit var binding: FragmentCloseDeleteBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val shoppingListFragmentViewModel by navGraphViewModels<ShoppingListFragmentViewModel>(R.id.navigation_graph)

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_close_delete, container, false)

        val safeArgs: CloseDeleteFragmentArgs by navArgs()
        val shoppingList = safeArgs.shoppingList
        val closeDelete = safeArgs.closeDelete

        when (closeDelete)
        {
            CLOSED -> {
                binding.llCloseShoppingList.visibility = View.VISIBLE
                binding.llDeleteShoppingList.visibility = View.INVISIBLE
            }
            DELETE -> {
                binding.llCloseShoppingList.visibility = View.INVISIBLE
                binding.llDeleteShoppingList.visibility = View.VISIBLE
            }
        }

        binding.btnNegative.setOnClickListener { view ->
            navigateToCreateNewListFragment(view, shoppingList)
        }
        binding.btnPositive.setOnClickListener { view ->
            when (closeDelete)
            {
                CLOSED -> {
                    shoppingList.shoppingListStatus = ShoppingListStatus.CLOSED
                    firebaseViewModel.updateShoppingList(shoppingList)
                    shoppingListFragmentViewModel.openedShoppingList.shoppingListId = ""
                    navigateToMainFragment(view)
                }
                DELETE -> {
                    firebaseViewModel.deleteShoppingList(shoppingList.shoppingListId)
                    shoppingListFragmentViewModel.openedShoppingList.shoppingListId = ""
                    navigateToMainFragment(view)
                }
            }
        }

        return binding.root
    }

    private fun navigateToCreateNewListFragment(view: View, shoppingList: ShoppingList)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = CloseDeleteFragmentDirections.actionCloseDeleteDialogToCreateNewListFragment(shoppingList)
        navController.navigate(action)
    }
    private fun navigateToMainFragment(view: View)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = CloseDeleteFragmentDirections.actionDeleteDialogToMainFragment()
        navController.navigate(action)
    }


}
