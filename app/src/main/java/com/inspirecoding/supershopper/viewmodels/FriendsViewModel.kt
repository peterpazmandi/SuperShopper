package com.inspirecoding.supershopper.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inspirecoding.supershopper.enums.FriendshipStatus
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.FriendRequest
import com.inspirecoding.supershopper.model.User

private const val TAG = "FriendsViewModel"
class FriendsViewModel: ViewModel()
{
    var currentUser = User()
    var openedUser = User()
    var friendRequest: FriendRequest? = FriendRequest()
    var friend: Friend? = Friend()

    var friendFetched: Boolean = false
    var friendRequestFetched: Boolean = false

    private val _friendshipStatus = MutableLiveData<FriendshipStatus>()
    val friendshipStatus: LiveData<FriendshipStatus>
        get() = _friendshipStatus

    fun setFriendshipStatus (friendshipStatus: FriendshipStatus)
    {
        _friendshipStatus.value = friendshipStatus
    }

    fun calculateFriendshipStatus()
    {
        Log.d(TAG, "Friend: ${friend} FriendShip: ${friendRequest}")
        // If friends
        if(friend?.id != "" && friendRequest?.id == "")
        {
            setFriendshipStatus(FriendshipStatus.FRIENDS)
        }
        // If the current user sent a friend request
        else if (friend?.id == "" && friendRequest?.friendshipStatus == FriendshipStatus.SENDER)
        {
            setFriendshipStatus(FriendshipStatus.SENDER)
        }
        // If the current user got a friend request
        else if (friend?.id == "" && friendRequest?.friendshipStatus == FriendshipStatus.RECEIVER)
        {
            setFriendshipStatus(FriendshipStatus.RECEIVER)
        }
        // If there aren't any friendship and request between the users
        else if (friend?.id == "" && friendRequest?.id == "")
        {
            setFriendshipStatus(FriendshipStatus.NOFRIENDSHIP)
        }
    }




}