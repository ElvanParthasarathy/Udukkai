package com.elvan.udukkai.core.update

private val FLAVORS = listOf("textiles", "factory")

/**
 * Which file of a release is this app's APK, as an index into [names]. A release carries one APK per app
 * (Udukkai-Textiles.apk, Udukkai-Factory.apk) and the other app's APK is a different package: Android would scan it and
 * then refuse it, over and over on every Update tap. So only a name that carries this app's own [flavor] counts; a
 * single APK that names no app at all (Udukkai.apk) is taken as the only possible one.
 */
fun pickApkIndex(names: List<String>, flavor: String): Int? {
    val apks = names.withIndex().filter { it.value.endsWith(".apk", ignoreCase = true) }
    val own = flavor.lowercase()
    if (own.isNotEmpty()) apks.firstOrNull { own in it.value.lowercase() }?.let { return it.index }
    val one = apks.singleOrNull() ?: return null
    return one.index.takeIf { FLAVORS.none { f -> f in one.value.lowercase() } }
}
