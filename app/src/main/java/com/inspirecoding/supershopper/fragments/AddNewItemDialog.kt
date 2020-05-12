package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentAddNewItemDialogBinding
import com.inspirecoding.supershopper.enums.Prioirities
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.viewmodels.CreateNewListFragmentViewModel
import java.util.*

private const val TAG = "AddNewItemDialog"
class AddNewItemDialog : BottomSheetDialogFragment()
{
    private lateinit var binding: FragmentAddNewItemDialogBinding

    private val createNewListFragmentViewModel by navGraphViewModels<CreateNewListFragmentViewModel>(R.id.navigation_graph)

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_add_new_item_dialog, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        val safeArgs: AddNewItemDialogArgs by navArgs()
        val listItem = safeArgs.listItem
        val position = safeArgs.position

        listItem?.let {
            populateForm(it)
            binding.tvSaveUpdateItem.text = getString(R.string.update_item)
        }

        binding.etCreateNewItemFirstItemName.addTextChangedListener {
            binding.tilCreateNewItemFirstItemName.error = null
        }
        binding.chgCreateNewItemUnit.setOnCheckedChangeListener { chipGroup, i ->
            if(binding.chgCreateNewItemUnit.checkedChipId == -1)
            {
                binding.tvAddNewItemUnitError.visibility = View.VISIBLE
            }
            else
            {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
            }
        }
        binding.chgCreateNewItemPriority.setOnCheckedChangeListener { chipGroup, i ->
            if(binding.chgCreateNewItemPriority.checkedChipId == -1)
            {
                binding.tvAddNewItemPriorityError.visibility = View.VISIBLE
            }
            else
            {
                binding.tvAddNewItemPriorityError.visibility = View.INVISIBLE
            }
        }
        binding.etCreateNewItemThirdItemQuantity.addTextChangedListener {
            binding.tilCreateNewItemThirdItemQuantity.error = null
        }

        binding.llSaveItem.setOnClickListener {
            if(listItem == null)
            {
                if(validateName() &&
                    validateSelectedUnit() != "" &&
                    validateQuantity() &&
                    validateSelectedPriority() != Prioirities.EMPTY)
                {
                    val _listItem = createListItemObject()
                    Log.d(TAG, "$_listItem")
                    createNewListFragmentViewModel.addItem(_listItem)
                    dismiss()
                }
            }
            else
            {
                val _listItem = createListItemObject()
                createNewListFragmentViewModel.updateItem(position, _listItem)
                dismiss()
            }
        }
    }

    private fun validateName(): Boolean
    {
        return if(binding.etCreateNewItemFirstItemName.text.toString().isEmpty())
        {
            binding.tilCreateNewItemFirstItemName.error = getString(R.string.enter_a_name)
            false
        }
        else
        {
            true
        }
    }
    private fun validateSelectedUnit(): String
    {
        return when(binding.chgCreateNewItemUnit.checkedChipId)
        {
            R.id.chip_createNewItem_gram -> {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
                getString(R.string.g)
            }
            R.id.chip_createNewItem_dag -> {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
                getString(R.string.dag)
            }
            R.id.chip_createNewItem_kg -> {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
                getString(R.string.kg)
            }
            R.id.chip_createNewItem_ml -> {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
                getString(R.string.ml)
            }
            R.id.chip_createNewItem_dl -> {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
                getString(R.string.dl)
            }
            R.id.chip_createNewItem_l -> {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
                getString(R.string.l)
            }
            R.id.chip_createNewItem_pc -> {
                binding.tvAddNewItemUnitError.visibility = View.INVISIBLE
                getString(R.string.pc)
            }
            else -> {
                binding.tvAddNewItemUnitError.visibility = View.VISIBLE
                ""
            }
        }
    }
    private fun validateQuantity(): Boolean
    {
        return if(binding.etCreateNewItemThirdItemQuantity.text.toString().isEmpty())
        {
            binding.tilCreateNewItemThirdItemQuantity.error = getString(R.string.enter_a_quantity)
            false
        }
        else
        {
            binding.tilCreateNewItemThirdItemQuantity.error = null
            true
        }
    }
    private fun validateSelectedPriority(): Prioirities
    {
        return when(binding.chgCreateNewItemPriority.checkedChipId)
        {
            R.id.chip_createNewItem_low -> {
                binding.tvAddNewItemPriorityError.visibility = View.INVISIBLE
                Prioirities.LOW
            }
            R.id.chip_createNewItem_medium -> {
                binding.tvAddNewItemPriorityError.visibility = View.INVISIBLE
                Prioirities.MEDIUM
            }
            R.id.chip_createNewItem_high -> {
                binding.tvAddNewItemPriorityError.visibility = View.INVISIBLE
                Prioirities.HIGH
            }
            else -> {
                binding.tvAddNewItemPriorityError.visibility = View.VISIBLE
                Prioirities.EMPTY
            }
        }
    }

    private fun createListItemObject(): ListItem
    {
        return ListItem (
            UUID.randomUUID().toString(),
            binding.etCreateNewItemFirstItemName.text.toString(),
            validateSelectedUnit(),
            binding.etCreateNewItemThirdItemQuantity.text.toString().toInt(),
            validateSelectedPriority()
        )
    }

    private fun populateForm(listItem: ListItem)
    {
        binding.etCreateNewItemFirstItemName.setText(listItem.item)
        when(listItem.unit)
        {
            getString(R.string.g) -> {
                binding.chgCreateNewItemUnit.check(R.id.chip_createNewItem_gram)
            }
            getString(R.string.dag) -> {
                binding.chgCreateNewItemUnit.check(R.id.chip_createNewItem_dag)
            }
            getString(R.string.kg) -> {
                binding.chgCreateNewItemUnit.check(R.id.chip_createNewItem_kg)
            }
            getString(R.string.ml) -> {
                binding.chgCreateNewItemUnit.check(R.id.chip_createNewItem_ml)
            }
            getString(R.string.dl) -> {
                binding.chgCreateNewItemUnit.check(R.id.chip_createNewItem_dl)
            }
            getString(R.string.l) -> {
                binding.chgCreateNewItemUnit.check(R.id.chip_createNewItem_l)
            }
            getString(R.string.l) -> {
                binding.chgCreateNewItemUnit.check(R.id.chip_createNewItem_pc)
            }
        }
        binding.etCreateNewItemThirdItemQuantity.setText(listItem.qunatity.toString())
        when(listItem.priority)
        {
            Prioirities.LOW -> binding.chgCreateNewItemPriority.check(R.id.chip_createNewItem_low)
            Prioirities.MEDIUM -> binding.chgCreateNewItemPriority.check(R.id.chip_createNewItem_medium)
            Prioirities.HIGH -> binding.chgCreateNewItemPriority.check(R.id.chip_createNewItem_high)
            else -> binding.chgCreateNewItemPriority.check(R.id.chip_createNewItem_low)
        }
    }
}
