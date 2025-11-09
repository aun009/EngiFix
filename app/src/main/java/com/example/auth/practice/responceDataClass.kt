package com.example.auth.practice

import kotlin.collections.emptyList
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.awaitResponse


data class responceDataClass(
    val address: Address,
    val company: Company,
    val email: String,
    val id: Int,
    val name: String,
    val phone: String,
    val username: String,
    val website: String
)


class UserViewModel : ViewModel() {

    var userList = mutableStateOf<List<responceDataClass>>(emptyList())
        private set

    var errorMessage = mutableStateOf("")

    fun fetchUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var response = RetrofitInstance.apiInterface.getData().awaitResponse()
                if (response.isSuccessful) {
                    userList.value = response.body()?: emptyList();
                } else {
                    errorMessage.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception: ${e.message}", e)
                errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }
}
