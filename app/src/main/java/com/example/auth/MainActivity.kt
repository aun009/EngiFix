package com.example.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.data.Mentor
import com.example.auth.practice.UserScreen
import com.example.auth.presentation.authentication.AskFirstName
import com.example.auth.presentation.authentication.AuthViewModel
import com.example.auth.presentation.authentication.FirstScreen
import com.example.auth.presentation.authentication.LoginScreen
import com.example.auth.presentation.authentication.RegisterScreen
import com.example.auth.presentation.authentication.UserNameAndPassScreen
import com.example.auth.presentation.components.MentorCardEx
import com.example.auth.presentation.features.contest.ContestScreen
import com.example.auth.presentation.features.contest.ContestNotificationService
import com.example.auth.presentation.features.mentorship.MentorDetailScreen
import com.example.auth.presentation.features.mentorship.MentorshipScreen
import com.example.auth.presentation.inApp.homescreen.HomeScreen
import com.example.auth.presentation.inApp.profilescreen.ProfileScreen
import com.example.auth.ui.theme.AuthTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // Permission launcher for notification permission (Android 13+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "✅ Notification permission granted")
        } else {
            android.util.Log.w("MainActivity", "⚠️ Notification permission denied")
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission for Android 13+ (API 33+)
        requestNotificationPermission()
        
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

                        composable("mentor") {
                            MentorshipScreen (
                                onBackClick = {
                                    navController.navigateUp()
                                },

                                onMentorClick = {mentor ->
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "selected_mentor",
                                        mentor
                                    )

                                    navController.navigate("mentor_detail")
                                }
                            )
                        }

                        composable("mentor_detail") {
                            // Retrieve the mentor object
                            val mentor = navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.get<Mentor>("selected_mentor")  // 👈 Same key used above

                            // Show detail screen if mentor exists
                            mentor?.let { selectedMentor ->
                                MentorDetailScreen(
                                    mentor = selectedMentor,
                                    onBackClick = { navController.navigateUp() },
                                    onGetAccessClick = {
                                        // Handle successful payment - navigate to success screen or show confirmation
                                        // You can add navigation to a success screen here
                                        navController.navigateUp()
                                    },
                                    razorpayKeyId = "YOUR_RAZORPAY_KEY_ID" // TODO: Replace with your actual Razorpay Key ID
                                )
                            }
                        }



                    }
                }
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if permission is already granted
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    android.util.Log.d("MainActivity", "✅ Notification permission already granted")
                }
                else -> {
                    // Request the permission
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android 12 and below, notification permission is automatically granted
            android.util.Log.d("MainActivity", "Android version < 13, notification permission not needed")
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
