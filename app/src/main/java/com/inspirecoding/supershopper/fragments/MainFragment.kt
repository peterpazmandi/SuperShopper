package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentMainBinding

class MainFragment : Fragment()
{
    private lateinit var binding: FragmentMainBinding

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
    }
}
