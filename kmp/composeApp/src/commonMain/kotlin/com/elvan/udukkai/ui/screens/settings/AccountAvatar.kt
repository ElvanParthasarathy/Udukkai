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

/** Fills its circle with the photo of the signed-in Google account; [fallback] shows until there is one. */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun AccountAvatar(fallback: @Composable () -> Unit) {
    val email = AuthManager.signedInEmail
    var photo by remember(email) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(email) { mutableStateOf(email != null) }

    LaunchedEffect(email) {
        if (email == null) return@LaunchedEffect
        // The saved copy first, so the picture is there at once; then a fresh download
        platformCachedPhotoBytes()?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()?.let { photo = it }
        }
        // With a saved picture there is nothing to wait for
        if (photo != null) loading = false
        platformProfilePhotoBytes()?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()?.let { photo = it }
        }
        loading = false
    }

    val current = photo
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
                modifier = Modifier.fillMaxSize().padding(19.dp),
                color = rememberShellColors().accent
            )
        }
    } else {
        fallback()
    }
}
