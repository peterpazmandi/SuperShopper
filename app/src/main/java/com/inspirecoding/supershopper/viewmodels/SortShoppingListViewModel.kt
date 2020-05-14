package com.inspirecoding.supershopper.viewmodels

import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.model.ShoppingList

class SortShoppingListViewModel: ViewModel()
{
    fun getPositionsForShoppingListOrderingByDueDate(newElement: ShoppingList, listOfShoppingLists: List<ShoppingList>): Int
    {
        var position: Int = 0
        for(shoppingList in listOfShoppingLists)
        {
            if(shoppingList.dueDate > newElement.dueDate)
            {
                 position++
            }
        }

        return position
    }


}