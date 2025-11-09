package com.example.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.practice.UserScreen
import com.example.auth.presentation.authentication.AskFirstName
import com.example.auth.presentation.authentication.AuthViewModel
import com.example.auth.presentation.authentication.FirstScreen
import com.example.auth.presentation.authentication.LoginScreen
import com.example.auth.presentation.authentication.RegisterScreen
import com.example.auth.presentation.authentication.UserNameAndPassScreen
import com.example.auth.presentation.features.contest.ContestScreen
import com.example.auth.presentation.features.contest.ContestNotificationService
import com.example.auth.presentation.inApp.homescreen.HomeScreen
import com.example.auth.presentation.inApp.profilescreen.ProfileScreen
import com.example.auth.ui.theme.AuthTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start the contest notification service
        startContestNotificationService()
        setContent {
            AuthTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel() // 👈 shared ViewModel



                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "first_screen"
                    ) {
                        composable("first_screen") {
                            FirstScreen(navController)
                        }
                        composable("register_screen") {
                            RegisterScreen(navController, authViewModel) // 👈 pass viewModel
                        }
                        composable("login_screen") {
                            LoginScreen(navController, authViewModel)
                        }

                        composable("ask_name_screen") {
                            AskFirstName(navController, authViewModel)
                        }

                        composable("username_pass_screen") {
                            UserNameAndPassScreen(navController, authViewModel)
                        }

                        composable("home") {
                            HomeScreen(navController)
                        }

                        composable("profile") {
                            ProfileScreen(authViewModel)
                        }

                        composable("ui") {
                            ContestScreen(
                                onBackClick = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    private fun startContestNotificationService() {
        val serviceIntent = Intent(this, ContestNotificationService::class.java)
        startService(serviceIntent)
    }
}


//class None() {
//    private fun getData() {
//
//        val progressDialog = ProgressDialog(context)
//        progressDialog.setMessage("Please wait")
//        progressDialog.show()
//
//        RetrofitInstance.apiInterface.getData().enqueue(object : retrofit2.Callback<responceDataClass?> {
//            override fun onResponse(
//                call: Call<responceDataClass?>,
//                response: Response<responceDataClass?>
//            ) {
//
//                progressDialog.dismiss()
//            }
//
//            override fun onFailure(
//                call: Call<responceDataClass?>,
//                t: Throwable
//            ) {
//                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
//                progressDialog.dismiss()
//            }
//
//        })
//    }
//}
