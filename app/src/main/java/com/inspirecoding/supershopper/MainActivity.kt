package com.inspirecoding.supershopper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.inspirecoding.supershopper.databinding.ActivityMainBinding
import android.util.Base64
import android.util.Log
import android.content.pm.PackageManager
import android.widget.Toast
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        binding.lifecycleOwner = this

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainFragment
            ), binding.drawerLayout
        )

        navController = Navigation.findNavController(this, R.id.fragmentContainer)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        NavigationUI.setupWithNavController(
            toolbar,
            navController,
            appBarConfiguration
        )

        printKeyHash()
    }
    private fun printKeyHash()
    {
        try
        {
            val info = packageManager.getPackageInfo("com.pazpeti.runningapp", PackageManager.GET_SIGNATURES)
            for(signature in info.signatures)
            {
                val messageDigest = MessageDigest.getInstance("SHA")
                messageDigest.update(signature.toByteArray())
                Log.i("printKeyHash", Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT))
            }
        }
        catch (exception: PackageManager.NameNotFoundException)
        {
            Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
        }
        catch (exception: NoSuchAlgorithmException)
        {
            Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }
}
