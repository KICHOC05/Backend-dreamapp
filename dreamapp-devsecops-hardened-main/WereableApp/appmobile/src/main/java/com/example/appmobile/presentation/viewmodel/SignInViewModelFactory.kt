package com.example.appmobile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appmobile.domain.usecase.GoogleAuthUiClient

class SignInViewModelFactory(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SignInViewModel::class.java) -> {
                SignInViewModel(googleAuthUiClient, context) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
