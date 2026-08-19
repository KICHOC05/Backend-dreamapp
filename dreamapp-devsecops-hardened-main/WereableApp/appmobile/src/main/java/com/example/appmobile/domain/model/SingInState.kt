package com.example.appmobile.domain.model

import android.content.IntentSender

data class SignInState(
    val uid: String? = null,
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isLoading: Boolean = false,
    val signInIntentSender: IntentSender? = null,
    val isNewUser: Boolean = false,  // Flag si el usuario no existe en la BD remota
    val userDataModel: UserDataModel? = null,
    val signInSuccessMessage: String? = null  // Mensaje de éxito del inicio de sesión
)