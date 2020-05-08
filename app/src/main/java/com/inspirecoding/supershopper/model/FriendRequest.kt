package com.inspirecoding.supershopper.model

import android.os.Parcelable
import com.inspirecoding.supershopper.enums.FriendshipStatus
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class FriendRequest (
    var id: String = "",
    var date: Date = Calendar.getInstance().time,
    var friendshipStatus: FriendshipStatus = FriendshipStatus.NOFRIENDSHIP,
    var requestPartnerId: String = "",
    var requestOwnerId: String = ""
) : Parcelable