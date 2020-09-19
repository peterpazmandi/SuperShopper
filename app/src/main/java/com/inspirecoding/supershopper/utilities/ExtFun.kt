package com.inspirecoding.supershopper.utilities

import android.util.Log

fun Float.toStringWithDecimal(): String
{
    return if (this.rem(1).toString() == "0.0")
    {
        this.toString().substringBefore(".")
    }
    else
    {
        this.toString()
    }
}