package com.inspirecoding.supershopper.repository.sharedpreferences

import android.content.SharedPreferences
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.utilities.ConverterFunctions
import java.util.*

class SharedPreferencesRepository
{
    private val KEY_STATUS = "status"
    private val KEY_NAME = "name"
    private val KEY_FRIENDS = "friends"
    private val KEY_DUEDATEFROM = "duedatefrom"
    private val KEY_DUEDATETO = "duedateto"

    private lateinit var mySharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun initFilterSharedPreferences(sharedPreferences: SharedPreferences)
    {
        mySharedPreferences = sharedPreferences
    }

    /** Shopping list status **/
    fun setStatusFilter(listOfStatuses: List<ShoppingListStatus>)
    {
        val _listOfStatuses = mutableListOf<String>()
        for (shoppingListStatus in listOfStatuses)
        {
            _listOfStatuses.add(shoppingListStatus.name)
        }
        editor = mySharedPreferences.edit()
        editor.putStringSet(KEY_STATUS, _listOfStatuses.toSet())
        editor.apply()
    }
    fun getStatusFilter(): MutableList<ShoppingListStatus>?
    {
        val listOfStatuses = mutableListOf<ShoppingListStatus>()
        val _listOfStatuses = mySharedPreferences.getStringSet(KEY_STATUS, setOf())?.toList()
        if (_listOfStatuses != null)
        {
            for (shoppingListStatus in _listOfStatuses)
            {
                when (shoppingListStatus)
                {
                    ShoppingListStatus.OPEN.name -> listOfStatuses.add(ShoppingListStatus.OPEN)
                    ShoppingListStatus.DONE.name -> listOfStatuses.add(ShoppingListStatus.DONE)
                    ShoppingListStatus.CLOSED.name -> listOfStatuses.add(ShoppingListStatus.CLOSED)
                }
            }
            return listOfStatuses
        }
        else
        {
            return null
        }
    }
    /** Name **/
    fun setNameFilter(name: String?)
    {
        editor = mySharedPreferences.edit()
        if(name != null)
        {
            editor.putString(KEY_NAME, name)
        }
        else
        {
            editor.putString(KEY_NAME, "")
        }
        editor.apply()
    }
    fun getNameFilter(): String?
    {
        return mySharedPreferences.getString(KEY_NAME, "")
    }
    /** Friends share with **/
    fun setFriendsFilter(listOfFriends: List<String>)
    {
        editor = mySharedPreferences.edit()
        editor.putStringSet(KEY_FRIENDS, listOfFriends.toSet())
        editor.apply()
    }
    fun getFriendsFilter(): MutableList<String>?
    {
        return mySharedPreferences.getStringSet(KEY_FRIENDS, setOf())?.toMutableList()
    }
    /** Due date from **/
    fun setDueDateFromFilter(dueDateFrom: Long?)
    {
        editor = mySharedPreferences.edit()
        if(dueDateFrom == null)
        {
            editor.putLong(KEY_DUEDATEFROM, 0)
        }
        else
        {
            editor.putLong(KEY_DUEDATEFROM, dueDateFrom)
        }
        editor.apply()
    }
    fun getDueDateFromFilter(): Date?
    {
        val dueDateFromInLong = mySharedPreferences.getLong(KEY_DUEDATEFROM, 0)
        return if (dueDateFromInLong != 0L)
        {
            ConverterFunctions.convertToDate(dueDateFromInLong)
        }
        else
        {
            null
        }
    }
    /** Due date to **/
    fun setDueDateToFilter(dueDateTo: Long?)
    {
        editor = mySharedPreferences.edit()
        if(dueDateTo == null)
        {
            editor.putLong(KEY_DUEDATETO, 0)
        }
        else
        {
            editor.putLong(KEY_DUEDATETO, dueDateTo)
        }
        editor.apply()
    }
    fun getDueDateToFilter(): Date?
    {
        val dueDateToInLong = mySharedPreferences.getLong(KEY_DUEDATETO, 0)
        return if (dueDateToInLong != 0L)
        {
            ConverterFunctions.convertToDate(dueDateToInLong)
        }
        else
        {
            null
        }
    }
}