package com.inspirecoding.supershopper.viewmodels

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.enums.Crud
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.ListItem
import java.util.*

private const val TAG = "CreateNewListViewModel"
class CreateNewListFragmentViewModel: ViewModel()
{
    val listOfFriendsIds = mutableListOf<String>()
    var selectedDueDate: Date? = null


    private val _itemsListActionLD = MutableLiveData<Triple<Crud, Int?, ListItem?>?>()
    val itemsListActionLD: LiveData<Triple<Crud, Int?, ListItem?>?>
        get() = _itemsListActionLD

    fun isUserAlreadySelected(context: Context, view: View, selectedUserId: String): Boolean
    {
        context.hideKeyboard(view)

        val userOnTheList = listOfFriendsIds.filter {
            it == selectedUserId
        }
        Log.d(TAG, "3_ $userOnTheList")

        return userOnTheList.count() != 0
    }
    fun removeAlreadyAddedFriends(resultList: MutableList<Friend>): MutableList<Friend>
    {
        val _usersList = resultList.toMutableList()
        val filteredList = _usersList.filter {
            !listOfFriendsIds.contains(it.friendId)
        }
        return filteredList.toMutableList()
    }

    fun addItem(listItem: ListItem)
    {
        val action: Triple<Crud, Int?, ListItem?> = Triple(Crud.CREATE, null, listItem)
        _itemsListActionLD.value = action
    }
    fun updateItem(position: Int, listItem: ListItem)
    {
        val action: Triple<Crud, Int?, ListItem?> = Triple(Crud.UPDATE, position, listItem)
        _itemsListActionLD.value = action
    }
    fun clearItemsListActionLD()
    {
        _itemsListActionLD.value = null
    }

    fun Context.hideKeyboard(view: View)
    {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}