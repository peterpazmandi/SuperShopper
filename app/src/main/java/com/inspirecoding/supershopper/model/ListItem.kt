package com.inspirecoding.supershopper.model

import android.os.Parcelable
import com.inspirecoding.supershopper.enums.Prioirities
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListItem (
    var id: String = "",
    var item: String = "",
    var unit: String = "",
    var qunatity: Float = 0f,
    var priority: Prioirities = Prioirities.LOW,
    var isBought: Boolean = false
) : Parcelable