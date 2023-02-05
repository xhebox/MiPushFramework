plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
}

fun versionBanner(): String {
    val os = org.apache.commons.io.output.ByteArrayOutputStream()
    project.exec {
        commandLine = "git describe --dirty --always".split(" ")
        standardOutput = os
    }
    return String(os.toByteArray()).trim()
}

val minSdkVersion by extra(21)
val compileSdkVersion by extra(33)
val targetSdkVersion by extra(30)
val pushVersionCode by extra(7)
val gitTag by extra(versionBanner())
val versionName by extra("0.3.7-${versionBanner()}")
