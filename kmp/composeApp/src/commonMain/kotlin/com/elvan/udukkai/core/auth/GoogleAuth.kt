package com.elvan.udukkai.core.auth

/** The only Google accounts that may use the app; everyone else is turned away at sign-in. */
object AllowedAccounts {
    private val emails = setOf(
        "srijaipriyasilks@gmail.com",
        "vrmshreesarathy@gmail.com",
        "elvanparthasarathy@gmail.com",
        "jaiprakashvp2006@gmail.com"
    )

    fun isAllowed(email: String?): Boolean =
        email != null && emails.contains(email.trim().lowercase())
}

sealed class GoogleSignInResult {
    data class Success(val email: String) : GoogleSignInResult()
    data class NotAllowed(val email: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
    data object Failed : GoogleSignInResult()
}

/** Shows the Google account chooser and signs in to Firebase with the chosen account. */
expect suspend fun platformGoogleSignIn(): GoogleSignInResult

/** Email of the account that is signed in right now, or null. */
expect fun platformSignedInEmail(): String?

expect fun platformSignOut()
