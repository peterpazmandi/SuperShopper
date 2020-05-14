package com.inspirecoding.supershopper.repository

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.repository.sharedpreferences.SharedPreferencesRepository
import java.util.*

class SharedPreferencesViewModel(private val sharedPreferencesRepository: SharedPreferencesRepository): ViewModel()
{
    fun initFilterSharedPreferences(sharedPreferences: SharedPreferences)
    {
        sharedPreferencesRepository.initFilterSharedPreferences(sharedPreferences)
    }

    /** Shopping list status **/
    fun setStatusFilter(listOfStatuses: List<ShoppingListStatus>)
    {
        sharedPreferencesRepository.setStatusFilter(listOfStatuses)
    }
    fun getStatusFilter(): MutableList<ShoppingListStatus>?
    {
        return sharedPreferencesRepository.getStatusFilter()
    }
    /** Name **/
    fun setNameFilter(name: String)
    {
        sharedPreferencesRepository.setNameFilter(name)
    }
    fun getNameFilter(): String?
    {
        return sharedPreferencesRepository.getNameFilter()
    }
    /** Friends share with **/
    fun setFriendsFilter(listOfFriends: List<String>)
    {
        sharedPreferencesRepository.setFriendsFilter(listOfFriends)
    }
    fun getFriendsFilter(): MutableList<String>?
    {
        return sharedPreferencesRepository.getFriendsFilter()
    }
    /** Due date from **/
    fun setDueDateFromFilter(dueDateFrom: Long)
    {
        sharedPreferencesRepository.setDueDateFromFilter(dueDateFrom)
    }
    fun getDueDateFromFilter(): Date?
    {
        return sharedPreferencesRepository.getDueDateFromFilter()
    }
    /** Due date to **/
    fun setDueDateToFilter(dueDateTo: Long)
    {
        sharedPreferencesRepository.setDueDateToFilter(dueDateTo)
    }
    fun getDueDateToFilter(): Date?
    {
        return sharedPreferencesRepository.getDueDateToFilter()
    }
}