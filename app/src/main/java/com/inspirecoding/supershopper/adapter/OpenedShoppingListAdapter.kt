package com.inspirecoding.supershopper.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.inspirecoding.supershopper.fragments.DetailsFragment
import com.inspirecoding.supershopper.fragments.OpenBoughtFragment

class OpenedShoppingListAdapter(activity: AppCompatActivity, private var itemCount: Int): FragmentStateAdapter(activity)
{
    private var fragmentList: MutableList<Fragment> = ArrayList()

    fun addFragments(fragment: Fragment) = fragmentList.add(fragment)
    override fun getItemCount() = itemCount

    override fun createFragment(position: Int): Fragment
    {
        return when(position)
        {
            0 -> OpenBoughtFragment()
            1 -> DetailsFragment()
            else -> OpenBoughtFragment()
        }
    }
}