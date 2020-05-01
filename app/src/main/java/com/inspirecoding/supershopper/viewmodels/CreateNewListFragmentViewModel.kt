package com.inspirecoding.supershopper.viewmodels

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.enums.Crud
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.User
import java.util.*

private const val TAG = "CreateNewListViewModel"
class CreateNewListFragmentViewModel: ViewModel()
{
    val listOfFriends = mutableListOf<User>()
    var selectedDueDate: Date? = null


    private val _itemsListAction = MutableLiveData<Triple<Crud, Int?, ListItem?>>()
    val itemsListAction: LiveData<Triple<Crud, Int?, ListItem?>>
        get() = _itemsListAction

    fun isUserAlreadySelected(context: Context, view: View, selectedUser: User): Boolean
    {
        context.hideKeyboard(view)

        val userOnTheList = listOfFriends.filter {
            it.id == selectedUser.id
        }

        return userOnTheList.count() != 0
    }
    fun removeCurrentUserAndAddedFriends(currentUser: User, alreadySelectedUsers: MutableList<User>, resultList: MutableList<User>): MutableList<User>
    {
        val _usersList = resultList.toMutableList()
        _usersList.remove(currentUser)
        _usersList.removeAll(alreadySelectedUsers)
        return _usersList
    }

    fun addItem(listItem: ListItem)
    {
        val action: Triple<Crud, Int?, ListItem?> = Triple(Crud.CREATE, null, listItem)
        _itemsListAction.value = action
    }
    fun updateItem(position: Int, listItem: ListItem)
    {
        val action: Triple<Crud, Int?, ListItem?> = Triple(Crud.UPDATE, position, listItem)
        _itemsListAction.value = action
    }

    fun Context.hideKeyboard(view: View)
    {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}