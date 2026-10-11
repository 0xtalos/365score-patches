package app.morphe.patches.score365.microg

import app.morphe.patcher.apk.ApkSignatureScheme
import java.security.MessageDigest
import java.security.cert.X509Certificate

class NoCertificateException : Exception("Unable to extract certificate from APK")

/**
 * Extracts the leaf / developer end-entity signing certificate from the APK's signature blocks.
 */
fun getEndEntityCertificate(
    schemeToCertsMap: Map<ApkSignatureScheme, List<X509Certificate>>,
): X509Certificate {
    val filteredMap = schemeToCertsMap.filterValues { it.isNotEmpty() }
    val highestScheme = filteredMap.keys.maxByOrNull { it.id } ?: throw NoCertificateException()
    val certs = filteredMap[highestScheme] ?: throw NoCertificateException()

    if (certs.isEmpty()) throw NoCertificateException()

    if (certs.size == 1) {
        return certs.first()
    }

    val issuerPrincipals = certs.map { it.issuerX500Principal }.toSet()
    return certs.firstOrNull { cert ->
        !issuerPrincipals.contains(cert.subjectX500Principal)
    } ?: certs.first()
}

/**
 * Computes the lowercase hex-encoded SHA-1 fingerprint of the original APK signing certificate.
 */
fun extractSignatureSHA1(schemeToCertsMap: Map<ApkSignatureScheme, List<X509Certificate>>): String {
    val cert = getEndEntityCertificate(schemeToCertsMap)
    val digest = MessageDigest.getInstance("SHA-1").digest(cert.encoded)
    return digest.joinToString("") { "%02x".format(it) }
}
