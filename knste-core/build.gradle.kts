import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd

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
