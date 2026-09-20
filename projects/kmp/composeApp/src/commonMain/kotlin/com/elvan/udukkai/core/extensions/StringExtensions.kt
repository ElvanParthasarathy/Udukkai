package com.elvan.udukkai.core.extensions

fun String.capitalizeFirst(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun String?.orEmpty(): String = this ?: ""
