package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentOtherUsersProfileBinding
import com.inspirecoding.supershopper.enums.FriendshipStatus
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.FriendRequest
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.utilities.CombinedLiveData
import com.inspirecoding.supershopper.viewmodels.FriendsViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking
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
        }

        binding.btnSendRequest.setOnClickListener {

        }
        binding.btnAccept.setOnClickListener {

        }
        binding.btnDelete.setOnClickListener {
//            friend?.let { _friend ->
//                firebaseViewModel.deleteFriendFromFirestore(_friend.id).observe(viewLifecycleOwner)  { _friendshipStatus ->
//                    if(_friendshipStatus == FriendshipStatus.DELETED)
//                    {
//                        setSendRequestButtonToVisible()
//                    }
//                }
//            }
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
}
