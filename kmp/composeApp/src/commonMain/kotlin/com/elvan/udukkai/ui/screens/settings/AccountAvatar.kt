package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.auth.platformCachedPhotoBytes
import com.elvan.udukkai.core.auth.platformProfilePhotoBytes
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveDotsLoader
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/** The account picture once per app run and account, so each screen shows it at once and none starts a download of its own. */
private object AccountPhotoHolder {
    var email: String? = null
    var version = 0
    var photo by mutableStateOf<ImageBitmap?>(null)
    var checked by mutableStateOf(false)
}

/** Fills its circle with the photo of the signed-in Google account; [fallback] shows until there is one. */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun AccountAvatar(fallback: @Composable () -> Unit) {
    val email = AuthManager.signedInEmail
    val version = AuthManager.photoVersion
    val same = AccountPhotoHolder.email == email && AccountPhotoHolder.version == version
    val current = AccountPhotoHolder.photo.takeIf { same }
    val loading = email != null && !(same && AccountPhotoHolder.checked)

    LaunchedEffect(email, version) {
        if (!same) {
            AccountPhotoHolder.email = email
            AccountPhotoHolder.version = version
            AccountPhotoHolder.photo = null
            AccountPhotoHolder.checked = false
        }
        if (email == null || AccountPhotoHolder.checked) return@LaunchedEffect
        // The saved copy first, so the picture is there at once; then a download only if it is old or missing
        platformCachedPhotoBytes()?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()?.let { AccountPhotoHolder.photo = it }
        }
        if (AccountPhotoHolder.photo != null) AccountPhotoHolder.checked = true
        platformProfilePhotoBytes()?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()?.let { AccountPhotoHolder.photo = it }
        }
        AccountPhotoHolder.checked = true
    }

    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(CircleShape)
        )
    } else if (loading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            ExpressiveDotsLoader(
                modifier = Modifier.fillMaxSize().padding(13.dp),
                color = rememberShellColors().textSecondary
            )
        }
    } else {
        fallback()
    }
}
