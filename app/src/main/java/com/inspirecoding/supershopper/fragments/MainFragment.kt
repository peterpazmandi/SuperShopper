package com.inspirecoding.supershopper.fragments

import android.content.Context
import android.graphics.drawable.AnimationDrawable
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
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.adapter.ShoppingListAdapter
import com.inspirecoding.supershopper.databinding.FragmentMainBinding
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.viewmodels.FilterShoppingListViewModel
import com.inspirecoding.supershopper.viewmodels.MainFragmentViewModel
import com.inspirecoding.supershopper.viewmodels.SortShoppingListViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    private lateinit var cartLoadingAnimation: AnimationDrawable

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

        /** Init cart loading animation **/
        binding.ivCartLoading.setBackgroundResource(R.drawable.anim_cart_loading)
        cartLoadingAnimation = binding.ivCartLoading.background as AnimationDrawable

        /** Init shopping lists RecyclerView **/
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
            /** Start cart loading animation **/
            startCartLoadingAnimation()
            showHideEmptyCart(null)
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

        firebaseViewModel.currentUserLD.observe(viewLifecycleOwner) { user ->
            firebaseViewModel.getCurrentUserShoppingListsRealTime(user).observe(viewLifecycleOwner) { mapOfShoppingLists ->
                Log.i(TAG, "${mapOfShoppingLists.size}")
                for(key in mapOfShoppingLists.keys)
                {
                    when(key.type)
                    {
                        DocumentChange.Type.ADDED -> {
                            val shoppingList = mapOfShoppingLists.get(key)
                            shoppingList?.let { _shoppingList ->
                                firebaseViewModel.viewModelScope.launch {
                                    /** Some delay to show for the user the cart loading animation longer **/
                                    delay(1_500)

                                    /** Order the shopping lists in date **/
                                    val intoPosition = sortShoppingListViewModel.getPositionsForShoppingListOrderingByDueDate(_shoppingList, shoppingListAdapter.getAllShoppingList())
                                    mainFragmentViewModel.addShoppingList(intoPosition, _shoppingList)
                                    val filteredList = filterShoppingListViewModel.runFilter(mainFragmentViewModel.fullListOfShoppingLists)

                                    refreshFilterBadge()

                                    /** Add to the full shopping lists **/
                                    shoppingListAdapter.addAllShoppingListItem(filteredList)

                                    /** Stop cart loading animation **/
                                    stopCartLoadingAnimation()

                                    /** Show/Hide empty cart screen based on the number of shopping lists **/
                                    showHideEmptyCart(shoppingListAdapter.itemCount)
                                }
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

                refreshFilterBadge()
            }
        })
    }


    private fun addAllShoppingListOrShowEmpty(filteredList: MutableList<ShoppingList>)
    {
        if (filteredList.size != 0)
        {
            /** Update adapter of shopping lists **/
            shoppingListAdapter.addAllShoppingListItem(filteredList)
        }
        else
        {
            /** No shopping list found **/
        }
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

    private fun refreshFilterBadge()
    {
        if(filterShoppingListViewModel.numberOfFilters > 0)
        {
            binding.tvActiveFitersBadge.text = filterShoppingListViewModel.numberOfFilters.toString()
            binding.tvActiveFitersBadge.visibility = View.VISIBLE
            Log.i(TAG, filterShoppingListViewModel.getNumberOfActiveFilters().toString())
        }
        else
        {
            binding.tvActiveFitersBadge.visibility = View.GONE
            Log.i(TAG, binding.tvActiveFitersBadge.text.toString())
        }
    }
    private fun refreshFriendRequestsBadge()
    {
        if(mainFragmentViewModel.numberOfReceivedFriendRequests > 0)
        {
            binding.tvFriendRequests.text = mainFragmentViewModel.numberOfReceivedFriendRequests.toString()
            binding.tvFriendRequests.visibility = View.VISIBLE
        }
        else
        {
            binding.tvFriendRequests.visibility = View.GONE
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
    private fun startCartLoadingAnimation()
    {
        binding.ivCartLoading.visibility = View.VISIBLE
        cartLoadingAnimation.start()
    }
    private fun stopCartLoadingAnimation()
    {
        binding.ivCartLoading.visibility = View.GONE
        cartLoadingAnimation.stop()
    }
    private fun showFabs()
    {
        binding.fabFilterList.show()
        binding.fabCreateNewList.show()
        binding.fabFriends.show()

        if(filterShoppingListViewModel.numberOfFilters > 0)
        {
            binding.tvActiveFitersBadge.visibility = View.VISIBLE
        }
        if(mainFragmentViewModel.numberOfReceivedFriendRequests > 0)
        {
            binding.tvFriendRequests.visibility = View.VISIBLE
        }
    }
    private fun hideFabs()
    {
        binding.fabFilterList.hide()
        binding.fabCreateNewList.hide()
        binding.fabFriends.hide()

        binding.tvActiveFitersBadge.visibility = View.INVISIBLE
    }
    private fun showHideEmptyCart(shoppingListsCount: Int?)
    {
        if(shoppingListsCount != null)
        {
            if(shoppingListsCount > 0)
            {
                binding.rvShoppingLists.visibility = View.VISIBLE
                binding.llEmptyCart.visibility = View.INVISIBLE
            }
            else
            {
                binding.rvShoppingLists.visibility = View.INVISIBLE
                binding.llEmptyCart.visibility = View.VISIBLE
            }
        }
        else
        {
            binding.rvShoppingLists.visibility = View.INVISIBLE
            binding.llEmptyCart.visibility = View.INVISIBLE
        }
    }
}
