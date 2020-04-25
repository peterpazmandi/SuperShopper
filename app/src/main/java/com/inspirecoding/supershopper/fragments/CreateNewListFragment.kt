package com.inspirecoding.supershopper.fragments

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.ListItemAdapter
import com.inspirecoding.supershopper.customview.adapter.UserAutoCompleteAdapter
import com.inspirecoding.supershopper.databinding.FragmentCreateNewListBinding
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.utilities.CurrentDateFunctions
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.CreateNewListFragmentViewModel
import org.joda.time.DateTime
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.observe
import com.inspirecoding.supershopper.enums.Crud
import com.inspirecoding.supershopper.model.ListItem
import kotlinx.android.synthetic.main.fragment_create_new_list.*


private const val TAG = "CreateNewListFragment"
class CreateNewListFragment : Fragment(), DatePickerDialog.OnDateSetListener
{
    private lateinit var binding: FragmentCreateNewListBinding

    private val firebaseViewModel: FirebaseViewModel by inject()
    private lateinit var userAutoCompleteAdapter: UserAutoCompleteAdapter
    private val createNewListFragmentViewModel: CreateNewListFragmentViewModel by navGraphViewModels(R.id.navigation_graph)

    private lateinit var listItemAdapter: ListItemAdapter

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_create_new_list, container, false)

        binding.rvCreateNewListFourthItem.invalidate()
        binding.rvCreateNewListFourthItem.postInvalidate()

        userAutoCompleteAdapter = UserAutoCompleteAdapter(this, firebaseViewModel)

        setTodaysDate()

        context?.let {context ->
            binding.actvCreateNewListThirdItemSearchFriends.addTextChangedListener { searchText ->
                searchText?.let { _searchText ->
                    if(_searchText.length < 3)
                    {
                        binding.tilCreateNewListThirdItemSearchFreinds.error = getString(R.string.enter_at_least_3_characters)
                        binding.pbCreateNewListThirdItemSearchFriends.visibility = View.GONE
                    }
                    else
                    {
                        binding.tilCreateNewListThirdItemSearchFreinds.error = null
                        binding.pbCreateNewListThirdItemSearchFriends.visibility = View.VISIBLE
                    }
                }
            }

            binding.actvCreateNewListThirdItemSearchFriends.threshold = 3

            binding.actvCreateNewListThirdItemSearchFriends.setAdapter(userAutoCompleteAdapter)

            binding.actvCreateNewListThirdItemSearchFriends.setOnItemClickListener{ parent, view, position, arg3 ->
                binding.actvCreateNewListThirdItemSearchFriends.text = null
                val selectedFriend = parent.getItemAtPosition(position) as User

                if(createNewListFragmentViewModel.isUserAlreadySelected(context, binding.actvCreateNewListThirdItemSearchFriends, selectedFriend))
                {
                    Toast.makeText(context, R.string.you_have_already_added_this_friend, Toast.LENGTH_LONG).show()
                }
                else
                {
                    binding.tilCreateNewListThirdItemSearchFreinds.error = null
                    createNewListFragmentViewModel.insertToFriends(selectedFriend)
                    createNewListFragmentViewModel.addFriendChip(context, selectedFriend, binding.chgCreateNewListThirdItemFriends)
                }
            }

            listItemAdapter = ListItemAdapter(context, mutableListOf<ListItem>())
            listItemAdapter.removeAllItems()
            binding.rvCreateNewListFourthItem.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listItemAdapter
            }
        }

        binding.tvCreateNewListSecondItemDueDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.fabCreateNewListFourthItemAddNewItem.setOnClickListener {
            findNavController().navigate(R.id.action_createNewListFragment_to_addNewItemDialog)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        firebaseViewModel.usersListLD.observe(viewLifecycleOwner, Observer { usersList ->
            userAutoCompleteAdapter.updateUsersList(usersList)
            binding.pbCreateNewListThirdItemSearchFriends.visibility = View.GONE
        })

        createNewListFragmentViewModel.itemsListAction.observe(viewLifecycleOwner) { result ->
            val (crud, position, listItem) = result
            when(crud)
            {
                Crud.CREATE -> {
                    listItem?.let {
                        listItemAdapter.addItem(listItem)
                    }
                }
                Crud.UPDATE -> {
                    position?.let {
                        listItem?.let {
                            listItemAdapter.updateItem(position, listItem)
                        }
                    }

                }
                Crud.DELETE -> {
                    position?.let {
                        listItemAdapter.removeItem(position)
                    }

                }
                else -> null
            }
        }

        binding.llSaveList.setOnClickListener {
//            firebaseViewModel.insertListItemInFirestore()
            findNavController().navigate(R.id.action_createNewListFragment_to_mainFragment)
        }

        listItemAdapter.setOnItemClickListener(object : ListItemAdapter.OnItemClickListener
        {
            override fun onUpdateClick(position: Int, listItem: ListItem)
            {
                listItemAdapter.updateItem(position, listItem)
            }

            override fun onDeleteClick(position: Int)
            {
                listItemAdapter.removeItem(position)
            }
        })
    }

    override fun onResume()
    {
        super.onResume()
        Log.d(TAG, "Size_3: ${listItemAdapter.itemCount}")
        listItemAdapter.removeAllItems()
        Log.d(TAG, "Size_5: ${listItemAdapter.itemCount}")
    }
    override fun onDestroy()
    {
        super.onDestroy()

        createNewListFragmentViewModel.clearListOfFriends()
        listItemAdapter.removeAllItems()
    }

    private fun setTodaysDate()
    {
        val today = CurrentDateFunctions.getToday()
        createNewListFragmentViewModel.selectedDueDate = today.toDate()
    }

    fun showDatePickerDialog()
    {
        context?.let {context ->
            val datePickerDialog = DatePickerDialog(
                context,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = DateTime.now().millis
            datePickerDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)
    {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val formatedDate: String = sdf.format(calendar.time)
        val date: Date = sdf.parse(formatedDate)

        createNewListFragmentViewModel.selectedDueDate = date

        binding.tvCreateNewListSecondItemDueDate.text = date.toLocaleString().substringBeforeLast(" ")
        context?.let {context ->
            binding.tvCreateNewListSecondItemDueDate.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }
    }
}
