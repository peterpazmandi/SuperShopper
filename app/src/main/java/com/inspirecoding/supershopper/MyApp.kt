package com.inspirecoding.supershopper

import android.app.Application
import android.content.Context
import com.inspirecoding.supershopper.modul.firebaseViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp: Application()
{
    init
    {
        instance = this
    }

    override fun onCreate()
    {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(firebaseViewModelModule))
        }
    }

    companion object
    {
        private var instance: MyApp? = null

        fun applicationContext() : Context
        {
            return instance!!.applicationContext
        }
    }
}