package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.datepicker.MaterialDatePicker

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentFilterShoppingListBinding
import com.inspirecoding.supershopper.enums.ShoppingListStatus
import com.inspirecoding.supershopper.viewmodels.FilterShoppingListViewModel
import java.util.*

private const val TAG = "FilterShoppingList"
class FilterShoppingListFragment : Fragment()
{
    private lateinit var binding: FragmentFilterShoppingListBinding
    private val filterViewModel by navGraphViewModels<FilterShoppingListViewModel>(R.id.navigation_graph)

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

        populateForm()

        binding.tvDueDateRange.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnActionButtonsApplyFilter.setOnClickListener {
            /** Set filter **/
            setFilters()
            /** Apply the new filters **/
            filterViewModel.onFilterChangedClickListener.onFilterChanged()

            findNavController().navigate(R.id.action_filterShoppingListFragment_to_mainFragment)
        }
    }
    private fun showDatePickerDialog()
    {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance()

        if (filterViewModel.fromToDueDate != null)
        {
            val fromDueDate = (filterViewModel.fromToDueDate as Pair).first!!.time
            val toDueDate = (filterViewModel.fromToDueDate as Pair).second!!.time
            builder.setSelection(Pair(fromDueDate, toDueDate))
        }
        else if (filterViewModel.fromToDueDate_temp != null)
        {
            val fromDueDate = (filterViewModel.fromToDueDate_temp as Pair).first!!.time
            val toDueDate = (filterViewModel.fromToDueDate_temp as Pair).second!!.time
            builder.setSelection(Pair(fromDueDate, toDueDate))
        }
        else
        {
            builder.setSelection(Pair(now.timeInMillis, now.timeInMillis))
        }

        val picker = builder.build()

        picker.show(parentFragmentManager, "picker")

        picker.addOnPositiveButtonClickListener { pairOfTimePeriod ->
            /** Temp Pair is needed because it is not sure that the user will apply the filter
             *  But the selected date has to be saved and showed for the user
             */
            filterViewModel.setTempDueDate(pairOfTimePeriod)
            filterViewModel.fromToDueDate_temp?.let {
                populateDueDate(it)
            }
        }
    }
    private fun populateForm()
    {
        /** Shopping list statuses **/
        for(shoppingListStatus in filterViewModel.listOfShoppingListStatus)
        {
            when (shoppingListStatus)
            {
                ShoppingListStatus.OPEN -> {
                    binding.chbStatusOpenList.isChecked = true
                }
                ShoppingListStatus.DONE -> {
                    binding.chbStatusDoneList.isChecked = true
                }
                ShoppingListStatus.CLOSED -> {
                    binding.chbStatusClosedList.isChecked = true
                }
            }
        }
        /** Name of shopping list **/
        if (filterViewModel.name != null)
        {
            binding.etName.setText(filterViewModel.name)
        }
        else
        {
            binding.etName.setText("")
        }
        /** Due date **/
        if (filterViewModel.fromToDueDate != null)
        {
            filterViewModel.fromToDueDate?.let {
                populateDueDate(it)
            }
        }
        else
        {
            binding.etName.setText("")
        }
    }
    private fun populateDueDate(fromToDueDate: Pair<Date, Date>)
    {
        val fromDueDate = fromToDueDate.first!!.toLocaleString()!!.substringBeforeLast(" ")
        val toDueDate = fromToDueDate.second!!.toLocaleString()!!.substringBeforeLast(" ")
        binding.tvDueDateRange.text = String.format(resources.getString(R.string.from_to), fromDueDate, toDueDate)
    }

    private fun setFilters()
    {
        /** When the user click on the Apply button, then the date can be moved to the permanent variable **/
        filterViewModel.fromToDueDate = filterViewModel.fromToDueDate_temp
        filterViewModel.setShoppingListFilter(
            isOpenChecked = binding.chbStatusOpenList.isChecked,
            isDoneChecked = binding.chbStatusDoneList.isChecked,
            isClosedChecked = binding.chbStatusClosedList.isChecked)
        filterViewModel.setNameFilter(binding.etName.text.toString())
    }
}
