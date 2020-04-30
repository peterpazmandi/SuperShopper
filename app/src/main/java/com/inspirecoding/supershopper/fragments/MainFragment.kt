package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.ShoppingListAdapter
import com.inspirecoding.supershopper.databinding.FragmentMainBinding
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.MainFragmentViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import org.koin.android.ext.android.inject

private const val TAG = "MainFragment"
class MainFragment : Fragment()
{
    private lateinit var binding: FragmentMainBinding
    private lateinit var ivProfilePicture: ImageView

    private val firebaseViewModel: FirebaseViewModel by inject()
    private val mainFragmentViewModel by navGraphViewModels<MainFragmentViewModel>(R.id.navigation_graph)

    private lateinit var shoppingListAdapter: ShoppingListAdapter

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)

        context?.let { context ->
            shoppingListAdapter = ShoppingListAdapter(context, firebaseViewModel)
            binding.rvShoppingLists.apply {
                adapter = shoppingListAdapter
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        // Create new item
        binding.fabCreateNewList.setOnClickListener {view ->
            navigateToCreateNewList(view)
        }

        firebaseViewModel.currentUserLD.observe(viewLifecycleOwner) {user ->
            firebaseViewModel.getCurrentUserShoppingListsRealTime(user).observe(viewLifecycleOwner) {listOfShoppingLists ->
                Log.d(TAG, "${listOfShoppingLists.size}")
                for(key in listOfShoppingLists.keys)
                {
                    when(key.type)
                    {
                        DocumentChange.Type.ADDED -> {
                            Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                            val listOfToDos = listOfShoppingLists.get(key)
                            listOfToDos?.let { shoppingList ->
                                shoppingListAdapter.addShoppingListItem(shoppingList)

                                if(shoppingList.id  == mainFragmentViewModel.selectedShoppingList.id)
                                {
                                    val position = shoppingListAdapter.getPositionOfShoppingListItem(shoppingList)
                                    openShoppingList(shoppingList, position)
                                }
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                            val listOfToDos = listOfShoppingLists.get(key)
                            listOfToDos?.let { shoppingList ->
                                val position = shoppingListAdapter.getPositionOfShoppingListItem(shoppingList)
                                shoppingListAdapter.updateShoppingListItem(position, shoppingList)

                                if(shoppingList.id  == mainFragmentViewModel.selectedShoppingList.id)
                                {
                                    openShoppingList(shoppingList, position)
                                }
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                            val listOfToDos = listOfShoppingLists.get(key)
                            listOfToDos?.let { shoppingList ->
                                val position = shoppingListAdapter.getPositionOfShoppingListItem(shoppingList)
                                shoppingListAdapter.removeShoppingListItem(position)

                                if(shoppingList.id  == mainFragmentViewModel.selectedShoppingList.id)
                                {
                                    openShoppingList(shoppingList, position)
                                }
                            }
                        }
                    }
                }

                if(shoppingListAdapter.itemCount > 0)
                {
                    openShoppingList(shoppingListAdapter.getShoppingListItemFromPosition(0), 0)
                }
            }
        }
    }

    private fun openShoppingList(shoppingList: ShoppingList, position: Int)
    {
        mainFragmentViewModel.selectedShoppingList = shoppingList
        mainFragmentViewModel.selectedPosition = position
    }

    private fun navigateToCreateNewList(view: View)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = MainFragmentDirections.actionMainFragmentToCreateNewListFragment()
        navController.navigate(action)
    }

    private fun setProfilePictures(user: User?, imageView: ImageView)
    {
        user?.let {
            Log.d(TAG, "$user _2")
            if(user.profilePicture.isNotEmpty())
            {
                Log.d(TAG, "$user _3")
                Picasso
                    .get()
                    .load(user.profilePicture)
                    .placeholder(R.drawable.ic_person)
                    .into(imageView)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu)
    {
        val alertMenuItem = menu.findItem(R.id.menu_profilePicture)
        val rootView = alertMenuItem.actionView
        ivProfilePicture = rootView.findViewById(R.id.civ_profilePicture)

        firebaseViewModel.currentUserLD.observe(this) { user ->
            Log.d(TAG, "$user _1")
            setProfilePictures(user, ivProfilePicture)
        }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_mainfragment_profilepicture, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}
