package com.inspirecoding.supershopper.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.model.ShoppingList

class MainFragmentViewModel: ViewModel()
{
    val fullListOfShoppingLists = mutableListOf<ShoppingList>()

    var numberOfReceivedFriendRequests = 0

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