package com.inspirecoding.supershopper.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.ListItemAdapter
import com.inspirecoding.supershopper.customview.adapter.UserAutoCompleteAdapter
import com.inspirecoding.supershopper.databinding.FragmentCreateNewListBinding
import com.inspirecoding.supershopper.utilities.CurrentDateFunctions
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.CreateNewListFragmentViewModel
import org.joda.time.DateTime
import org.koin.android.ext.android.inject
import java.util.*
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.inspirecoding.supershopper.adapter.FriendsListChipAdapter
import com.inspirecoding.supershopper.enums.Crud
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.utilities.ConverterFunctions
import com.inspirecoding.supershopper.viewmodels.MainFragmentViewModel
import com.inspirecoding.supershopper.viewmodels.SortShoppingListViewModel
import kotlinx.coroutines.launch


private const val TAG = "CreateNewListFragment"
class CreateNewListFragment : Fragment(), DatePickerDialog.OnDateSetListener
{
    private lateinit var binding: FragmentCreateNewListBinding

    private val firebaseViewModel: FirebaseViewModel by inject()
    private lateinit var userAutoCompleteAdapter: UserAutoCompleteAdapter
    private val createNewListFragmentViewModel: CreateNewListFragmentViewModel by navGraphViewModels(R.id.navigation_graph)
    private val mainFragmentViewModel by navGraphViewModels<MainFragmentViewModel>(R.id.navigation_graph)

    private lateinit var listItemAdapter: ListItemAdapter
    private lateinit var friendsListChipAdapter: FriendsListChipAdapter

    private var selectedShoppingList: ShoppingList? = null
    private var selectedPosition = -1

