package com.inspirecoding.supershopper.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.ShoppingList

class MainFragmentViewModel: ViewModel()
{
    var selectedShoppingList = ShoppingList()
    var selectedPosition: Int = -1
}