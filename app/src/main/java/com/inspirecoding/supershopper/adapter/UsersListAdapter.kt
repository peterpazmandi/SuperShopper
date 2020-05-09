package com.inspirecoding.supershopper.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ItemOfFriendslistBinding
import com.inspirecoding.supershopper.fragments.SearchFriendFragmentDirections
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

private const val TAG = "UsersListAdapter"
class UsersListAdapter(val context: Context, val firebaseViewModel: FirebaseViewModel): RecyclerView.Adapter<UsersListAdapter.UsersListViewHolder>()
{
    private val listOfUsers = mutableListOf<User>()
    private var fetchedProfilePicture: HashMap<Int, Boolean> = HashMap()



    fun addUsers(listOfUsers: MutableList<User>)
    {
        for(i in 0..listOfUsers.lastIndex)
        {
            this.listOfUsers.add(listOfUsers[i])
        }
        notifyItemRangeInserted(this.listOfUsers.size - listOfUsers.size, this.listOfUsers.size)
    }
    fun replaceAllUsers(listOfUsers: MutableList<User>)
    {
        this.listOfUsers.clear()
        notifyDataSetChanged()
        this.listOfUsers.addAll(listOfUsers)
        notifyDataSetChanged()
    }
    fun updateListOfUsers(listOfUsers: MutableList<User>)
    {
        listOfUsers.clear()
        for(i in 0..listOfUsers.lastIndex)
        {
            this.listOfUsers.add(listOfUsers[i])
        }
        notifyDataSetChanged()
    }
    fun removeAllItems()
    {
        listOfUsers.clear()
        notifyDataSetChanged()
    }

    override fun onViewAttachedToWindow(holder: UsersListViewHolder)
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
    override fun onViewDetachedFromWindow(holder: UsersListViewHolder)
    {
        holder.setIsRecyclable(true)
        super.onViewDetachedFromWindow(holder)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersListViewHolder
    {
        val layoutInflater = LayoutInflater.from(context)
        val binding: ItemOfFriendslistBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_of_friendslist,
            parent, false
        )

        return UsersListViewHolder(binding)
    }

    override fun getItemCount() = listOfUsers.size

    override fun onBindViewHolder(usersListViewHolder: UsersListViewHolder, position: Int)
    {
        if(!fetchedProfilePicture.contains(position))
        {
            usersListViewHolder.bindItem(listOfUsers[position])
            fetchedProfilePicture[position] = true
            Log.d(TAG, "Fetched now")
        }
        else
        {
            Log.d(TAG, "Already fetched")
        }
    }

    inner class UsersListViewHolder (val binding: ItemOfFriendslistBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener
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
                    .error(R.drawable.profilepicture_blank)
                    .placeholder(R.drawable.profilepicture_blank)
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
            context.hideKeyboard(view)
            firebaseViewModel.viewModelScope.launch {
                val navController: NavController = Navigation.findNavController(view)
                val action = SearchFriendFragmentDirections.actionSearchFriendFragmentToOtherUsersProfileFragment(
                    user = listOfUsers[adapterPosition])
                navController.navigate(action)
            }
        }
    }



    fun Context.hideKeyboard(view: View)
    {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}