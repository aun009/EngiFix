package com.example.auth.presentation.authentication

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var email by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var passwordSignIn by mutableStateOf("")

    var displayName by mutableStateOf("")

    var passwordSignUp by mutableStateOf("")

    var userName by mutableStateOf("")




    fun registerUser(
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, passwordSignUp)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "User registered: ${auth.currentUser?.uid}")
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val user = hashMapOf(
                        "id" to userId,
                        "email" to email,
                        "phoneNumber" to phoneNumber,
                        "displayName" to displayName,
                        "userName" to userName,
                    )

                    db.collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d("AuthViewModel", "User added to Firestore")
                            onResult(true, null)
                        }

                        .addOnFailureListener {
                            Log.e("AuthViewModel", "Firestore error", it)
                            onResult(false, null)
                        }

//                    onResult(true, null)
                } else {
                    Log.e("AuthViewModel", "Register failed", task.exception)
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Login existing user
    fun loginUser(
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, passwordSignIn)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Login success: ${auth.currentUser?.uid}")
                    onResult(true, null)
                } else {
                    Log.e("AuthViewModel", "Login failed", task.exception)
                    onResult(false, task.exception?.message)
                }
            }
    }
}