package app.morphe.patches.score365.microg

internal object MicroGConstants {
    const val GMS_CORE_VENDOR_GROUP_ID = "app.revanced"
    const val GMS_CORE_PACKAGE_NAME = "$GMS_CORE_VENDOR_GROUP_ID.android.gms"

    val ACTIONS = setOf(
        "com.google.android.c2dm.intent.RECEIVE",
        "com.google.android.c2dm.intent.REGISTER",
        "com.google.android.c2dm.intent.REGISTRATION",
        "com.google.android.gms.auth.NO_IMPL",
        "com.google.android.gms.signin.service.START",
        "com.google.android.gms.common.telemetry.service.START",
        "com.google.android.gms.auth.service.START",
        "com.google.android.gms.auth.api.signin.service.START",
        "com.google.android.gms.auth.account.service.START",
        "com.google.android.gms.auth.api.credentials.service.START",
        "com.google.android.gms.auth.api.phone.service.START",
        "com.google.android.gms.phenotype.service.START",
        "com.google.android.gms.clearcut.service.START",
        "com.google.android.gms.ads.identifier.service.START",
        "com.google.android.gms.measurement.START",
        "com.google.android.gms.location.service.START",
        "com.google.android.gms.location.places.PlaceDetectionService.START",
        "com.google.android.gms.location.places.GeoDataApi.START",
        "com.google.firebase.auth.api.gms.service.START",
        "com.google.firebase.dynamiclinks.service.START",
        "com.google.iid.TOKEN_REQUEST",
    )

    val AUTHORITIES = setOf(
        "com.google.android.gms.auth.accounts",
        "com.google.android.gms.chimera",
        "com.google.android.gms.fonts",
        "com.google.android.gms.phenotype",
        "com.google.android.gsf.gservices",
        "com.google.settings",
    )
}
