package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.configureAppCheck
import com.example.data.firebase.onGoogleSignInClicked
import com.example.data.firebase.signOutFromApp
import com.example.ui.screens.DiktorMainScreen
import com.example.ui.theme.DiktorProTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureAppCheck(this, intent)

        setContent {
            DiktorProTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val credentialManager = remember(context) { CredentialManager.create(context) }

                val currentUser by remember { Firebase.auth.authStateFlow() }
                    .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

                var isSigningIn by remember { mutableStateOf(false) }
                var authError by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    if (Firebase.auth.currentUser == null) {
                        attemptAutoSignIn(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isSigningIn = false
                                authError = null
                            },
                            onUnauthenticated = {
                                isSigningIn = false
                            },
                            scope = scope
                        )
                    }
                }

                val activeUser = currentUser
                key(activeUser?.uid ?: "local_guest") {
                    DiktorMainScreen(
                        currentUserId = activeUser?.uid,
                        userDisplayName = activeUser?.displayName ?: activeUser?.email,
                        isSigningIn = isSigningIn,
                        authError = authError,
                        onGoogleSignInClick = {
                            isSigningIn = true
                            authError = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isSigningIn = false
                                    authError = null
                                },
                                onAuthError = { msg ->
                                    isSigningIn = false
                                    authError = msg
                                },
                                onAuthCancelled = {
                                    isSigningIn = false
                                },
                                scope = scope
                            )
                        },
                        onSignOutClick = {
                            signOutFromApp(
                                credentialManager = credentialManager,
                                scope = scope
                            )
                        }
                    )
                }
            }
        }
    }
}
