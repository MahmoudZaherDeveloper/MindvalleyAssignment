package com.mindvalley.assignment.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import app.mindvalley.assignment.utli.BASE_URL
import app.mindvalley.assignment.utli.REFRESH_ERROR
import app.mindvalley.assignment.utli.REFRESH_SUCCESS
import app.mindvalley.assignment.utli.SWIPE_DOWN
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.mindvalley.assignment.data.model.ItemPin
import com.mindvalley.urlloader.DownloadableString

class MainViewModel : ViewModel() {
    val isRefreshing = MutableLiveData<Boolean>()
    val pinList = MutableLiveData<MutableList<ItemPin>>()
    val snackBarText = SingleLiveEvent<String>()
    private val gson = Gson()

    init {
        pinList.value = mutableListOf()
        snackBarText.value = SWIPE_DOWN
        isRefreshing.value = false
    }

    fun doRefresh() {
        isRefreshing.value = true
        //Load the main JSON using the library
        DownloadableString(BASE_URL).load { result, throwable ->
            if (throwable != null) {
                isRefreshing.value = false
                snackBarText.value = REFRESH_ERROR
                return@load
            }
            //add some image_holder handling, other stuff may go wrong
            //but image_holder handling is not the purpose of this demonstration
            try {
                val items = mutableListOf<ItemPin>()
                val array = gson.fromJson(result, JsonArray::class.java)
                array.mapTo(items) {
                    val obj = it.asJsonObject
                    ItemPin.fromJsonObject(obj)
                }
                pinList.value = items
                snackBarText.value = REFRESH_SUCCESS
            } catch (e: Exception) {
                snackBarText.value = REFRESH_ERROR
            }
            isRefreshing.value = false
        }


    }

}