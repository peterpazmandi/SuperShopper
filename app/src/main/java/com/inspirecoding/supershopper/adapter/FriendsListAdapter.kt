package com.inspirecoding.supershopper.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ItemOfFriendslistBinding
import com.inspirecoding.supershopper.fragments.FriendsMainFragmentDirections
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

private const val TAG = "FriendsListAdapter"
class FriendsListAdapter(val context: Context, val firebaseViewModel: FirebaseViewModel): RecyclerView.Adapter<FriendsListAdapter.FriendsListViewHolder>()
{
    private var listOfFriends = mutableListOf<Pair<Friend, User>>()
    private var fetchedProfilePicture: HashMap<Int, Boolean> = HashMap()

    fun addFriend(pairOfFriendAndUser: Pair<Friend, User>)
    {
        listOfFriends.add(pairOfFriendAndUser)
        notifyItemInserted(listOfFriends.size)
    }
    fun updateFriend(position: Int, pairOfFriendAndUser: Pair<Friend, User>)
    {
        listOfFriends[position] = pairOfFriendAndUser
        notifyItemChanged(position)
    }
    fun removeFriend(position: Int)
    {
        listOfFriends.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listOfFriends.size)
    }
    fun addFriends(listOfFriends: MutableList<Pair<Friend, User>>)
    {
        for(i in 0..listOfFriends.lastIndex)
        {
            this.listOfFriends.add(listOfFriends[i])
        }
        notifyItemRangeInserted(this.listOfFriends.size - listOfFriends.size, this.listOfFriends.size)
    }
    fun getPositionOfTheFriend(pairOfFriendAndUser: Pair<Friend, User>): Int
    {
        val foundFriend = listOfFriends.find {
            it.first.id.equals(pairOfFriendAndUser.first.id)
        }
        return listOfFriends.indexOf(foundFriend)
    }

    override fun onViewAttachedToWindow(holder: FriendsListViewHolder)
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
    override fun onViewDetachedFromWindow(holder: FriendsListViewHolder)
    {
        holder.setIsRecyclable(true)
        super.onViewDetachedFromWindow(holder)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsListViewHolder
    {
        val layoutInflater = LayoutInflater.from(context)
        val binding: ItemOfFriendslistBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_of_friendslist,
            parent, false
        )

        return FriendsListViewHolder(binding)
    }

    override fun getItemCount() = listOfFriends.size

    override fun onBindViewHolder(friendsListViewHolder: FriendsListViewHolder, position: Int)
    {
        if(!fetchedProfilePicture.contains(position))
        {
            friendsListViewHolder.bindItem(listOfFriends[position].second)
            fetchedProfilePicture[position] = true
            Log.d(TAG, "Fetched now")
        }
        else
        {
            Log.d(TAG, "Already fetched")
        }
    }





    inner class FriendsListViewHolder (val binding: ItemOfFriendslistBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener
    {
        init
        {
            binding.constraintLayout.setOnClickListener(this)
        }

        fun bindItem(user: User)
        {
            if(user.profilePicture.isNotEmpty())
            {
                Picasso.get()
                    .load(user.profilePicture)
                    .into(binding.civProfilePicture)
            }

            binding.tvNameOfFriend.text = user.name
        }

        override fun onClick(view: View)
        {
            navigateToCreateNewList(view)
        }

        private fun navigateToCreateNewList(view: View)
        {
            firebaseViewModel.viewModelScope.launch {
                val navController: NavController = Navigation.findNavController(view)
                val action = FriendsMainFragmentDirections.actionFriendsMainFragmentToOtherUsersProfileFragment(
                    user = listOfFriends[adapterPosition].second)
                navController.navigate(action)
            }
        }
    }
}