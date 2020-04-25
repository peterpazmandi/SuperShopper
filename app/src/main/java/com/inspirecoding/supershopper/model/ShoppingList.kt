package com.inspirecoding.supershopper.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class ShoppingList (
    var id: String = "",
    var timeStamp: Long = 0,
    var name: String = "",
    var dueDate: Date = Calendar.getInstance().time,
    var friendsSharedWith: MutableList<String> = mutableListOf(),
    var listOfItems: String = ""
) : Parcelable