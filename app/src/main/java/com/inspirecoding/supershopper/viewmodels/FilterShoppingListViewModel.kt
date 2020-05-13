package com.inspirecoding.supershopper.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.model.ShoppingList
import org.joda.time.DateTime
import androidx.core.util.Pair
import com.inspirecoding.supershopper.utilities.ConverterFunctions
import java.util.*

private const val TAG = "FilterViewModel"
class FilterShoppingListViewModel: ViewModel()
{
    /** FILTERS **/
    var listOfShoppingListStatus = mutableListOf<ShoppingListStatus>()
    var name: String? = null
    var fromToDueDate: Pair<Date, Date>? = null
    var fromToDueDate_temp: Pair<Date, Date>? = null
    var idsListOffriendsSharedWith = mutableListOf<String>()

    lateinit var onFilterChangedClickListener: OnFilterChangedClickListener
    interface OnFilterChangedClickListener
    {
        fun onFilterChanged()
    }
    fun setFilterChangedClickListener(onFilterChangedClickListener: OnFilterChangedClickListener)
    {
        this.onFilterChangedClickListener = onFilterChangedClickListener
    }

    fun runFilter(listOfShoppingLists: MutableList<ShoppingList>): MutableList<ShoppingList>
    {
        val filteredShoppingLists = mutableListOf<ShoppingList>()

        for (shoppingList in listOfShoppingLists)
        {
            if(containsName(shoppingList) &&
                containsShoppingListState(shoppingList) &&
                isDueDateInTimePeriod(shoppingList))
            {
                filteredShoppingLists.add(shoppingList)
                Log.d(TAG, "filteredShoppingLists: ${filteredShoppingLists}")
            }
        }

        return filteredShoppingLists
    }

    fun setShoppingListFilter(isOpenChecked: Boolean, isDoneChecked: Boolean, isClosedChecked: Boolean)
    {
        listOfShoppingListStatus.clear()
        if (isOpenChecked)
        {
            listOfShoppingListStatus.add(ShoppingListStatus.OPEN)
        }
        if (isDoneChecked)
        {
            listOfShoppingListStatus.add(ShoppingListStatus.DONE)
        }
        if (isClosedChecked)
        {
            listOfShoppingListStatus.add(ShoppingListStatus.CLOSED)
        }
    }
    fun setNameFilter(name: String)
    {
        this.name = name
    }
    fun setDueDateFilter(fromToDueDate: Pair<Long, Long>)
    {
        fromToDueDate.first?.let { _fromDate ->
            fromToDueDate.second?.let {  _toDate ->
                this.fromToDueDate = Pair(ConverterFunctions.convertToDate(_fromDate), ConverterFunctions.convertToDate(_toDate))
            }
        }
    }
    fun setTempDueDate(fromToDueDate: Pair<Long, Long>)
    {
        fromToDueDate.first?.let { _fromDate ->
            fromToDueDate.second?.let {  _toDate ->
                this.fromToDueDate_temp = Pair(ConverterFunctions.convertToDate(_fromDate), ConverterFunctions.convertToDate(_toDate))
            }
        }
    }

    private fun containsName(shoppingList: ShoppingList): Boolean
    {
        return if (!name.isNullOrEmpty())
        {
            shoppingList.name.contains(name as String)
        }
        else
        {
            true
        }
    }
    private fun containsShoppingListState(shoppingList: ShoppingList): Boolean
    {
        return if(listOfShoppingListStatus.size != 0)
        {
            listOfShoppingListStatus.contains(shoppingList.shoppingListStatus)
        }
        else
        {
            true
        }
    }
    private fun isDueDateInTimePeriod(shoppingList: ShoppingList): Boolean
    {
        return if(fromToDueDate != null)
        {
            val fromDate = (fromToDueDate as Pair).first as Date
            val toDate = (fromToDueDate as Pair).second as Date
            val timePeriod = fromDate .. toDate

            shoppingList.dueDate in timePeriod
        }
        else
        {
            true
        }
    }



}