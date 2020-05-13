package com.inspirecoding.supershopper.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.ShoppingListAdapter
import com.inspirecoding.supershopper.databinding.FragmentMainBinding
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.FilterShoppingListViewModel
import com.inspirecoding.supershopper.viewmodels.MainFragmentViewModel
import com.inspirecoding.supershopper.viewmodels.SortShoppingListViewModel
import com.squareup.picasso.Picasso
import org.koin.android.ext.android.inject

private const val TAG = "MainFragment"
class MainFragment : Fragment()
{
    private lateinit var binding: FragmentMainBinding
    private lateinit var ivProfilePicture: ImageView

    private val firebaseViewModel: FirebaseViewModel by inject()
    private val mainFragmentViewModel by navGraphViewModels<MainFragmentViewModel>(R.id.navigation_graph)
    private val sortShoppingListViewModel by navGraphViewModels<SortShoppingListViewModel>(R.id.navigation_graph)
    private val filterShoppingListViewModel by navGraphViewModels<FilterShoppingListViewModel>(R.id.navigation_graph)

    private lateinit var shoppingListAdapter: ShoppingListAdapter

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(null)


        context?.let { context ->
            context.hideKeyboard(binding.root)
            shoppingListAdapter = ShoppingListAdapter(context, firebaseViewModel)
            binding.rvShoppingLists.apply {
                adapter = shoppingListAdapter
            }
        }

        firebaseViewModel.toast.observe(viewLifecycleOwner) { message ->
            if(message.first)
            {
                Toast.makeText(context, message.second, Toast.LENGTH_SHORT).show()
            }
        }
        firebaseViewModel.spinner.observe(viewLifecycleOwner) { show ->

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        // Create new item
        binding.fabFilterList.setOnClickListener { _view ->
            navigateToFilterShoppingList(_view)
        }
        binding.fabCreateNewList.setOnClickListener { _view ->
            navigateToCreateNewList(_view)
        }
        binding.fabFriends.setOnClickListener { _view ->
            navigateToFriends(_view)
        }

        firebaseViewModel.currentUserLD.observe(viewLifecycleOwner) {user ->
            firebaseViewModel.getCurrentUserShoppingListsRealTime(user).observe(viewLifecycleOwner) { mapOfShoppingLists ->
                Log.d(TAG, "${mapOfShoppingLists.size}")
                for(key in mapOfShoppingLists.keys)
                {
                    when(key.type)
                    {
                        DocumentChange.Type.ADDED -> {
                            val shoppingList = mapOfShoppingLists.get(key)
                            shoppingList?.let { _shoppingList ->
                                /** Add to the full shopping lists **/
                                val intoPosition = sortShoppingListViewModel.getPositionsForShoppingListOrderingByDueDate(_shoppingList, shoppingListAdapter.getAllShoppingList())
                                mainFragmentViewModel.addShoppingList(intoPosition, _shoppingList)
                                val filteredList = filterShoppingListViewModel.runFilter(mainFragmentViewModel.fullListOfShoppingLists)
                                shoppingListAdapter.addAllShoppingListItem(filteredList)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            val shoppingList = mapOfShoppingLists.get(key)
                            shoppingList?.let { _shoppingList ->
                                val position = shoppingListAdapter.getPositionOfShoppingListItem(_shoppingList)
                                shoppingListAdapter.updateShoppingListItem(position, _shoppingList)
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            val shoppingList = mapOfShoppingLists.get(key)
                            shoppingList?.let { _shoppingList ->
                                val position = shoppingListAdapter.getPositionOfShoppingListItem(_shoppingList)
                                shoppingListAdapter.removeShoppingListItem(position)
                            }
                        }
                    }
                }
            }
        }

        binding.rvShoppingLists.addOnScrollListener(object: RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
            {
                if(dy > 0)
                {
                    hideFabs()
                }
                else
                {
                    showFabs()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        filterShoppingListViewModel.setFilterChangedClickListener(object : FilterShoppingListViewModel.OnFilterChangedClickListener
        {
            override fun onFilterChanged()
            {
                val filteredList = filterShoppingListViewModel.runFilter(mainFragmentViewModel.fullListOfShoppingLists)
                shoppingListAdapter.addAllShoppingListItem(filteredList)
            }
        })
    }

    private fun navigateToFilterShoppingList(view: View)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = MainFragmentDirections.actionMainFragmentToFilterShoppingListFragment()
        navController.navigate(action)
    }
    private fun navigateToCreateNewList(view: View)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = MainFragmentDirections.actionMainFragmentToCreateNewListFragment()
        navController.navigate(action)
    }
    private fun navigateToFriends(view: View)
    {
        val navController: NavController = Navigation.findNavController(view)
        val action = MainFragmentDirections.actionMainFragmentToFriendsMainFragment()
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
                    .error(R.drawable.profilepicture_blank)
                    .placeholder(R.drawable.profilepicture_blank)
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

        ivProfilePicture.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_profileFragment)
        }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        inflater.inflate(R.menu.menu_mainfragment_profilepicture, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }



    fun Context.hideKeyboard(view: View)
    {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    private fun showFabs()
    {
        binding.fabFilterList.show()
        binding.fabCreateNewList.show()
        binding.fabFriends.show()
    }
    private fun hideFabs()
    {
        binding.fabFilterList.hide()
        binding.fabCreateNewList.hide()
        binding.fabFriends.hide()
    }
}
