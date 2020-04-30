package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import androidx.navigation.navGraphViewModels
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentDetailsBinding
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.CreateNewListFragmentViewModel
import com.inspirecoding.supershopper.viewmodels.ShoppingListFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "DetailsFragment"
class DetailsFragment() : Fragment()
{
    private lateinit var binding: FragmentDetailsBinding
    private val shoppingListFragmentViewModel by navGraphViewModels<ShoppingListFragmentViewModel>(R.id.navigation_graph)
    private val createNewListFragmentViewModel: CreateNewListFragmentViewModel by navGraphViewModels(R.id.navigation_graph)
    private val firebaseViewModel: FirebaseViewModel by inject()

    override fun onCreateView (layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_details, container, false)

        firebaseViewModel.getShoppingListRealTime(shoppingListFragmentViewModel.openedShoppingList.id).observe(viewLifecycleOwner) { listOfShoppingLists ->
            Log.d(TAG, "$listOfShoppingLists")
            for(key in listOfShoppingLists.keys)
            {
                val shoppingList = listOfShoppingLists[key]
                shoppingList?.let { shoppingListItem ->
                    populateDetailsForm(shoppingListItem)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume()
    {
        super.onResume()
    }

    private fun populateDetailsForm(shoppingList: ShoppingList)
    {
        binding.tvName.text = shoppingList.name
        var creator: User? = User()
        firebaseViewModel.viewModelScope.launch {
            creator = firebaseViewModel.getUserFromFirestore(shoppingList.friendsSharedWith[0])
            binding.tvCreator.text = creator?.name
        }
        binding.tvDueDate.text = shoppingList.dueDate.toLocaleString().substringBeforeLast(" ")

        context?.let {_context ->
            for (friendId in shoppingList.friendsSharedWith)
            {
                if (friendId != firebaseViewModel.currentUserLD.value?.id)
                {
                    firebaseViewModel.viewModelScope.launch {
                        val friend = firebaseViewModel.getUserFromFirestore(friendId)
                        friend?.let { _friend ->
                            createNewListFragmentViewModel.addFriendChip(_context, _friend, binding.chgFriends)
                            createNewListFragmentViewModel.listOfFriends.add(_friend)
                        }
                    }
                }
            }
        }
    }
}
