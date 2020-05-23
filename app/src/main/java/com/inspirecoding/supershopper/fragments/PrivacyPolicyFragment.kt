package com.inspirecoding.supershopper.fragments

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment()
{
    private lateinit var binding: FragmentPrivacyPolicyBinding

    override fun onStart()
    {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onCreateView(layoutInflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_privacy_policy, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.webViewPrivacyPolicy1.text = Html.fromHtml(getString(R.string.privacy_policy_text_1))
        binding.webViewPrivacyPolicy2.text = Html.fromHtml(getString(R.string.privacy_policy_text_2))
    }
}
