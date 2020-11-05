/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("PropertyName")

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.nio.file.Files
import java.nio.file.Path


plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}


// == Architecture detection == //
val ARCH = DefaultNativePlatform.getCurrentArchitecture()
val DEFAULT_SEARCH_PATHS = listOf("/usr/lib", "/lib").map { Path.of(it) }

/**
 * Gets the AArch64 library paths for linking.
 */
fun getAarch64LibraryPaths(): List<Path> {
    // native ARM library path
    if (ARCH.isArm) return DEFAULT_SEARCH_PATHS

    // dpkg multiplatform path, debian cross-compiler lib path
    val tryPaths = listOf(
        "/usr/lib/aarch64-linux-gnu",
        "/lib/aarch64-linux-gnu",
        "/usr/aarch64-linux-gnu/lib"
    )
    return tryPaths.map { Path.of(it) }.filter { Files.exists(it) }
}

/**
 * Gets the AMD64 library paths for linking.
 */
fun getAMD64LibraryPaths(): List<Path> {
    // our own native paths
    if (ARCH.isAmd64) return DEFAULT_SEARCH_PATHS

    // this can be a few paths...
    val tryPaths = listOf(
        "/usr/x86_64-pc-linux-gnu",  // Arch convention
        "/usr/lib/x86_64-pc-linux-gnu",

        "/usr/x86_64-linux-gnu/lib",  // dpkg cross-compile convention
        "/usr/lib/x86_64-linux-gnu",
        "/lib/x86_64-linux-gnu"
    )

    // dpkg multiplatform path, debian cross-compiler lib path
    return tryPaths.map { Path.of(it) }.filter { Files.exists(it) }
}

// == End architecture detection == //

kotlin {
    val x64 = linuxX64() {
        val linuxX64Main by sourceSets.getting {
            dependencies {
                implementation(project(":tinlok-static-monocypher"))
            }
        }

        val main = compilations.getByName("main")
        main.cinterops.create("uuid") {
            defFile(project.file("src/linuxMain/cinterop/uuid.def"))
        }
    }

    val arm64 = linuxArm64() {
        val linuxMain by sourceSets.getting
        val linuxArm64Main by sourceSets.getting {
            dependencies {
                implementation(project(":tinlok-static-monocypher"))
            }
        }

        val main = compilations.getByName("main")
        main.cinterops.create("uuid") {
            defFile(project.file("src/linuxMain/cinterop/uuid.def"))
        }
    }

    mingwX64() {
        val mingwX64Main by sourceSets.getting {
            dependencies {
                implementation(project(":tinlok-static-monocypher"))
            }
        }

        val main = compilations.getByName("main")
        main.cinterops.create("ddk") {
            val path = project.file("src/mingwX64Main/cinterop/ddk.def")
            defFile(path)
        }
    }

    sourceSets.commonMain {
        dependencies {
            api(project(":copperchain"))
        }
    }
}

// commonizer hack
tasks.register("copyCommonCBindings") {
    copy {
        from(
            "src/linuxX64Main/kotlin/tf/lotte/tinlok/crypto/Blake2b.kt",
            "src/linuxX64Main/kotlin/tf/lotte/tinlok/crypto/Argon2i.kt",
            "src/linuxX64Main/kotlin/tf/lotte/tinlok/crypto/CryptoLLA.kt"
        )
        into("src/linuxArm64Main/kotlin/tf/lotte/tinlok/crypto")
    }

    copy {
        from(
            "src/linuxX64Main/kotlin/tf/lotte/tinlok/crypto/Blake2b.kt",
            "src/linuxX64Main/kotlin/tf/lotte/tinlok/crypto/Argon2i.kt",
            "src/linuxX64Main/kotlin/tf/lotte/tinlok/crypto/CryptoLLA.kt"
        )
        into("src/mingwX64Main/kotlin/tf/lotte/tinlok/crypto")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
    dependsOn(tasks.named("copyCommonCBindings"))
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("The core Tinlok module.")
            url.set("https://tinlok.lotte.tf")

            licenses {
                license {
                    name.set("LGPL-3.0-or-later")
                    url.set("https://www.gnu.org/licenses/lgpl-3.0.en.html")
                }
                license {
                    name.set("MPL-2.0")
                    url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                }
            }

            developers {
                developer {
                    id.set("Constellarise")
                    name.set("Charlotte Skye")
                    url.set("https://lotte.tf")
                }
            }
        }
    }

    repositories {
        maven {
            val ROOT = "https://api.bintray.com/maven/constellarise/tinlok"
            url = uri("$ROOT/tinlok-core/;publish=1;override=1")

            credentials {
                username = System.getenv("BINTRAY_USERNAME")
                password = System.getenv("BINTRAY_KEY")
            }
        }
    }
}
