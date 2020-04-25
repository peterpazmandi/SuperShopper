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
import com.facebook.internal.Mutable
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

    private var resultList = mutableListOf<User>()
    override fun getCount(): Int {
        return resultList.size
    }

    override fun getItem(index: Int): User
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
        (convertView?.findViewById(R.id.text1) as TextView).setText(getItem(position).name)
        return convertView
    }

    override fun getFilter(): Filter
    {
        return object : Filter()
        {
            override fun performFiltering(constraint: CharSequence?): FilterResults
            {
                Log.d(TAG, "$constraint")
                val filterResults = FilterResults()
                if (constraint != null)
                {
                    Log.d(TAG, "$constraint")
                    val books: List<String> = foundUsers(constraint.toString())

                    // Assign the data to the FilterResults
                    filterResults.values = books
                    filterResults.count = books.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?)
            {
                Log.d(TAG, "$constraint")
                if (results != null && results.count > 0)
                {
                    resultList = results.values as MutableList<User>
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

        firebaseViewModel.getListOfFilteredUsersFromFirestore(searchText, 5)

        return protocol
    }

    fun updateUsersList(usersList: List<User>)
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