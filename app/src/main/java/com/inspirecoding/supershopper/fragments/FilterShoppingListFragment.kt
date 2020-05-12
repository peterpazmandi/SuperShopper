package com.inspirecoding.supershopper.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentFilterShoppingListBinding
import java.util.*

private const val TAG = "FilterShoppingList"
class FilterShoppingListFragment : Fragment()
{
    private lateinit var binding: FragmentFilterShoppingListBinding

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_filter_shopping_list, container, false)

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDueDateRange.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog()
    {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()
        builder.setSelection(Pair(now.timeInMillis, now.timeInMillis))

        val picker = builder.build()

        picker.show(parentFragmentManager, "picker")

        picker.addOnPositiveButtonClickListener {
            Toast.makeText(context, "From: ${it.first}, to: ${it.second}", Toast.LENGTH_LONG).show()
        }
    }
}
