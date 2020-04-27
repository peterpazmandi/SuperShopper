package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentShoppingListDetailsBinding


class ShoppingListDetailsFragment : Fragment()
{
    private lateinit var binding: FragmentShoppingListDetailsBinding

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_shopping_list_details, container, false)



        return binding.root
    }
}
