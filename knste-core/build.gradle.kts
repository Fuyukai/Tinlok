plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {

    linuxX64() {
        val linuxX64Main by sourceSets.getting {
            dependencies {
                api(project(":knste-static-ipv6"))
            }
        }
    }
}
