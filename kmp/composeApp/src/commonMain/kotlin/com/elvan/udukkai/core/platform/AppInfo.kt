package com.elvan.udukkai.core.platform

/** The version of this build ("1.0.8"), as the About pages show it. */
expect fun appVersionName(): String

/** False in the Google Play build, whose updates come from Google Play: no updater row or button is shown there. */
expect fun hasInAppUpdater(): Boolean

/** The build number (version code), e.g. 10. */
expect fun appBuildNumber(): Int

/** The package name of this app, e.g. com.elvan.udukkai.textiles. */
expect fun appPackageName(): String

/** Where this build comes from: "GitHub" or "Google Play". */
expect fun appSourceName(): String
