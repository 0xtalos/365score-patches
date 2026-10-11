package app.morphe.patches.score365.microg

import app.morphe.patches.score365.utils.COMPATIBILITY_365SCORE
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import org.w3c.dom.Element

/**
 * Manifest modifications for MicroG / ReVanced GmsCore support:
 * 1. Spoofs the genuine package signature so GmsCore can authenticate with Google servers.
 * 2. Injects the GmsCore package name and queries declaration.
 * 3. Redirects C2DM push permissions to GmsCore.
 */
val microGResourcePatch = resourcePatch {
    execute {
        val sha1 = try {
            extractSignatureSHA1(packageMetadata.signingCertificates)
        } catch (_: Exception) {
            null
        }

        document("AndroidManifest.xml").use { document ->
            val applicationNodes = document.getElementsByTagName("application")
            if (applicationNodes.length > 0) {
                val applicationNode = applicationNodes.item(0) as Element

                if (sha1 != null) {
                    val sigElement = document.createElement("meta-data")
                    sigElement.setAttribute(
                        "android:name",
                        "${MicroGConstants.GMS_CORE_VENDOR_GROUP_ID}.android.gms.SPOOFED_PACKAGE_SIGNATURE",
                    )
                    sigElement.setAttribute("android:value", sha1)
                    applicationNode.appendChild(sigElement)
                }

                val microgPkgElement = document.createElement("meta-data")
                microgPkgElement.setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                microgPkgElement.setAttribute("android:value", MicroGConstants.GMS_CORE_PACKAGE_NAME)
                applicationNode.appendChild(microgPkgElement)
            }
        }

        val manifestFile = get("AndroidManifest.xml")
        var manifestText = manifestFile.readText()
        manifestText = manifestText.replace(
            "com.google.android.c2dm",
            "${MicroGConstants.GMS_CORE_VENDOR_GROUP_ID}.android.c2dm",
        )
        if (manifestText.contains("</queries>")) {
            manifestText = manifestText.replace(
                "</queries>",
                "<package android:name=\"${MicroGConstants.GMS_CORE_PACKAGE_NAME}\"/></queries>",
            )
        }
        manifestFile.writeText(manifestText)
    }
}

/**
 * Bytecode modifications for MicroG / ReVanced GmsCore support:
 * 1. Redirects all string references from "com.google.android.gms" to "app.revanced.android.gms".
 * 2. Redirects GMS actions, authorities, and content URIs to ReVanced GmsCore.
 * 3. Forces Google Play Services availability checks to return SUCCESS (0).
 */
@Suppress("unused")
val microGSupportPatch = bytecodePatch(
    name = "MicroG integration",
    description = "Allows Google Sign-In and account sync to work on non-rooted devices by redirecting Google Play Services to ReVanced GmsCore / MicroG.",
    default = false,
) {
    compatibleWith(*COMPATIBILITY_365SCORE)
    dependsOn(microGResourcePatch)

    execute {
        fun commonTransform(referencedString: String): String? = when (referencedString) {
            "com.google.android.gms" -> MicroGConstants.GMS_CORE_PACKAGE_NAME
            in MicroGConstants.ACTIONS,
            in MicroGConstants.AUTHORITIES -> referencedString.replace(
                "com.google",
                MicroGConstants.GMS_CORE_VENDOR_GROUP_ID,
            )
            "subscribedfeeds" -> "${MicroGConstants.GMS_CORE_VENDOR_GROUP_ID}.subscribedfeeds"
            else -> null
        }

        fun contentUrisTransform(str: String): String? {
            if (str.startsWith("content://")) {
                for (authority in MicroGConstants.AUTHORITIES) {
                    val uriPrefix = "content://$authority"
                    if (str.startsWith(uriPrefix)) {
                        return str.replace(
                            uriPrefix,
                            "content://${authority.replace("com.google", MicroGConstants.GMS_CORE_VENDOR_GROUP_ID)}",
                        )
                    }
                }
            }
            return null
        }

        // 1. Transform GMS string references across all classes
        classDefForEach { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            for (method in mutableClass.methods) {
                if (method.implementation == null) continue
                for (instruction in method.instructions) {
                    if (instruction.opcode == Opcode.CONST_STRING || instruction.opcode == Opcode.CONST_STRING_JUMBO) {
                        val ref = (instruction as? Instruction21c)?.reference as? StringReference ?: continue
                        val transformed = commonTransform(ref.string) ?: contentUrisTransform(ref.string) ?: continue
                        val register = instruction.registerA
                        method.replaceInstruction(
                            instruction.location.index,
                            BuilderInstruction21c(
                                instruction.opcode,
                                register,
                                ImmutableStringReference(transformed),
                            ),
                        )
                    }
                }
            }
        }

        // 2. Bypass Google Play Services availability checks
        val availabilityTargets = listOf(
            GooglePlayServicesUtilFingerprint,
            GooglePlayServicesUtilLightCheckFingerprint,
            GoogleApiAvailabilityLightCheckFingerprint,
            GoogleApiAvailabilityLightContextOnlyFingerprint,
        )

        for (target in availabilityTargets) {
            val method = target.methodOrNull ?: continue
            if (method.implementation != null) {
                method.addInstructions(
                    0,
                    """
                    const/4 v0, 0x0
                    return v0
                    """.trimIndent(),
                )
            }
        }

        ServiceCheckFingerprint.methodOrNull?.let { method ->
            if (method.implementation != null) {
                method.addInstructions(0, "return-void")
            }
        }
    }
}