    private var shoppingListMLD = MutableLiveData<ShoppingList>()

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_create_new_list, container, false)

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        createNewListFragmentViewModel.clearItemsListActionLD()

        userAutoCompleteAdapter = UserAutoCompleteAdapter(this, firebaseViewModel)
        binding.actvCreateNewListThirdItemSearchFriends.threshold = 3
        binding.actvCreateNewListThirdItemSearchFriends.setAdapter(userAutoCompleteAdapter)

        setTodayDateForDueDateSelection()

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
                        userAutoCompleteAdapter.notifyDataSetChanged()
                    }
                }
            }

            binding.actvCreateNewListThirdItemSearchFriends.setOnItemClickListener{ parent, view, position, arg3 ->
                binding.actvCreateNewListThirdItemSearchFriends.text = null
                val selectedFriend = parent.getItemAtPosition(position) as Friend

                if(createNewListFragmentViewModel.isUserAlreadySelected(
                        context, binding.root, selectedFriend.friendId))
                {
                    Toast.makeText(context, R.string.you_have_already_added_this_friend, Toast.LENGTH_LONG).show()
                }
                else
                {
                    firebaseViewModel.viewModelScope.launch {
                        val selectedUser = firebaseViewModel.getUserFromFirestore(selectedFriend.friendId)
                        selectedUser?.let { _selectedUser ->
                            binding.tilCreateNewListThirdItemSearchFreinds.error = null
                            createNewListFragmentViewModel.listOfFriendsIds.add(selectedFriend.friendId)
                            friendsListChipAdapter.addFriend(_selectedUser)
                        }
                    }
                }
            }

            initFriendListRecyclerView(context)
            initListItemRecyclerView(context)
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

        val safeArgs: CreateNewListFragmentArgs by navArgs()
        selectedShoppingList = safeArgs.shoppingList

        shoppingListMLD.postValue(selectedShoppingList)

        Log.i(TAG, "$selectedShoppingList")
        Log.i(TAG, "$selectedPosition")

        if(selectedShoppingList == null)
        {
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.create_new_list)
            binding.btnSaveList.text = getString(R.string.save_list)
        }
        else
        {
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.edit_list)
            binding.btnSaveList.text = getString(R.string.edit_list)

            setHasOptionsMenu(true)

            populateForm(selectedShoppingList as ShoppingList)
        }

        binding.etCreateNewListFirstItemName.addTextChangedListener {
            binding.tilCreateNewListFirstItemName.error = null
        }

        firebaseViewModel.friendsListMLD.observe(viewLifecycleOwner) { friendsList ->
            val _usersList = createNewListFragmentViewModel
                .removeAlreadyAddedFriends(friendsList.toMutableList())

            userAutoCompleteAdapter.updateUsersList(_usersList)
            binding.pbCreateNewListThirdItemSearchFriends.visibility = View.GONE
        }

        /**
         * List of the items
         **/
        createNewListFragmentViewModel.itemsListActionLD.observe(viewLifecycleOwner) { result ->
            result?.let { _result ->
                val (crud, position, listItem) = _result
                when(crud)
                {
                    Crud.CREATE -> {
                        listItem?.let {
                            listItemAdapter.addItem(listItem)
                            binding.tvCreateNewListItemsError.visibility = View.INVISIBLE
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
        }

        binding.llSaveList.setOnClickListener {
            if(validateName() && validateDueDate() && validateItems())
            {
                val newShoppingList = createShoppingListObject()
                if(newShoppingList != null)
                {
                    if(selectedShoppingList == null)
                    {
                        /** Insert into the cloud shopping lists **/
                        firebaseViewModel.insertShoppingList(newShoppingList, this)

                        findNavController().navigate(R.id.action_createNewListFragment_to_mainFragment)
                    }
                    else
                    {
                        /** Update the cloud shopping lists **/
                        newShoppingList.shoppingListId = (selectedShoppingList as ShoppingList).shoppingListId
                        firebaseViewModel.updateShoppingList(newShoppingList)

                        /** Update the local temporary shopping lists **/
                        val positionToUpdate = mainFragmentViewModel.fullListOfShoppingLists.indexOfFirst {
                            it.shoppingListId == (selectedShoppingList as ShoppingList).shoppingListId
                        }
                        if(positionToUpdate != -1)
                        {
                            mainFragmentViewModel.fullListOfShoppingLists[positionToUpdate] = newShoppingList
                        }

                        findNavController().navigate(R.id.action_createNewListFragment_to_mainFragment)
                    }
                }
                else
                {
                    Toast.makeText(context, getString(R.string.sorry_something_went_wrong), Toast.LENGTH_SHORT).show()
                }
            }
        }

        friendsListChipAdapter.setOnItemClickListener(object: FriendsListChipAdapter.OnItemClickListener {
            override fun onDeleteClick(position: Int)
            {
                createNewListFragmentViewModel.listOfFriendsIds.removeAt(position)
                friendsListChipAdapter.removeFriend(position)
            }
        })
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

    private fun initFriendListRecyclerView(context: Context)
    {
        friendsListChipAdapter = FriendsListChipAdapter(context, this)
        binding.rvCreateNewListThirdItemFriends.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = friendsListChipAdapter
        }
    }
    private fun initListItemRecyclerView(context: Context)
    {
        listItemAdapter = ListItemAdapter(context)
        binding.rvCreateNewListFourthItem.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listItemAdapter
        }
    }

    private fun populateForm(shoppingList: ShoppingList)
    {
        context?.let { context ->
            binding.etCreateNewListFirstItemName.setText(shoppingList.name)
            binding.tvCreateNewListSecondItemDueDate.text = shoppingList.dueDate.toLocaleString().substringBeforeLast(" ")
            binding.tvCreateNewListSecondItemDueDate.setTextColor(ContextCompat.getColor(context, R.color.black))

            /** Remove all users from the list **/
            listItemAdapter.removeAllItems()
            listItemAdapter.addAllItem(shoppingList.listOfItems)

            /** Remove all friends from the list **/
            createNewListFragmentViewModel.listOfFriendsIds.clear()

            for(friendId in shoppingList.friendsSharedWith)
            {
                if(friendId != firebaseViewModel.currentUserLD.value?.id)
                {
                    createNewListFragmentViewModel.listOfFriendsIds.add(friendId)
                    firebaseViewModel.viewModelScope.launch {
                        val friend = firebaseViewModel.getUserFromFirestore(friendId)
                        friend?.let {user ->
                            friendsListChipAdapter.addFriend(user)
                        }
                    }
                }
            }
        }
    }

    private fun setTodayDateForDueDateSelection()
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
            datePickerDialog.setOnShowListener {  _datePickerDialog ->
                /** Set the color of the action buttons, otherwise it will get it form the primary color! **/
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
            }
            datePickerDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)
    {
        val date = ConverterFunctions.convertToDate(year, month, dayOfMonth)

        createNewListFragmentViewModel.selectedDueDate = date

        binding.tvCreateNewListSecondItemDueDate.text = date.toLocaleString().substringBeforeLast(" ")
        context?.let {context ->
            binding.tvCreateNewListSecondItemDueDate.setTextColor(ContextCompat.getColor(context, R.color.black))
            binding.tvCreateNewListDueDateError.visibility = View.INVISIBLE
        }
    }



    private fun createShoppingListObject(): ShoppingList?
    {
        val idsOfFriends = mutableListOf<String>()
        firebaseViewModel.currentUserLD.value?.id?.let {id ->
            idsOfFriends.clear()
            idsOfFriends.add(id)
            for(friend in friendsListChipAdapter.getFrindsList())
            {
                idsOfFriends.add(friend.id)
            }

            return ShoppingList (
                "",
                System.currentTimeMillis(),
                binding.etCreateNewListFirstItemName.text.toString(),
                ShoppingListStatus.OPEN,
                createNewListFragmentViewModel.selectedDueDate ?: CurrentDateFunctions.getToday().toDate(),
                idsOfFriends,
                listItemAdapter.getItemsList()
            )
        }

        return null
    }



    private fun validateName(): Boolean
    {
        return if(binding.etCreateNewListFirstItemName.text.toString().isEmpty())
        {
            binding.tilCreateNewListFirstItemName.error = getString(R.string.enter_a_name)
            false
        }
        else
        {
            true
        }
    }
    private fun validateDueDate(): Boolean
    {
        return if(binding.tvCreateNewListSecondItemDueDate.text.toString().equals(getString(R.string.select_a_due_date)))
        {
            binding.tvCreateNewListDueDateError.visibility = View.VISIBLE
            false
        }
        else
        {
            binding.tvCreateNewListDueDateError.visibility = View.INVISIBLE
            true
        }
    }
    private fun validateItems(): Boolean
    {
        val listSize = listItemAdapter.itemCount
        return if (listSize == 0)
        {
            binding.tvCreateNewListItemsError.visibility = View.VISIBLE
            false
        }
        else
        {
            binding.tvCreateNewListItemsError.visibility = View.INVISIBLE
            true
        }
    }

    private fun navigateCloseDeleteFragment(view: View, shoppingList: ShoppingList, closeDelete: String)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = CreateNewListFragmentDirections.actionCreateNewListFragmentToCloseDeleteDialog(shoppingList, closeDelete)
        navController.navigate(action)
    }


    override fun onPrepareOptionsMenu(menu: Menu)
    {
        val alertMenuItem = menu.findItem(R.id.item_open_close)
        val rootView = alertMenuItem.actionView
        val ivOpenClose = rootView.findViewById(R.id.iv_openClose) as ImageView

        shoppingListMLD.observe(this) { _shoppingList ->
            Log.d(TAG, "1_shoppingList: $_shoppingList")
            when(_shoppingList.shoppingListStatus)
            {
                ShoppingListStatus.OPEN -> {
                    ivOpenClose.setImageResource(R.drawable.ic_close_orange)
                    ivOpenClose.visibility = View.VISIBLE
                }
                ShoppingListStatus.DONE -> {
                    ivOpenClose.visibility = View.INVISIBLE
                }
                ShoppingListStatus.CLOSED -> {
                    ivOpenClose.setImageResource(R.drawable.ic_open_green)
                    ivOpenClose.visibility = View.VISIBLE
                }
            }
        }

        ivOpenClose.setOnClickListener {
            selectedShoppingList?.let { _selectedShoppingList ->
                when(_selectedShoppingList.shoppingListStatus)
                {
                    ShoppingListStatus.OPEN -> {
                        navigateCloseDeleteFragment(binding.root, _selectedShoppingList, CloseDeleteFragment.CLOSED)
                    }
                    ShoppingListStatus.DONE -> {
                        /** Don't do anything **/
                    }
                    ShoppingListStatus.CLOSED -> {
                        (selectedShoppingList as ShoppingList).shoppingListStatus = ShoppingListStatus.OPEN
                        shoppingListMLD.postValue(selectedShoppingList)
                        firebaseViewModel.updateShoppingList(selectedShoppingList as ShoppingList)
                    }
                }
            }
        }

        super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_close_delete_shoppinglist, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId)
        {
            R.id.item_open_close -> {
                true
            }
            R.id.item_delete -> {
                selectedShoppingList?.let { _selectedShoppingList ->
                    navigateCloseDeleteFragment(binding.root, _selectedShoppingList, CloseDeleteFragment.DELETE)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
