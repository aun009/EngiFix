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

    var loginSuccess by mutableStateOf(false)


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
        val cleanEmail = email.trim()
        val cleanUsername = userName.trim()
        val normalizedUsername = cleanUsername.normalizedUsernameKey()

        if (!cleanUsername.isValidUsername()) {
            onResult(false, "Use 3-20 letters, numbers, underscores or dots for username.")
            return
        }

        fun createFirebaseUser() {
            auth.createUserWithEmailAndPassword(cleanEmail, passwordSignUp)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AuthViewModel", "User registered: ${auth.currentUser?.uid}")
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userRef = db.collection("users").document(userId)
                        val usernameRef = db.collection("usernames").document(normalizedUsername)
                        val now = System.currentTimeMillis()

                        db.runTransaction { transaction ->
                            if (transaction.get(usernameRef).exists()) {
                                throw IllegalStateException("Username already taken.")
                            }

                            val user = hashMapOf(
                                "id" to userId,
                                "email" to cleanEmail,
                                "phoneNumber" to phoneNumber.trim(),
                                "displayName" to displayName.trim(),
                                "userName" to cleanUsername,
                                "userNameNormalized" to normalizedUsername,
                                "usernameUpdatedAt" to 0L,
                                "headline" to "",
                                "bio" to "",
                                "photoUrl" to "",
                                "collegeName" to "",
                                "collegeNameNormalized" to "",
                                "collegeKey" to "",
                                "collegeCity" to "",
                                "collegeState" to "",
                                "branch" to "",
                                "graduationYear" to "",
                                "goalRole" to "",
                                "skills" to emptyList<String>(),
                                "projects" to emptyList<Map<String, Any>>(),
                                "codingPlatforms" to emptyList<Map<String, String>>(),
                                "resumeText" to "",
                                "resumeUpdatedAt" to 0L,
                                "resumeChatMessages" to emptyList<Map<String, Any>>(),
                                "profileScore" to 0,
                                "profileCompletion" to 25,
                                "createdAt" to now
                            )

                            transaction.set(usernameRef, mapOf("uid" to userId, "userName" to cleanUsername, "createdAt" to now))
                            transaction.set(userRef, user)
                        }
                            .addOnSuccessListener {
                                Log.d("AuthViewModel", "User added to Firestore")
                                // Only report registration success; do NOT mark as logged in.
                                onResult(true, null)
                            }
                            .addOnFailureListener {
                                Log.e("AuthViewModel", "Firestore error", it)
                                auth.currentUser?.delete()
                                onResult(false, it.message ?: "Could not reserve username.")
                            }
                    } else {
                        Log.e("AuthViewModel", "Register failed", task.exception)
                        onResult(false, task.exception?.message)
                    }
                }
        }

        db.collection("users")
            .whereEqualTo("userNameNormalized", normalizedUsername)
            .limit(1L)
            .get()
            .addOnSuccessListener { normalizedSnapshot ->
                if (!normalizedSnapshot.isEmpty) {
                    onResult(false, "Username already taken.")
                    return@addOnSuccessListener
                }
                db.collection("users")
                    .whereEqualTo("userName", cleanUsername)
                    .limit(1L)
                    .get()
                    .addOnSuccessListener { legacySnapshot ->
                        if (!legacySnapshot.isEmpty) {
                            onResult(false, "Username already taken.")
                        } else {
                            createFirebaseUser()
                        }
                    }
                    .addOnFailureListener { error ->
                        onResult(false, error.message ?: "Could not check username.")
                    }
            }
            .addOnFailureListener { error ->
                onResult(false, error.message ?: "Could not check username.")
            }
    }

    /**
     * Send password reset email to the given address.
     */
    fun sendPasswordResetEmail(
        emailForReset: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (emailForReset.isBlank()) {
            onResult(false, "Please enter your email")
            return
        }

        auth.sendPasswordResetEmail(emailForReset)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Password reset email sent to $emailForReset")
                    onResult(true, null)
                } else {
                    Log.e("AuthViewModel", "Password reset failed", task.exception)
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
                    loginSuccess = true
                    onResult(true, null)
                } else {
                    Log.e("AuthViewModel", "Login failed", task.exception)
                    onResult(false, task.exception?.message)
                }
            }
    }
}

private fun String.normalizedUsernameKey(): String =
    trim().lowercase().replace(Regex("[^a-z0-9_.]"), "")

private fun String.isValidUsername(): Boolean =
    matches(Regex("^[A-Za-z0-9_.]{3,20}$"))
