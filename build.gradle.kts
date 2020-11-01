/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.nio.file.Files
import java.nio.file.Path

plugins {
    id("org.jetbrains.kotlin.multiplatform").version("1.4.10").apply(false)
    /*id("org.jetbrains.dokka").version("1.4.0").apply(true)*/
    id("maven-publish")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }
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
 * Determines if the current system is AArch64, or if we have a cross-compiler installed.
 */
fun hasAarch64(): Boolean {
    if (DefaultNativePlatform.getCurrentArchitecture().isArm) {
        return true
    }

    return getAarch64LibraryPaths().isNotEmpty()
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

/**
 * Determines if the current system is AMD64, or if we have a cross-compiler installed.
 */
fun hasAmd64(): Boolean {
    if (ARCH.isAmd64) {
        return true
    }
    return getAMD64LibraryPaths().isNotEmpty()
}
// == End architecture detection == //


subprojects {
    // ALL projects get the appropriately tracked version
    version = "1.2.0"

    // ignore the non tinlok branded project
    if (!this.name.startsWith("tinlok-")) return@subprojects

    // all projects get the group
    group = "tf.lotte.tinlok"

    // ignore all -static projects, we configure K/N ourselves
    if (this.name.startsWith("tinlok-static")) return@subprojects

    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    //apply(plugin = "org.jetbrains.dokka")

    // core kotlin configuration
    // (this is collapsed in my IDE)
    configure<KotlinMultiplatformExtension> {
        explicitApi = ExplicitApiMode.Strict

        // == Linux Targets == //
        // = AMD64 = //
        if (hasAmd64()) {
            linuxX64()
        }
        // = AArch64 = //
        if (hasAarch64()) {
            linuxArm64()
        }

        // == Darwin Targets == //
        // = OSX (Intel) = //
        // macosX64()

        // == Windows Targets == //
        // = Windows (AMD64) = //
        mingwX64()


        sourceSets {
            val commonMain by getting {
                dependencies {
                    // required to stop intellij from flipping out
                    implementation(kotlin("stdlib"))
                    implementation(kotlin("reflect"))
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                }
            }

            // native main sourceset, allows us access to cinterop
            val nativeMain by creating {
                dependsOn(commonMain)
            }

            // linux sourcesets all share a sourceset
            val linuxMain by creating { dependsOn(nativeMain) }

            if (hasAmd64()) {
                val main = sourceSets.getByName("linuxX64Main")
                main.dependsOn(linuxMain)
            }

            if (hasAarch64()) {
                val main = sourceSets.getByName("linuxArm64Main")
                main.dependsOn(linuxMain)
            }

            val mingwX64Main by getting {
                dependsOn(nativeMain)
            }

            all {
                languageSettings.apply {
                    enableLanguageFeature("InlineClasses")
                    useExperimentalAnnotation("kotlin.RequiresOptIn")
                }
            }
        }
    }

    // core dokka configuration
    // (equally collapsed)
    /*tasks.named<DokkaTask>("dokkaHtml") {
        dokkaSourceSets {
            configureEach {
                includeNonPublic.set(true)
            }
        }
    }*/
}

val sphinxClean = tasks.register<Exec>("sphinxClean") {
    group = "documentation"
    workingDir = project.rootDir.resolve("docs")
    commandLine("poetry run make clean".split(" "))
}

/*val sphinxCopy = tasks.register<Sync>("sphinxCopy") {
    group = "documentation"
    dependsOn(tasks.named("dokkaHtmlMultiModule"), clean)

    from("${project.buildDir}/dokka/htmlMultiModule")
    into("${project.rootDir}/docs/_external/_dokka")
}*/

tasks.register<Exec>("sphinxBuild") {
    group = "documentation"
    dependsOn(sphinxClean)
    workingDir = project.rootDir.resolve("docs")
    commandLine("poetry run make html".split(" "))
}
