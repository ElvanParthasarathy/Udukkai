package com.elvan.udukkai.core.auth

// Development stub: the desktop build is only used while working on the app.
private var devEmail: String? = null

actual suspend fun platformGoogleSignIn(): GoogleSignInResult {
    devEmail = "jaiprakashvp2006@gmail.com"
    return GoogleSignInResult.Success(devEmail!!)
}

actual fun platformSignedInEmail(): String? = devEmail

actual fun platformSignOut() {
    devEmail = null
}

actual suspend fun platformEmailSignIn(email: String, password: String): GoogleSignInResult = platformGoogleSignIn()

actual suspend fun platformSendPasswordReset(email: String): Boolean = true
