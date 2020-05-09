package com.inspirecoding.supershopper.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ItemOfReceivedFriendrequestsBinding
import com.inspirecoding.supershopper.fragments.FriendsMainFragmentDirections
import com.inspirecoding.supershopper.model.FriendRequest
import com.inspirecoding.supershopper.model.User
import com.squareup.picasso.Picasso

private const val TAG = "ReceivedFrReq'sAd"
class ReceivedFriendRequestsAdapter(val context: Context): RecyclerView.Adapter<ReceivedFriendRequestsAdapter.FriendRequestListViewHolder>()
{
    val listOfRequests = mutableListOf<Pair<FriendRequest, User>>()
    private var fetchedProfilePicture: HashMap<Int, Boolean> = HashMap()


    fun addRequests(listOfRequests: MutableList<Pair<FriendRequest, User>>)
    {
        for(i in 0..listOfRequests.lastIndex)
        {
            this.listOfRequests.add(listOfRequests[i])
        }
        notifyItemRangeInserted(this.listOfRequests.size - listOfRequests.size, this.listOfRequests.size)
    }

    override fun onViewAttachedToWindow(holder: FriendRequestListViewHolder)
    {
        holder.setIsRecyclable(false)
        super.onViewAttachedToWindow(holder)
    }
    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int
    {
        return position
    }
    override fun onViewDetachedFromWindow(holder: FriendRequestListViewHolder)
    {
        holder.setIsRecyclable(true)
        super.onViewDetachedFromWindow(holder)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestListViewHolder
    {
        val layoutInflater = LayoutInflater.from(context)
        val binding: ItemOfReceivedFriendrequestsBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_of_received_friendrequests, parent, false
        )

        return FriendRequestListViewHolder(binding)
    }

    override fun getItemCount() = listOfRequests.size

    override fun onBindViewHolder(friendRequestListViewHolder: FriendRequestListViewHolder, position: Int)
    {
        if(!fetchedProfilePicture.contains(position))
        {
            friendRequestListViewHolder.bindItem(listOfRequests[position])
            fetchedProfilePicture[position] = true
            Log.d(TAG, "Fetched now")
        }
        else
        {
            Log.d(TAG, "Already fetched")
        }
    }

    inner class FriendRequestListViewHolder (val binding: ItemOfReceivedFriendrequestsBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener
    {
        init
        {
            binding.constraintLayout.setOnClickListener(this)
        }

        fun bindItem(pairOfRequests: Pair<FriendRequest, User>)
        {
            binding.tvDate.text = pairOfRequests.first.date.toLocaleString().substringBeforeLast(" ")

            if(pairOfRequests.second.profilePicture.isNotEmpty())
            {
                Picasso.get()
                    .load(pairOfRequests.second.profilePicture)
                    .error(R.drawable.profilepicture_blank)
                    .placeholder(R.drawable.profilepicture_blank)
                    .into(binding.civProfilePicture)
            }

            binding.tvSenderName.text = pairOfRequests.second.name
        }

        override fun onClick(view: View)
        {
            navigateToCreateNewList(view)
        }

        private fun navigateToCreateNewList(view: View)
        {
            val navController: NavController = Navigation.findNavController(view)
            val action = FriendsMainFragmentDirections.actionFriendsMainFragmentToOtherUsersProfileFragment(
                user = listOfRequests[adapterPosition].second)
            navController.navigate(action)
        }
    }
}