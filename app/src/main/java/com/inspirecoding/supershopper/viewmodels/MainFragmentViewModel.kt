package com.inspirecoding.supershopper.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.model.ShoppingList

class MainFragmentViewModel: ViewModel()
{
    val fullListOfShoppingLists = mutableListOf<ShoppingList>()

    private val _mapOfShoppingListOperation = MutableLiveData<MutableMap<DocumentChange, ShoppingList>>()
    val mapOfShoppingListOperation: LiveData<MutableMap<DocumentChange, ShoppingList>>
        get() = _mapOfShoppingListOperation

    fun addShoppingList(intoPosition: Int, shoppingList: ShoppingList)
    {
        val findShoppingList = fullListOfShoppingLists.find {
            it.shoppingListId == shoppingList.shoppingListId
        }
        if (findShoppingList == null)
        {
            fullListOfShoppingLists.add(intoPosition, shoppingList)
        }
    }
}