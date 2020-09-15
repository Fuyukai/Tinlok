plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    linuxX64() {
        val linuxX64Main by sourceSets.getting {
            dependencies {
                // staticly compiled IPv6 helper
                api(project(":tinlok-static-ipv6"))
            }
        }
    }
}
