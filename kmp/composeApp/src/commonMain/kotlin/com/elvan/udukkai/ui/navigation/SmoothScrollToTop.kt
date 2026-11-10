package com.elvan.udukkai.ui.navigation

import androidx.compose.foundation.lazy.LazyListState

/** Uses the lazy list's measured-item animation and native scroll cancellation. */
suspend fun LazyListState.smoothScrollToTop() {
    if (canScrollBackward) animateScrollToItem(0, 0)
}
