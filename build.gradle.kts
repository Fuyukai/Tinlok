/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("PropertyName")

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.nio.file.Path

plugins {
    id("org.jetbrains.kotlin.multiplatform").version("1.6.20-M1").apply(false)
    id("com.diffplug.spotless").version("6.0.0").apply(false)
    id("com.github.ben-manes.versions").version("0.39.0").apply(false)
    id("maven-publish")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

// == Architecture detection == //
val ARCH = DefaultNativePlatform.getCurrentArchitecture()
val OS = DefaultNativePlatform.getCurrentOperatingSystem()
val DEFAULT_SEARCH_PATHS = listOf("/usr/lib", "/lib").map { Path.of(it) }

/**
 * Determines if the current system is AArch64, or if we have a cross-compiler installed.
 */
fun hasAarch64(): Boolean {
    return ARCH.isArm
}


/**
 * Determines if the current system is AMD64, or if we have a cross-compiler installed.
 */
fun hasAmd64(): Boolean {
    return ARCH.isAmd64
}

/**
 * Determines if we are Windows or not.
 */
fun hasWindows(): Boolean {
    return OS.isWindows
}

// == End architecture detection == //


subprojects {
    // ALL projects get the appropriately tracked version
    version = "1.4.1"
    // all projects get the group
    group = "tf.veriny.tinlok"

    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.ben-manes.versions")
    //apply(plugin = "org.jetbrains.dokka")

    // core kotlin configuration
    // (this is collapsed in my IDE)
    configure<KotlinMultiplatformExtension> {
        explicitApi = ExplicitApiMode.Strict

        // == Linux Targets == //
        // = AMD64 = //
        linuxX64()
        // = AArch64 = //
        linuxArm64()

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
            // and common posix stuff.
            val posixMain by creating {
                dependsOn(commonMain)
            }

            // linux sourcesets all share a sourceset
            val linuxMain by creating { dependsOn(posixMain) }

            val linuxMainX64 = sourceSets.getByName("linuxX64Main")
            linuxMainX64.dependsOn(linuxMain)

            val linuxMainArm = sourceSets.getByName("linuxArm64Main")
            linuxMainArm.dependsOn(linuxMain)


            val mingwX64Main by getting { dependsOn(posixMain) }

            all {
                languageSettings.apply {

                    enableLanguageFeature("InlineClasses")
                    optIn("kotlin.RequiresOptIn")
                }
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            ktlint().userData(mapOf(
                "disabled_rules" to "no-wildcard-imports",
                "max-line-length" to "100",
            ))

            target("src/**/kotlin/**")
            targetExclude("build/generated/**")
            licenseHeaderFile(rootProject.file("gradle/LICENCE-HEADER"))
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

    val linkTasks = tasks.filter { it.name.startsWith("link") }
    for (task in linkTasks) {
        when {
            task.name.endsWith("Arm64") -> {
                // disable all arm64 tasks, temporarily
                task.enabled = hasAarch64()
            }
            task.name.endsWith("MingwX64") -> {
                task.enabled = false
            }
            task.name.endsWith("X64") -> {
                task.enabled = hasAmd64()
            }
        }
    }
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
