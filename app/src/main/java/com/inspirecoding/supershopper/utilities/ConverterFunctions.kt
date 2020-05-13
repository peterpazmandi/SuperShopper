package com.inspirecoding.supershopper.utilities

import java.text.SimpleDateFormat
import java.util.*

object ConverterFunctions
{
    fun convertToDate(year: Int, month: Int, dayOfMonth: Int): Date
    {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)

        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val formattedDate: String = sdf.format(calendar.time)
        return sdf.parse(formattedDate) as Date
    }
    fun convertToDate(timeStamp: Long) = Date(timeStamp)
}