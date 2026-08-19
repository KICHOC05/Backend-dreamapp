package com.example.appmobile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appmobile.data.database.dao.UserDataDao
import com.example.appmobile.data.remote.api.UserApi
import com.example.appmobile.presentation.communication.WearMessageSender

class UserViewModelFactory(
    private val userDataDao: UserDataDao,
    private val context: Context,
    private val userApi: UserApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            val sender = WearMessageSender(context)
            return UserViewModel(userDataDao, sender, userApi) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
