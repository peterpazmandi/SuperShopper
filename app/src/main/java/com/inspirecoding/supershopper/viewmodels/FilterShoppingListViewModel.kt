package com.inspirecoding.supershopper.viewmodels

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.model.ShoppingList
import androidx.core.util.Pair
import androidx.lifecycle.MutableLiveData
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.repository.SharedPreferencesViewModel
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
    var listOfFriendsIds = mutableListOf<String>()
    var listOfFriendsIds_temp = mutableListOf<String>()
    var numberOfFilters: Int = 0

    lateinit var onFilterChangedClickListener: OnFilterChangedClickListener
    interface OnFilterChangedClickListener
    {
        fun onFilterChanged()
    }
    fun setFilterChangedClickListener(onFilterChangedClickListener: OnFilterChangedClickListener)
    {
        this.onFilterChangedClickListener = onFilterChangedClickListener
    }


    fun isUserAlreadySelected(context: Context, view: View, selectedUserId: String): Boolean
    {
        context.hideKeyboard(view)

        val userOnTheList = listOfFriendsIds_temp.filter {
            it == selectedUserId
        }

        return userOnTheList.count() != 0
    }
    fun removeAlreadyAddedFriends(resultList: MutableList<Friend>): MutableList<Friend>
    {
        val _usersList = resultList.toMutableList()
        val filteredList = _usersList.filter {
            !listOfFriendsIds_temp.contains(it.friendId)
        }
        return filteredList.toMutableList()
    }


    fun runFilter(listOfShoppingLists: MutableList<ShoppingList>): MutableList<ShoppingList>
    {
        val filteredShoppingLists = mutableListOf<ShoppingList>()

        for (shoppingList in listOfShoppingLists)
        {
            if(containsName(shoppingList) &&
                containsShoppingListState(shoppingList) &&
                isDueDateInTimePeriod(shoppingList) &&
                containsFriend(shoppingList))
            {
                filteredShoppingLists.add(shoppingList)
                Log.d(TAG, "filteredShoppingLists: ${filteredShoppingLists}")
            }
        }

        return filteredShoppingLists
    }



    fun setSharePreferencesFilters(sharedPreferencesViewModel: SharedPreferencesViewModel)
    {
        /** Shopping list status **/
        sharedPreferencesViewModel.setStatusFilter(listOfShoppingListStatus)
        /** Name **/
        sharedPreferencesViewModel.setNameFilter(name)
        /** Friends share with **/
        sharedPreferencesViewModel.setFriendsFilter(listOfFriendsIds)
        /** Due date from **/
        sharedPreferencesViewModel.setDueDateFromFilter(fromToDueDate?.first?.time)
        /** Due date to **/
        sharedPreferencesViewModel.setDueDateToFilter(fromToDueDate?.second?.time)
    }



    fun setShoppingListStatusFilter(isOpenChecked: Boolean, isDoneChecked: Boolean, isClosedChecked: Boolean)
    {
        listOfShoppingListStatus.clear()
        if (isOpenChecked)
        {
            listOfShoppingListStatus.add(ShoppingListStatus.OPEN)
            numberOfFilters++
        }
        if (isDoneChecked)
        {
            listOfShoppingListStatus.add(ShoppingListStatus.DONE)
            numberOfFilters++
        }
        if (isClosedChecked)
        {
            listOfShoppingListStatus.add(ShoppingListStatus.CLOSED)
            numberOfFilters++
        }
    }
    fun setNameFilter(name: String)
    {
        if(name.isNotEmpty())
        {
            numberOfFilters++
        }
        this.name = name
    }
    fun setToValidDueDateFilter()
    {
        if(fromToDueDate_temp != null)
        {
            numberOfFilters++
        }
        this.fromToDueDate = fromToDueDate_temp
    }
    fun setDueDate(fromToDueDate: Pair<Date, Date>)
    {
        fromToDueDate.first?.let { _fromDate ->
            fromToDueDate.second?.let {  _toDate ->
                this.fromToDueDate = Pair(_fromDate, _toDate)
                numberOfFilters++
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
    fun setToValidFriendsList()
    {
        if(listOfFriendsIds_temp.size > 0)
        {
            numberOfFilters++
        }
        this.listOfFriendsIds = listOfFriendsIds_temp
    }

    fun clearFilters()
    {
        listOfShoppingListStatus.clear()
        listOfFriendsIds_temp.clear()
        name = null
        listOfFriendsIds.clear()
        listOfFriendsIds_temp.clear()
        fromToDueDate = null
        fromToDueDate_temp = null

        resetNumberOfFilters()
    }

    fun resetNumberOfFilters()
    {
        numberOfFilters = 0
    }

    fun getNumberOfActiveFilters(): Int
    {
        var numberOfActiveFilters = 0

        if (listOfFriendsIds.size > 0)
        {
            numberOfActiveFilters++
        }
        if (name != null && name != "")
        {
            numberOfActiveFilters++
        }
        if (fromToDueDate != null && fromToDueDate != null)
        {
            numberOfActiveFilters++
        }
        if (listOfFriendsIds.size > 0)
        {
            numberOfActiveFilters++
        }

        return numberOfActiveFilters
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
    private fun containsFriend(shoppingList: ShoppingList): Boolean
    {
        var contains = false
        if (listOfFriendsIds.size != 0)
        {
            for(friendId in listOfFriendsIds)
            {
                if(shoppingList.friendsSharedWith.contains(friendId))
                {
                    contains = true
                }
            }
        }
        else
        {
            contains = true
        }
        return contains
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




    fun Context.hideKeyboard(view: View)
    {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}