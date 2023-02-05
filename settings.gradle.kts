pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android")) {
                useModule("com.android.tools.build:gradle:7.4.0")
                useVersion("7.4.0")
            }
            if (requested.id.id.startsWith("org.greenrobot")) {
                useModule("org.greenrobot:greendao-gradle-plugin:3.3.0")
            }
            if (requested.id.id.startsWith("org.jetbrains.kotlin")) {
                useVersion("1.7.10")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
				jcenter()
        maven { url = uri("https://jitpack.io") }
    }
    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
    }
}

rootProject.name = "MiPushFramework"

include(":push", ":common", ":provider", ":extenders")
