package com.varvet.barcodereadersample.utils

import com.google.gson.Gson

fun ArrayList<String>.toJson(): String? {
    return Gson().toJson(this)
}