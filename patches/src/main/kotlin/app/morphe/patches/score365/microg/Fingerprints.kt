package app.morphe.patches.score365.microg

import app.morphe.patcher.Fingerprint

internal object GooglePlayServicesUtilFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/common/GooglePlayServicesUtilLight;",
    name = "isGooglePlayServicesAvailable",
    returnType = "I",
)

internal object GooglePlayServicesUtilLightCheckFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/common/GooglePlayServicesUtilLight;",
    returnType = "I",
    strings = listOf(
        "MetadataValueReader",
        "This should never happen.",
    ),
)

internal object GoogleApiAvailabilityLightCheckFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/common/GoogleApiAvailabilityLight;",
    returnType = "I",
    parameters = listOf("I", "Landroid/content/Context;"),
)

internal object GoogleApiAvailabilityLightContextOnlyFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/common/GoogleApiAvailabilityLight;",
    returnType = "I",
    parameters = listOf("Landroid/content/Context;"),
)

internal object ServiceCheckFingerprint : Fingerprint(
    strings = listOf("Google Play Services not available"),
)
