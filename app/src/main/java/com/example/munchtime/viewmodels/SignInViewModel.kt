package com.example.munchtime.viewmodels

import androidx.lifecycle.ViewModel
import com.example.munchtime.auth.SignInResult
import com.example.munchtime.models.signInState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor() : ViewModel() {

    private val signInState = signInState()
    private val _state = MutableStateFlow(signInState)
    val state = _state.asStateFlow()


    fun onSignInResult(result : SignInResult){
        _state.update { it.copy(
            isSignInSuccesful = result.data != null,
            signInError = result.errorMessage
        ) }
    }


    fun resetState(){
        _state.update { signInState() }
    }
}