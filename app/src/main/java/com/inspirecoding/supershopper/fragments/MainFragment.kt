package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.DocumentChange

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentMainBinding
import com.inspirecoding.supershopper.repository.FirebaseViewModel
import org.koin.android.ext.android.inject

private const val TAG = "MainFragment"
class MainFragment : Fragment()
{
    private lateinit var binding: FragmentMainBinding
    private val firebaseViewModel: FirebaseViewModel by inject()

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.fabCreateNewList.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_createNewListFragment)
        }


        firebaseViewModel.getCurrentUserShoppingListsRealTime().observe(viewLifecycleOwner) {listOfShoppingLists ->
            Log.d(TAG, "${listOfShoppingLists.size}")
            for(key in listOfShoppingLists.keys)
            {
                when(key.type)
                {
                    DocumentChange.Type.ADDED -> {
                        Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                        val listOfToDos = listOfShoppingLists.get(key)
                        listOfToDos?.let {todo ->
//                            toDoAdapter.addToDo(todo)
                        }
                    }

                    DocumentChange.Type.MODIFIED -> {
                        Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                        val listOfToDos = listOfShoppingLists.get(key)
                        listOfToDos?.let {todo ->
//                            val position = toDoAdapter.getPositionOfToDo(todo)
//                            toDoAdapter.updateToDo(position, todo)
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        Log.d(TAG, "${key.type}: ${listOfShoppingLists.get(key)}")
                        val listOfToDos = listOfShoppingLists.get(key)
                        listOfToDos?.let {todo ->
//                            val position = toDoAdapter.getPositionOfToDo(todo)
//                            toDoAdapter.removeToDo(position)
                        }
                    }
                }
            }
        }
    }
}
