import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    // configure linux cinterop for correct type definitions
    val linux = targets.filterIsInstanceAnd<KotlinNativeTarget> { it.name.startsWith("linux") }
    configure(linux) {
        val main by compilations.getting
        val linuxInterop by main.cinterops.creating {
            defFile(project.file("src/linuxMain/linux.def"))
        }
    }
}
