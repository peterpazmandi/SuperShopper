package com.inspirecoding.supershopper.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.util.Pair
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.FriendsListChipAdapter
import com.inspirecoding.supershopper.customview.adapter.UserAutoCompleteAdapter
import com.inspirecoding.supershopper.databinding.FragmentFilterShoppingListBinding
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.repository.SharedPreferencesViewModel
import com.inspirecoding.supershopper.viewmodels.FilterShoppingListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*

private const val TAG = "FilterShoppingList"
class FilterShoppingListFragment : Fragment()
{
    private lateinit var binding: FragmentFilterShoppingListBinding
    private val firebaseViewModel: FirebaseViewModel by inject()
    private val sharedPreferencesViewModel: SharedPreferencesViewModel by inject()
    private val filterViewModel by navGraphViewModels<FilterShoppingListViewModel>(R.id.navigation_graph)

    private lateinit var userAutoCompleteAdapter: UserAutoCompleteAdapter
    private lateinit var friendsListChipAdapter: FriendsListChipAdapter

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_filter_shopping_list, container, false)

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        userAutoCompleteAdapter = UserAutoCompleteAdapter(this, firebaseViewModel)
        binding.actvFriendsSharedWith.threshold = 3
        binding.actvFriendsSharedWith.setAdapter(userAutoCompleteAdapter)

        context?.let {context ->
            binding.actvFriendsSharedWith.addTextChangedListener { searchText ->
                searchText?.let { _searchText ->
                    if(_searchText.length < 3)
                    {
                        binding.tilFriendsSharedWith.error = getString(R.string.enter_at_least_3_characters)
                        binding.pbFriendsSharedWith.visibility = View.GONE
                    }
                    else
                    {
                        binding.tilFriendsSharedWith.error = null
                        binding.pbFriendsSharedWith.visibility = View.VISIBLE
                        userAutoCompleteAdapter.notifyDataSetChanged()
                    }
                }
            }

            binding.actvFriendsSharedWith.setOnItemClickListener{ parent, view, position, arg3 ->
                binding.actvFriendsSharedWith.text = null
                val selectedFriend = parent.getItemAtPosition(position) as Friend

                if(filterViewModel.isUserAlreadySelected(context, binding.root, selectedFriend.friendId))
                {
                    Toast.makeText(context, R.string.you_have_already_added_this_friend, Toast.LENGTH_LONG).show()
                }
                else
                {
                    firebaseViewModel.viewModelScope.launch {
                        val selectedUser = firebaseViewModel.getUserFromFirestore(selectedFriend.friendId)
                        selectedUser?.let { _selectedUser ->
                            binding.tilFriendsSharedWith.error = null
                            filterViewModel.listOfFriendsIds_temp.add(selectedFriend.friendId)
                            friendsListChipAdapter.addFriend(_selectedUser)
                        }
                    }
                }
            }

            initFriendListRecyclerView(context)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        populateForm()

        binding.tvDueDateRange.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnActionButtonsApplyFilter.setOnClickListener {
            /** Set filter **/
            setFilters()
            /** Set filters into SharePreferences **/
            filterViewModel.setSharePreferencesFilters(sharedPreferencesViewModel)
            /** Apply the new filters **/
            filterViewModel.onFilterChangedClickListener.onFilterChanged()

            findNavController().navigate(R.id.action_filterShoppingListFragment_to_mainFragment)
        }

        firebaseViewModel.friendsListMLD.observe(viewLifecycleOwner) { friendsList ->
            val _usersList = filterViewModel
                .removeAlreadyAddedFriends(friendsList.toMutableList())

            userAutoCompleteAdapter.updateUsersList(_usersList)
            binding.pbFriendsSharedWith.visibility = View.GONE
        }

        friendsListChipAdapter.setOnItemClickListener(object: FriendsListChipAdapter.OnItemClickListener{
            override fun onDeleteClick(position: Int)
            {
                filterViewModel.listOfFriendsIds_temp.removeAt(position)
                friendsListChipAdapter.removeFriend(position)
            }
        })
    }

    private fun initFriendListRecyclerView(context: Context)
    {
        friendsListChipAdapter = FriendsListChipAdapter(context, this)
        binding.rvFriendsSharedWith.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsListChipAdapter
        }
    }

    private fun showDatePickerDialog()
    {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()

        if (filterViewModel.fromToDueDate != null)
        {
            val fromDueDate = (filterViewModel.fromToDueDate as Pair).first!!.time
            val toDueDate = (filterViewModel.fromToDueDate as Pair).second!!.time
            builder.setSelection(Pair(fromDueDate, toDueDate))
        }
        else if (filterViewModel.fromToDueDate_temp != null)
        {
            val fromDueDate = (filterViewModel.fromToDueDate_temp as Pair).first!!.time
            val toDueDate = (filterViewModel.fromToDueDate_temp as Pair).second!!.time
            builder.setSelection(Pair(fromDueDate, toDueDate))
        }
        else
        {
            builder.setSelection(Pair(now.timeInMillis, now.timeInMillis))
        }

        val picker = builder.build()

        picker.show(parentFragmentManager, "picker")

        picker.addOnPositiveButtonClickListener { pairOfTimePeriod ->
            /** Temp Pair is needed because it is not sure that the user will apply the filter
             *  But the selected date has to be saved and showed for the user
             */
            filterViewModel.setTempDueDate(pairOfTimePeriod)
            filterViewModel.fromToDueDate_temp?.let {
                populateDueDate(it)
            }
        }
    }
    private fun populateForm()
    {
        /** Shopping list statuses **/
        for(shoppingListStatus in filterViewModel.listOfShoppingListStatus)
        {
            when (shoppingListStatus)
            {
                ShoppingListStatus.OPEN -> {
                    binding.chbStatusOpenList.isChecked = true
                }
                ShoppingListStatus.DONE -> {
                    binding.chbStatusDoneList.isChecked = true
                }
                ShoppingListStatus.CLOSED -> {
                    binding.chbStatusClosedList.isChecked = true
                }
            }
        }
        /** Name of shopping list **/
        if (filterViewModel.name != null)
        {
            binding.etName.setText(filterViewModel.name)
        }
        else
        {
            binding.etName.setText("")
        }
        /** List of friends **/
        for(friendId in filterViewModel.listOfFriendsIds)
        {
            firebaseViewModel.viewModelScope.launch {
                val friend = firebaseViewModel.getUserFromFirestore(friendId)
                friend?.let {user ->
                    friendsListChipAdapter.addFriend(user)
                }
            }
        }
        /** Due date **/
        if (filterViewModel.fromToDueDate != null)
        {
            filterViewModel.fromToDueDate?.let {
                populateDueDate(it)
            }
        }
        else
        {
            binding.etName.setText("")
        }
    }
    private fun populateDueDate(fromToDueDate: Pair<Date, Date>)
    {
        val fromDueDate = fromToDueDate.first!!.toLocaleString()!!.substringBeforeLast(" ")
        val toDueDate = fromToDueDate.second!!.toLocaleString()!!.substringBeforeLast(" ")
        binding.tvDueDateRange.text = String.format(resources.getString(R.string.from_to), fromDueDate, toDueDate)
    }

    private fun setFilters()
    {
        filterViewModel.setShoppingListFilter(
            isOpenChecked = binding.chbStatusOpenList.isChecked,
            isDoneChecked = binding.chbStatusDoneList.isChecked,
            isClosedChecked = binding.chbStatusClosedList.isChecked)
        filterViewModel.setNameFilter(binding.etName.text.toString())
        /** When the user click on the Apply button, then the the list of friends can be moved to the permanent variable **/
        filterViewModel.listOfFriendsIds = filterViewModel.listOfFriendsIds_temp
        /** When the user click on the Apply button, then the date can be moved to the permanent variable **/
        filterViewModel.fromToDueDate = filterViewModel.fromToDueDate_temp
    }






}
