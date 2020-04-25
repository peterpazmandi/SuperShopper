package com.inspirecoding.supershopper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.ItemOfCreateNewListBinding
import com.inspirecoding.supershopper.enums.Prioirities
import com.inspirecoding.supershopper.fragments.CreateNewListFragmentDirections
import com.inspirecoding.supershopper.model.ListItem

private const val TAG = "ListItemAdapter"
class ListItemAdapter(val context: Context, var listOfItems: MutableList<ListItem>): RecyclerView.Adapter<ListItemAdapter.ItemViewHolder>()
{
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener
    {
        fun onUpdateClick(position: Int, listItem: ListItem)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?)
    {
        this.onItemClickListener = onItemClickListener
    }

    fun addItem(listItem: ListItem)
    {
        listOfItems.add(listItem)
        notifyItemInserted(listOfItems.size)
    }
    fun updateItem(position: Int, listItem: ListItem)
    {
        listOfItems[position] = listItem
        notifyItemChanged(position)
    }
    fun removeItem(position: Int)
    {
        listOfItems.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listOfItems.size)
    }
    fun removeAllItems()
    {
        val size = listOfItems.size
        listOfItems.clear()
        notifyItemRangeRemoved(0, size)
    }
    fun addAllItem(listOfItems: MutableList<ListItem>)
    {
        this.listOfItems.clear()
        this.listOfItems.addAll(listOfItems)
        notifyDataSetChanged()
    }
    fun getItemFromPosition(position: Int): ListItem
    {
        return listOfItems[position]
    }
//    fun getPositionOfItem(listItem: ListItem): Int
//    {
//        val foundToDo = listOfItems.find {
//            it.id.equals(listItem.id)
//        }
//        return listOfItems.indexOf(foundToDo)
//    }

    fun getItemsList() = listOfItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder
    {
        val layoutInflater = LayoutInflater.from(context)
        val binding: ItemOfCreateNewListBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_of_create_new_list,
            parent, false
        )

        return ItemViewHolder(binding)
    }
    override fun getItemCount() = listOfItems.size
    override fun onBindViewHolder(itemViewHolder: ItemViewHolder, position: Int)
    {
        itemViewHolder.bindItem(listOfItems[position])
    }

    inner class ItemViewHolder(val binding: ItemOfCreateNewListBinding): RecyclerView.ViewHolder(binding.root), View.OnClickListener
    {
        init
        {
            binding.ivEditItem.setOnClickListener(this)
            binding.ivDeleteItem.setOnClickListener(this)
        }
        override fun onClick(view: View)
        {
            when(view)
            {
                binding.ivEditItem -> {
                    if(adapterPosition != RecyclerView.NO_POSITION)
                    {
                        val navController: NavController = Navigation.findNavController(view)
                        val action = CreateNewListFragmentDirections.actionCreateNewListFragmentToAddNewItemDialog(listOfItems[adapterPosition], adapterPosition)
                        navController.navigate(action)
                    }
                }
                binding.ivDeleteItem -> {
                    if(adapterPosition != RecyclerView.NO_POSITION)
                    {
                        onItemClickListener?.onDeleteClick(adapterPosition)
                    }
                }
            }
        }
        fun bindItem(listItem: ListItem)
        {
            binding.tvItem.text = listItem.item
            binding.tvQuantity.text = listItem.qunatity.toString()
            binding.tvUnit.text = listItem.unit
            when (listItem.priority)
            {
                Prioirities.LOW -> binding.viewPriority.setBackgroundResource(R.drawable.shape_roundedleftside_green)
                Prioirities.MEDIUM -> binding.viewPriority.setBackgroundResource(R.drawable.shape_roundedleftside_orange)
                Prioirities.HIGH -> binding.viewPriority.setBackgroundResource(R.drawable.shape_roundedleftside_red)
            }
        }
    }
}