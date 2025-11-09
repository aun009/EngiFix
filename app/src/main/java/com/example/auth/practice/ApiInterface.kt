package com.example.auth.practice

import retrofit2.Call
import retrofit2.http.GET

// to hit which end point
interface ApiInterface {
    @GET("users")
    fun getData() : Call<List<responceDataClass>>
}