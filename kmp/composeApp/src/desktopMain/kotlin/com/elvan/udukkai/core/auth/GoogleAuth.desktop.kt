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

actual fun platformSignedInDisplayName(): String? = devEmail?.substringBefore("@")?.replace(".", " ")

actual fun platformIsGoogleProvider(): Boolean = true

actual fun platformUserPhotoUrl(): String? = null

actual fun platformHasPassword(): Boolean = true

actual fun platformIsGoogleLinked(): Boolean = true

actual fun platformGoogleEmail(): String? = devEmail

actual fun platformGooglePhotoUrl(): String? = null

actual suspend fun platformUnlinkGoogle(): Boolean = true

actual suspend fun platformLinkGoogle(): GoogleSignInResult = GoogleSignInResult.Success(devEmail ?: "dev@udukkai.local")

actual suspend fun platformVerifyPassword(password: String): Result<Unit> = Result.success(Unit)

actual suspend fun platformChangePassword(current: String, new: String): Result<Unit> = Result.success(Unit)

actual suspend fun platformCreatePassword(password: String): Result<Unit> = Result.success(Unit)

actual suspend fun platformDeleteAccount(password: String?): Result<Unit> {
    devEmail = null
    return Result.success(Unit)
}

actual suspend fun platformRefreshAccount() {}

actual suspend fun platformUpdateDisplayName(name: String): Result<Unit> = Result.success(Unit)
