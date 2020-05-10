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
import com.inspirecoding.supershopper.databinding.FragmentDeleteDialogBinding
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.ShoppingListFragmentViewModel
import org.koin.android.ext.android.inject

class DeleteDialog : Fragment()
{
    private lateinit var binding: FragmentDeleteDialogBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val shoppingListFragmentViewModel by navGraphViewModels<ShoppingListFragmentViewModel>(R.id.navigation_graph)

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_delete_dialog, container, false)

        val safeArgs: DeleteDialogArgs by navArgs()
        val shoppingList = safeArgs.shoppingList

        binding.btnCancel.setOnClickListener { view ->
            navigateToShoppingListFragment(view, shoppingList)
        }
        binding.btnDelete.setOnClickListener { view ->
            firebaseViewModel.deleteShoppingList(shoppingList.shoppingListId)
            shoppingListFragmentViewModel.openedShoppingList.shoppingListId = ""
            navigateToMainFragment(view)
        }

        return binding.root
    }

    private fun navigateToShoppingListFragment(view: View, shoppingList: ShoppingList)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = DeleteDialogDirections.actionDeleteDialogToShoppingListFragment(shoppingList)
        navController.navigate(action)
    }
    private fun navigateToMainFragment(view: View)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = DeleteDialogDirections.actionDeleteDialogToMainFragment()
        navController.navigate(action)
    }
}
