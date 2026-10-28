package com.elvan.udukkai.core.auth

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.elvan.udukkai.core.platform.AppContext
import com.elvan.udukkai.core.platform.CurrentActivity
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        val error = task.exception
        if (error != null) cont.resumeWithException(error) else cont.resume(task.result)
    }
}

/** The web client id the Google services plugin generates from google-services.json. */
private fun webClientId(): String? {
    val context = AppContext.context
    val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    return if (id == 0) null else context.getString(id)
}

actual suspend fun platformGoogleSignIn(): GoogleSignInResult {
    val activity = CurrentActivity.activity ?: return GoogleSignInResult.Failed
    val clientId = webClientId() ?: return GoogleSignInResult.Failed
    return try {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return GoogleSignInResult.Failed
        }
        val google = GoogleIdTokenCredential.createFrom(credential.data)
        // Checked before Firebase hears about the account, so no user is ever created for a stranger
        if (!AllowedAccounts.isAllowed(google.id)) return GoogleSignInResult.NotAllowed(google.id)

        val auth = FirebaseAuth.getInstance()
        val user = auth.signInWithCredential(GoogleAuthProvider.getCredential(google.idToken, null)).await().user
        val email = user?.email
        if (!AllowedAccounts.isAllowed(email)) {
            auth.signOut()
            return GoogleSignInResult.NotAllowed(email ?: google.id)
        }
        GoogleSignInResult.Success(email!!)
    } catch (_: GetCredentialCancellationException) {
        GoogleSignInResult.Cancelled
    } catch (_: Exception) {
        GoogleSignInResult.Failed
    }
}

actual fun platformSignedInEmail(): String? = try {
    FirebaseAuth.getInstance().currentUser?.email
} catch (_: Exception) {
    null
}

actual fun platformSignOut() {
    try {
        FirebaseAuth.getInstance().signOut()
    } catch (_: Exception) {}
    try {
        java.io.File(AppContext.context.cacheDir, "account_photo.jpg").delete()
    } catch (_: Exception) {}
    val activity = CurrentActivity.activity ?: return
    // Forget the chosen Google account so the next sign-in shows the chooser again
    CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
        try {
            CredentialManager.create(activity).clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (_: Exception) {}
    }
}

actual suspend fun platformEmailSignIn(email: String, password: String): GoogleSignInResult {
    val clean = email.trim().lowercase()
    if (!AllowedAccounts.isAllowed(clean)) return GoogleSignInResult.NotAllowed(clean)
    return try {
        val user = FirebaseAuth.getInstance().signInWithEmailAndPassword(clean, password).await().user
        val signedIn = user?.email
        if (!AllowedAccounts.isAllowed(signedIn)) {
            FirebaseAuth.getInstance().signOut()
            GoogleSignInResult.NotAllowed(signedIn ?: clean)
        } else {
            GoogleSignInResult.Success(signedIn!!)
        }
    } catch (_: Exception) {
        GoogleSignInResult.Failed
    }
}

actual suspend fun platformSendPasswordReset(email: String): Boolean {
    val clean = email.trim().lowercase()
    if (!AllowedAccounts.isAllowed(clean)) return false
    return try {
        FirebaseAuth.getInstance().sendPasswordResetEmail(clean).await()
        true
    } catch (_: Exception) {
        false
    }
}
