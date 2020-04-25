package com.inspirecoding.supershopper.utilities

import android.util.Log
import org.joda.time.LocalDate
import java.text.SimpleDateFormat

private val TAG = "CurrentDateFunctions"
class CurrentDateFunctions
{
    companion object
    {
        fun getToday(): LocalDate
        {
            val today = org.joda.time.LocalDate()
            return today
        }
        fun convertMillisToDate(millis: Long): String
        {
            val date: String

            val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm")
            date = formatter.format(millis)

            return date
        }
        fun convertMillisToDateTime(millis: Long): String
        {
            val date: String

            val formatter = SimpleDateFormat("hh:mm:ss")
            date = formatter.format(millis)
            Log.i(TAG, date)
            return date
        }
    }
}