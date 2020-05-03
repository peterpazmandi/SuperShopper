package com.inspirecoding.supershopper.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ItemOfFriendslistBinding
import com.inspirecoding.supershopper.fragments.CreateNewListFragment
import com.inspirecoding.supershopper.fragments.DetailsFragment
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.User
import com.squareup.picasso.Picasso

private const val TAG = "FriendsListAdapter"
class FriendsListAdapter(val context: Context, val fragment: Fragment): RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder>()
{
    var listOfFriends: MutableList<User> = mutableListOf()

    private var onItemClickListener: OnItemClickListener? = null
    interface OnItemClickListener
    {
        fun onDeleteClick(position: Int)
    }
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?)
    {
        this.onItemClickListener = onItemClickListener
    }

    fun addFriend(user: User)
    {
        listOfFriends.add(user)
        notifyItemInserted(listOfFriends.size)
    }
    fun removeFriend(position: Int)
    {
        listOfFriends.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listOfFriends.size)
    }

    fun getFrindsList() = listOfFriends

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder
    {
        val layoutInflater = LayoutInflater.from(context)
        val binding: ItemOfFriendslistBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_of_friendslist,
            parent, false
        )

        return FriendsViewHolder(binding)
    }
    override fun getItemCount() = listOfFriends.size
    override fun onBindViewHolder(friendsViewHolder: FriendsViewHolder, position: Int)
    {
        friendsViewHolder.bindFriend(listOfFriends[position])
    }

    inner class FriendsViewHolder(val binding: ItemOfFriendslistBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener
    {
        init
        {
            binding.ivRemove.setOnClickListener(this)
        }

        fun bindFriend(user: User)
        {
            if(user.profilePicture.isNotEmpty())
            {
                Picasso
                    .get()
                    .load(user.profilePicture)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(binding.civProfilePicture)
            }

            binding.tvNameOfFriend.text = user.name

            Log.d(TAG, "$fragment")
            when (fragment)
            {
                is DetailsFragment -> {
                    Log.d(TAG, "DetailsFragment")
                    binding.ivRemove.visibility = View.GONE
                }
                is CreateNewListFragment -> {
                    Log.d(TAG, "CreateNewListFragment")
                    binding.ivRemove.visibility = View.VISIBLE
                }
            }
        }

        override fun onClick(v: View?)
        {
            onItemClickListener?.onDeleteClick(adapterPosition)
        }
    }
}