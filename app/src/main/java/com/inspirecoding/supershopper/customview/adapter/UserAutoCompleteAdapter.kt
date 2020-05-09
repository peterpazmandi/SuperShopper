package com.inspirecoding.supershopper.customview.adapter

import android.R
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


private const val TAG = "UserAutoCompleteAdapter"
class UserAutoCompleteAdapter(
    val fragment: Fragment,
    val firebaseViewModel: FirebaseViewModel) : BaseAdapter(), Filterable
{
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var resultList = mutableListOf<Friend>()
    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(index: Int): Friend
    {
        return resultList[index]
    }

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        var convertView: View? = convertView
        if (convertView == null)
        {
            val inflater = fragment.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.simple_dropdown_item_1line, parent, false)
        }
        (convertView?.findViewById(R.id.text1) as TextView).setText(getItem(position).friendName)
        return convertView
    }

    override fun getFilter(): Filter
    {
        return object : Filter()
        {
            override fun performFiltering(constraint: CharSequence?): FilterResults
            {
                val filterResults = FilterResults()
                if (constraint != null)
                {
                    val users: List<String> = foundUsers(constraint.toString())

                    Log.d(TAG, "5_ $users")
                    // Assign the data to the FilterResults
                    filterResults.values = users
                    filterResults.count = users.size
                    Log.d(TAG, "1_ $filterResults")
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?)
            {
                Log.d(TAG, "2_ ${results?.count}")
                if (results != null && results.count > 0)
                {
                    resultList = results.values as MutableList<Friend>
                    Log.d(TAG, "3_ $resultList")
                    notifyDataSetChanged()
                }
                else
                {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun foundUsers(searchText: String): List<String>
    {
        val protocol = mutableListOf<String>()

        firebaseViewModel.currentUserLD.value?.id?.let { currentUserId ->
            firebaseViewModel.getListOfFilteredFriendsFromFirestore(currentUserId, searchText, 5)
        }

        return protocol
    }

    fun updateUsersList(usersList: List<Friend>)
    {
        resultList.clear()
        resultList.addAll(usersList)
        notifyDataSetChanged()
    }

    companion object
    {
        private const val MAX_RESULTS = 10
    }
}