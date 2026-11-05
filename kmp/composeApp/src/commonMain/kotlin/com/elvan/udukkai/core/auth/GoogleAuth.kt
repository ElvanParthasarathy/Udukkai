package com.elvan.udukkai.core.auth

/** The only Google accounts that may use the app; everyone else is turned away at sign-in. */
object AllowedAccounts {
    private val emails = setOf(
        "srijaipriyasilks@gmail.com",
        "vrmshreesarathy@gmail.com",
        "elvanpartha@gmail.com",
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

/** Signs in to Firebase with an allowed email and its password. */
expect suspend fun platformEmailSignIn(email: String, password: String): GoogleSignInResult

/** Sends the password reset mail for an allowed email; true when it was sent. */
expect suspend fun platformSendPasswordReset(email: String): Boolean

/** Display name of the signed-in account (from Google profile), or null if not available. */
expect fun platformSignedInDisplayName(): String?

/** True if the signed-in account uses Google as its auth provider. */
expect fun platformIsGoogleProvider(): Boolean

/** Photo URL of the signed-in account (e.g. Google profile photo), or null. */
expect fun platformUserPhotoUrl(): String?

/** True if the user account has a password-based credential set. */
expect fun platformHasPassword(): Boolean

/** True if the user account has Google linked. */
expect fun platformIsGoogleLinked(): Boolean

/** Google email associated with the linked Google account, if any. */
expect fun platformGoogleEmail(): String?

/** Google photo URL associated with the linked Google account, if any. */
expect fun platformGooglePhotoUrl(): String?

/** Unlinks the Google provider from the current user account. Returns true on success. */
expect suspend fun platformUnlinkGoogle(): Boolean

/** Links a Google account to the current user. */
expect suspend fun platformLinkGoogle(): GoogleSignInResult

/** Changes password for an email/password account after verifying current password. */
expect suspend fun platformChangePassword(current: String, new: String): Result<Unit>

/** Creates an email/password credential for a user signed in via Google. */
expect suspend fun platformCreatePassword(password: String): Result<Unit>

/** Permanently deletes the user account from Firebase Auth. */
expect suspend fun platformDeleteAccount(password: String? = null): Result<Unit>
