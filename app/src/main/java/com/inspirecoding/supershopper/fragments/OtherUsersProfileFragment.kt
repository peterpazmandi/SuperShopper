package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentOtherUsersProfileBinding
import com.inspirecoding.supershopper.enums.FriendshipStatus
import com.inspirecoding.supershopper.model.FriendRequest
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.utilities.CurrentDateFunctions
import com.inspirecoding.supershopper.viewmodels.FriendsViewModel
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject

private const val TAG = "OtherUsersProfileFrag"
class OtherUsersProfileFragment : Fragment()
{
    private lateinit var binding: FragmentOtherUsersProfileBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val friendsViewModel by navGraphViewModels<FriendsViewModel>(R.id.navigation_graph)


    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_other_users_profile, container, false)


        // Get the opened user
        val safeArgs: OtherUsersProfileFragmentArgs by navArgs()
        friendsViewModel.openedUser = safeArgs.user // User

        // Get currently logged in user
        firebaseViewModel.currentUserLD.observe(viewLifecycleOwner) { _currentUser ->
            friendsViewModel.currentUser = _currentUser
            // Get the friendship, if exist
            firebaseViewModel.getFriend(friendsViewModel.currentUser.id, friendsViewModel.openedUser.id).observe(viewLifecycleOwner) { _friend ->
                friendsViewModel.friendFetched = true
                    _friend?.id?.let { _id ->
                        if(_id.isNotEmpty())
                        {
                            friendsViewModel.friend = _friend
                        }
                        else
                        {
                            friendsViewModel.friend?.id = ""
                        }
                    }
                    Log.d(TAG, "Friend: ${friendsViewModel.friend}")
                    friendsViewModel.calculateFriendshipStatus()
                }

            // Get the friend request, if exist
            firebaseViewModel.getFriendRequest(friendsViewModel.currentUser.id, friendsViewModel.openedUser.id).observe(viewLifecycleOwner) { _friendRequest ->
                friendsViewModel.friendRequestFetched = true
                _friendRequest?.id?.let { _id ->
                    if(_id.isNotEmpty())
                    {
                        friendsViewModel.friendRequest = _friendRequest
                    }
                    else
                    {
                        friendsViewModel.friendRequest?.id = ""
                    }
                }
                Log.d(TAG, "FriendRequest: ${friendsViewModel.friendRequest}")
                friendsViewModel.calculateFriendshipStatus()

            }
            Log.d(TAG, "Friend: ${friendsViewModel.friend} FriendShip: ${friendsViewModel.friendRequest}")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // Calculate the status of the friendship between the current and opened user
        friendsViewModel.calculateFriendshipStatus()

        // Set the visible button regarding the status of the friendship
        friendsViewModel.friendshipStatus.observe(viewLifecycleOwner) { _friendshipStatus ->
            Log.d(TAG, "FriendShipStatus: ${_friendshipStatus}")
            Log.d(TAG, "Fetched: ${friendsViewModel.friendFetched}  ${friendsViewModel.friendRequestFetched}")

            if (friendsViewModel.friendFetched && friendsViewModel.friendRequestFetched)
            {
                setButtonStatuses(_friendshipStatus)
            }
        }

        friendsViewModel.friend?.let {

        }

        populateForm(friendsViewModel.openedUser)

        firebaseViewModel.spinner.observe(viewLifecycleOwner) { show ->
            binding.spinnerLoading.visibility = if (show) View.VISIBLE else View.GONE

            if(show)
            {
                setButtonsEnabled(false)
            }
            else
            {
                setButtonsEnabled(true)
            }
        }

        binding.btnSendRequest.setOnClickListener {
            insertSenderFriendRequest()
            insertRecieverFriendRequest()
            friendsViewModel.setFriendshipStatus(FriendshipStatus.SENDER)
        }
        binding.btnAccept.setOnClickListener {

        }
        binding.btnDelete.setOnClickListener {
            when (friendsViewModel.friendshipStatus.value)
            {
                FriendshipStatus.FRIENDS -> {
                    friendsViewModel.friend?.let { _friend ->
                        if(_friend.id.isNotEmpty()) {
                            // Delete from the current user the friend status
                            firebaseViewModel.deleteFriendFromFirestore(_friend.id).observe(viewLifecycleOwner)  { _friendshipStatus ->
                                if(_friendshipStatus == FriendshipStatus.DELETED) {
                                    friendsViewModel.setFriendshipStatus(FriendshipStatus.NOFRIENDSHIP)
                                }
                            }
                            // Delete from the partner user the friend status
                            // First we have to fetch this friend object
                            firebaseViewModel.getFriend(friendsViewModel.openedUser.id, friendsViewModel.currentUser.id).observe(viewLifecycleOwner) { _friend ->
                                _friend?.let { __friend ->
                                    // Second, delete this friend document also
                                    firebaseViewModel.deleteFriendFromFirestore(__friend.id).observe(viewLifecycleOwner)  { _friendshipStatus ->
                                        if(_friendshipStatus == FriendshipStatus.DELETED) {
                                            friendsViewModel.setFriendshipStatus(FriendshipStatus.NOFRIENDSHIP)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                FriendshipStatus.SENDER -> {
                    friendsViewModel.friendRequest?.let { _friendRequest ->
                        /** Get the first part of the friend request pair **/
                        removeSenderFriendRequest()
                        /** Get the other part of the friend request pair **/
                        removeReceiverFriendRequest()
                    }

                    friendsViewModel.setFriendshipStatus(FriendshipStatus.NOFRIENDSHIP)
                }
                FriendshipStatus.RECEIVER -> {
                    friendsViewModel.friendRequest?.let { _friendRequest ->
                        /** Get the other part of the friend request pair **/
                        removeSenderFriendRequest()
                        /** Get the other part of the friend request pair **/
                        removeReceiverFriendRequest()
                    }

                    friendsViewModel.setFriendshipStatus(FriendshipStatus.NOFRIENDSHIP)
                }
                else -> { /** Don't do anything **/ }
            }
        }
    }

    private fun removeReceiverFriendRequest()
    {
        firebaseViewModel.getFriendRequest(friendsViewModel.openedUser.id,friendsViewModel.currentUser.id).observe(viewLifecycleOwner) { _recieverFriendRequest ->
            _recieverFriendRequest?.let { __recieverFriendRequest ->
                firebaseViewModel.deleteFriendRequest(__recieverFriendRequest)
            }
        }
    }

    private fun removeSenderFriendRequest()
    {
        firebaseViewModel.getFriendRequest(friendsViewModel.currentUser.id,friendsViewModel.openedUser.id).observe(viewLifecycleOwner) { _recieverFriendRequest ->
            _recieverFriendRequest?.let { __recieverFriendRequest ->
                firebaseViewModel.deleteFriendRequest(__recieverFriendRequest)
            }
        }
    }

    override fun onDetach()
    {
        super.onDetach()

        friendsViewModel.friendFetched = false
        friendsViewModel.friendRequestFetched = false
    }

    private fun populateForm(user: User)
    {
        binding.tvName.text = user.name

        if(user.profilePicture.isNotEmpty())
        {
            Picasso
                .get()
                .load(user.profilePicture)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.civProfilePicture)
        }
    }
    private fun setButtonStatuses(friendshipStatus: FriendshipStatus?)
    {
        // If friends
        if(friendshipStatus == FriendshipStatus.FRIENDS)
        {
            binding.btnAccept.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnSendRequest.visibility = View.GONE
        }
        // If the current user sent a friend request
        if(friendshipStatus == FriendshipStatus.SENDER)
        {
            binding.btnAccept.visibility = View.GONE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnSendRequest.visibility = View.GONE
        }
        // If the current user got a friend request
        if(friendshipStatus == FriendshipStatus.RECEIVER)
        {
            binding.btnAccept.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnSendRequest.visibility = View.GONE
        }
        // If there aren't any friendship and request between the users
        if(friendshipStatus == FriendshipStatus.NOFRIENDSHIP)
        {
            binding.btnAccept.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
            binding.btnSendRequest.visibility = View.VISIBLE
        }
        // If there aren't any friendship and request between the users
        if(friendshipStatus == null)
        {
            binding.btnAccept.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
            binding.btnSendRequest.visibility = View.GONE
        }
    }

    private fun setButtonsEnabled(isEnabled: Boolean)
    {
        binding.btnAccept.isEnabled = isEnabled
        binding.btnDelete.isEnabled = isEnabled
        binding.btnSendRequest.isEnabled = isEnabled
    }

    private fun insertSenderFriendRequest()
    {
        val sender = firebaseViewModel.createFriendRequestInstance(
            CurrentDateFunctions.getToday().toDate(),
            FriendshipStatus.SENDER,
            friendsViewModel.currentUser.id,
            friendsViewModel.openedUser.id
        )
        firebaseViewModel.insertFriendRequest(sender)
    }
    private fun insertRecieverFriendRequest()
    {
        val reciever = firebaseViewModel.createFriendRequestInstance(
            CurrentDateFunctions.getToday().toDate(),
            FriendshipStatus.RECEIVER,
            friendsViewModel.openedUser.id,
            friendsViewModel.currentUser.id
        )
        firebaseViewModel.insertFriendRequest(reciever)
    }
}
