package com.example.munchtime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.munchtime.auth.GoogleAuthClient
import com.example.munchtime.screens.ProfileScreen
import com.example.munchtime.screens.SignInScreen
import com.example.munchtime.screens.onBoardingScreen
import com.example.munchtime.ui.theme.MunchTimeTheme
import com.example.munchtime.viewmodels.SignInViewModel
import com.example.munchtime.viewmodels.onBoardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val onboardingViewModel: onBoardingViewModel by viewModels()

    private val googleAuthUIClient by lazy {
        GoogleAuthClient(
            context = applicationContext,
            oneTapCient = com.google.android.gms.auth.api.identity.Identity.getSignInClient(
                applicationContext
            )
        )
    }

    private var currentRoute by mutableStateOf("sign_in")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        installSplashScreen()


        setContent {
            MunchTimeTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "onboarding") {
                    composable(route = "sign_in") {
                        SignInContent(navController)
                    }
                    composable(route = "onboarding") {
                        OnboardingContent(navController)
                    }
                    composable(route = "profile_screen") {
                        ProfileContent(navController)
                    }
                }
            }
        }

    }

    @Composable
    private fun SignInContent(navController: NavController) {
        val viewModel = viewModel<SignInViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = Unit) {
            if (googleAuthUIClient.getSignedInUser() != null) {
                navController.navigate("profile_screen")
            }
        }

        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    lifecycleScope.launch {
                        val signinResult = googleAuthUIClient.getSignInResult(
                            intent = result.data ?: return@launch
                        )
                        viewModel.onSignInResult(signinResult)
                    }
                }
            }

        LaunchedEffect(key1 = state.isSignInSuccesful) {
            if (state.isSignInSuccesful) {
                Toast.makeText(applicationContext, "success", Toast.LENGTH_LONG).show()
                navController.navigate("profile_screen")
                viewModel.resetState()
            }
        }

        SignInScreen(state = state, onSignInClick = {
            lifecycleScope.launch {
                val signInIntentSender = googleAuthUIClient.signIn()
                launcher.launch(
                    IntentSenderRequest.Builder(
                        signInIntentSender ?: return@launch
                    ).build()
                )
            }
        })
    }

    @Composable
    private fun OnboardingContent(navController: NavController) {
        val shouldLaunchOnboarding = onboardingViewModel.shouldLaunchOnboarding()
        if (!shouldLaunchOnboarding) {
            LaunchedEffect(Unit) {
                navController.navigate("sign_in")
            }
        }
        onBoardingScreen(onStartClick = {
            lifecycleScope.launch {
                navController.navigate("sign_in")
            }
        })
    }

    @Composable
    private fun ProfileContent(navController: NavController) {
        ProfileScreen(
            userData = googleAuthUIClient.getSignedInUser(),
            onSignOut = {
                lifecycleScope.launch {
                    googleAuthUIClient.signOut()
                    Toast.makeText(
                        applicationContext,
                        "Signed Out",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.popBackStack()
                }
            })
    }


    override fun onBackPressed() {
        if (currentRoute == "sign_in") {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
