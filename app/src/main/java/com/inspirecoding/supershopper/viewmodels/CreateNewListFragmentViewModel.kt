package com.inspirecoding.supershopper.viewmodels

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.enums.Crud
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.User
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "CreateNewListViewModel"
class CreateNewListFragmentViewModel: ViewModel()
{
    private val listOfFriends = mutableListOf<User>()
    var selectedDueDate: Date? = null


    private val _itemsListAction = MutableLiveData<Triple<Crud, Int?, ListItem?>>()
    val itemsListAction: LiveData<Triple<Crud, Int?, ListItem?>>
        get() = _itemsListAction

    fun addFriendChip(context: Context, user: User, chipGroup: ChipGroup)
    {
        val chip = Chip(context)
        chip.text = user.name
        chip.tag = user

        if(user.profilePicture.isEmpty())
        {
            Log.i(TAG, "isEmpty")
            chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_person)
        }
        else
        {
            Log.i(TAG, "isNotEmpty")
            val target = getTargetOfPicasso(context, chip)
            Picasso
                .get()
                .load(user.profilePicture)
                .placeholder(R.drawable.ic_person)
                .transform(CropCircleTransformation())
                .into(target)
        }

        chip.isCloseIconVisible = true

        context?.let { context ->
            chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
        }
        chip.setChipIconTintResource(R.color.colorPrimaryDark)

        chip.invalidate()

        chipGroup.addView(chip as View)
        chipGroup.invalidate()

        chip.setOnCloseIconClickListener { selectedView ->
            val selectedUser = selectedView.tag as User
            listOfFriends.remove(selectedUser)
            chipGroup.removeView(chip as View)

            chipGroup.invalidate()
            selectedView.invalidate()
        }

        context.hideKeyboard(chipGroup)
    }
    fun insertToFriends(friend: User)
    {
        listOfFriends.add(friend)
    }
    fun clearListOfFriends()
    {
        listOfFriends.clear()
    }
    fun isUserAlreadySelected(context: Context, view: View, selectedUser: User): Boolean
    {
        context.hideKeyboard(view)

        val userOnTheList = listOfFriends.filter {
            it.id == selectedUser.id
        }

        return userOnTheList.count() != 0
    }
    private fun getTargetOfPicasso(context: Context, targetChip: Chip): Target = object : Target
    {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom)
        {
            viewModelScope.launch {
                delay(1_000)
                val drawable: Drawable = BitmapDrawable(context.resources, bitmap)
                targetChip.chipIcon = drawable
            }
        }

        override fun onBitmapFailed(exception: Exception, errorDrawable: Drawable)
        {
            targetChip.chipIcon = errorDrawable
            Log.i(TAG, exception.message!!)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable)
        {
            viewModelScope.launch {
                targetChip.chipIcon?.invalidateSelf()
                targetChip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_person)
                delay(2_000)
            }
        }
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
    fun removeItem(position: Int)
    {
        val action: Triple<Crud, Int?, ListItem?> = Triple(Crud.DELETE, position, null)
        _itemsListAction.value = action
    }

    fun Context.hideKeyboard(view: View)
    {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}