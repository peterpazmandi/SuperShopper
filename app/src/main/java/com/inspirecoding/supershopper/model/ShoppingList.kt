package com.inspirecoding.supershopper.model

import android.os.Parcelable
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ShoppingList (
    var shoppingListId: String = "",
    var timeStamp: Long = 0,
    var name: String = "",
    var shoppingListStatus: ShoppingListStatus = ShoppingListStatus.OPEN,
    var dueDate: Date = Calendar.getInstance().time,
    var friendsSharedWith: MutableList<String> = mutableListOf(),
    var listOfItems: MutableList<ListItem> = mutableListOf()
) : Parcelable