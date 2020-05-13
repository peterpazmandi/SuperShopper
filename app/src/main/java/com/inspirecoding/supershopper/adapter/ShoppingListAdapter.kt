package com.inspirecoding.supershopper.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ItemOfShoppinglistBinding
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.fragments.MainFragmentDirections
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

private const val TAG = "ShoppingListAdapter"
class ShoppingListAdapter(val context: Context, val firebaseViewModel: FirebaseViewModel): RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>()
{
    var listOfShoppingLists: MutableList<ShoppingList> = mutableListOf()


    fun addShoppingListItem(shoppingListItem: ShoppingList, intoPosition: Int)
    {
        listOfShoppingLists.add(intoPosition, shoppingListItem)
        notifyItemInserted(intoPosition)
    }
    fun updateShoppingListItem(position: Int, shoppingListItem: ShoppingList)
    {
        listOfShoppingLists[position] = shoppingListItem
        notifyItemChanged(position)
    }
    fun removeShoppingListItem(position: Int)
    {
        listOfShoppingLists.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listOfShoppingLists.size)
    }
    fun removeAllShoppingListItem()
    {
        val size = listOfShoppingLists.size
        listOfShoppingLists.clear()
        notifyDataSetChanged()
//        notifyItemRangeRemoved(0, size)
    }
    fun addAllShoppingListItem(listOfItems: MutableList<ShoppingList>)
    {
//        this.listOfShoppingLists.clear()
        Log.d(TAG, "1_ list: ${listOfItems}")
        Log.d(TAG, "1_ all list: ${getAllShoppingList()}")
        removeAllShoppingListItem()
        this.listOfShoppingLists.addAll(listOfItems)
        Log.d(TAG, "2_ list: ${listOfItems}")
        Log.d(TAG, "2_ all list: ${getAllShoppingList()}")
        notifyDataSetChanged()
    }
    fun getAllShoppingList() = listOfShoppingLists
    fun getShoppingListItemFromPosition(position: Int): ShoppingList
    {
        return listOfShoppingLists[position]
    }
    fun getPositionOfShoppingListItem(shoppingListItem: ShoppingList): Int
    {
        val foundShoppingList = listOfShoppingLists.find {
            it.shoppingListId.equals(shoppingListItem.shoppingListId)
        }
        return listOfShoppingLists.indexOf(foundShoppingList)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListAdapter.ShoppingListViewHolder
    {
        val layoutInflater = LayoutInflater.from(context)
        val binding: ItemOfShoppinglistBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_of_shoppinglist,
            parent, false
        )

        return ShoppingListViewHolder(binding)
    }

    override fun getItemCount() = listOfShoppingLists.size

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int)
    {
        holder.bindShoppingList(listOfShoppingLists[position])
    }

    inner class ShoppingListViewHolder(val binding: ItemOfShoppinglistBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener
    {
        init {
            binding.clShoppingList.setOnClickListener(this)
        }

        fun bindShoppingList(shoppingList: ShoppingList)
        {
            binding.tvShoppingListDueDate.text = shoppingList.dueDate.toLocaleString().substringBeforeLast(" ")
            binding.tvShoppingListName.text = shoppingList.name

            val itemsCount = shoppingList.listOfItems.size
            val boughtItemsCount = shoppingList.listOfItems.filter { it.isBought }.size
            binding.pbItemProgress.progress = calculateRatio(boughtItemsCount, itemsCount)

            setShoppingListBackColorRegardingStatus(shoppingList.shoppingListStatus)

            setCheckMarkPrograssbarVisibility(itemsCount, boughtItemsCount)

            val sharedWith = shoppingList.friendsSharedWith.size

            // First, Remove the ID of the currently logged in user
            val removedCurrentUser = shoppingList.friendsSharedWith.filter { it != firebaseViewModel.currentUserLD.value?.id }
            when(sharedWith)
            {
                1 -> { // The creator user of the list
                    binding.ivShoppingListSharedWith1.visibility = View.INVISIBLE
                    binding.ivShoppingListSharedWith2.visibility = View.INVISIBLE
                    binding.ivShoppingListSharedWith3.visibility = View.INVISIBLE
                    binding.tvShoppingListSharedWithMore.visibility = View.INVISIBLE
                }
                2 -> {
                    binding.ivShoppingListSharedWith1.visibility = View.VISIBLE
                    binding.ivShoppingListSharedWith2.visibility = View.INVISIBLE
                    binding.ivShoppingListSharedWith3.visibility = View.INVISIBLE
                    binding.tvShoppingListSharedWithMore.visibility = View.INVISIBLE

                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[0]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                }
                3 -> {
                    binding.ivShoppingListSharedWith1.visibility = View.VISIBLE
                    binding.ivShoppingListSharedWith2.visibility = View.VISIBLE
                    binding.ivShoppingListSharedWith3.visibility = View.INVISIBLE
                    binding.tvShoppingListSharedWithMore.visibility = View.INVISIBLE

                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[0]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[1]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                }
                4 -> {
                    binding.ivShoppingListSharedWith1.visibility = View.VISIBLE
                    binding.ivShoppingListSharedWith2.visibility = View.VISIBLE
                    binding.ivShoppingListSharedWith3.visibility = View.VISIBLE
                    binding.tvShoppingListSharedWithMore.visibility = View.INVISIBLE

                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[0]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[1]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[2]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                }
                else -> {
                    binding.ivShoppingListSharedWith1.visibility = View.VISIBLE
                    binding.ivShoppingListSharedWith2.visibility = View.VISIBLE
                    binding.ivShoppingListSharedWith3.visibility = View.VISIBLE
                    binding.tvShoppingListSharedWithMore.visibility = View.VISIBLE
                    binding.tvShoppingListSharedWithMore.text = context.getString(R.string.plus_number, (sharedWith-4).toString())

                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[0]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[1]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                    firebaseViewModel.viewModelScope.launch {
                        if (removedCurrentUser.isNotEmpty())
                        {
                            setProfilePictures(
                                firebaseViewModel.getUserFromFirestore(removedCurrentUser[2]),
                                binding.ivShoppingListSharedWith1
                            )
                        }
                    }
                }
            }
        }

        private fun setShoppingListBackColorRegardingStatus(shoppingListStatus: ShoppingListStatus)
        {
            when (shoppingListStatus)
            {
                ShoppingListStatus.OPEN -> {
                    binding.mcvItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    binding.clShoppingList.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }
                ShoppingListStatus.DONE -> {
                    binding.mcvItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.lightGreen))
                    binding.clShoppingList.setBackgroundColor(ContextCompat.getColor(context, R.color.lightGreen))
                }
                ShoppingListStatus.CLOSED -> {
                    binding.mcvItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.lightOrange))
                    binding.clShoppingList.setBackgroundColor(ContextCompat.getColor(context, R.color.lightOrange))
                }
            }
        }
        private fun setProfilePictures(user: User?, imageView: ImageView)
        {
            user?.let {
                if(user.profilePicture.isNotEmpty())
                {
                    Picasso
                        .get()
                        .load(user.profilePicture)
                        .placeholder(R.drawable.profilepicture_blank)
                        .into(imageView)
                }
                else
                {
                    imageView.setImageResource(R.drawable.profilepicture_blank)
                }
            }
        }
        private fun setCheckMarkPrograssbarVisibility(itemsCount: Int, boughtItemsCount: Int)
        {
            if(itemsCount != boughtItemsCount)
            {
                binding.tvOpenBoughtItemsCount.text = getItemRateCountString(boughtItemsCount, itemsCount)

                binding.tvOpenBoughtItemsCount.visibility = View.VISIBLE
                binding.ivShoppingListsDoneMark.visibility = View.INVISIBLE
            }
            else
            {
                binding.tvOpenBoughtItemsCount.visibility = View.INVISIBLE
                binding.ivShoppingListsDoneMark.visibility = View.VISIBLE
            }
        }
        private fun calculateRatio(subset: Int, completeSet: Int): Int
        {
            val rate = (subset.toFloat()/completeSet.toFloat())*100f
            return rate.toInt()
        }
        private fun getItemRateCountString(subset: Int, completeSet: Int) = "$subset / $completeSet"

        override fun onClick(view: View)
        {
            val navController: NavController = Navigation.findNavController(view)
            val action = MainFragmentDirections.actionMainFragmentToShoppingListFragment(listOfShoppingLists[adapterPosition])
            navController.navigate(action)
        }
    }
}