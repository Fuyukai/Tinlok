/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

@file:Suppress("PropertyName")

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    id("de.undercouch.download")
}


kotlin {
    linuxX64 {
        sourceSets["linuxX64Main"].apply {
            dependencies {
                implementation(project(":tinlok-static-monocypher"))
                implementation(project(":tinlok-static-openssl"))
                implementation(project(":tinlok-static-libuuid"))
            }
        }

        val main = compilations.getByName("main")
        main.cinterops.create("extra") {
            defFile(project.file("src/linuxMain/cinterop/linux_extra.def"))
        }
    }

    linuxArm64 {
        sourceSets["linuxArm64Main"].apply {
            dependencies {
                implementation(project(":tinlok-static-monocypher"))
                implementation(project(":tinlok-static-openssl"))
                implementation(project(":tinlok-static-libuuid"))
            }
        }

        val main = compilations.getByName("main")
        main.cinterops.create("extra") {
            defFile(project.file("src/linuxMain/cinterop/linux_extra.def"))
            compilerOpts += "-I/usr/include"
        }
    }

    mingwX64 {
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
}

/**
 * Copies from linuxX64Main to the specified sourceset.
 */
fun Task.copyLinuxMain(dir: String, file: String, setTo: String) = copy {
    from("src/linuxX64Main/kotlin/tf/lotte/tinlok/$dir/${file}.kt")
    into("src/${setTo}Main/kotlin/tf/lotte/tinlok/$dir")
}

/**
 * Copies from linuxX64Test to the specified sourceset.
 */
fun Task.copyLinuxTest(dir: String, file: String, setTo: String) = copy {
    from("src/linuxX64Test/kotlin/tf/lotte/tinlok/$dir/${file}.kt")
    into("src/${setTo}Test/kotlin/tf/lotte/tinlok/$dir")
}

// commonizer or expect/actual hacks
tasks.register("copyCommonCBindings") {
    group = "interop"

    // cryptography
    copyLinuxMain("crypto", "Blake2b", "linuxArm64")
    copyLinuxMain("crypto", "Argon2i", "linuxArm64")
    copyLinuxMain("crypto", "CryptoLLA", "linuxArm64")
    copyLinuxMain("crypto", "Blake2b", "mingwX64")
    copyLinuxMain("crypto", "Argon2i", "mingwX64")
    copyLinuxMain("crypto", "CryptoLLA", "mingwX64")

    // common libuuid binding
    copyLinuxMain("util", "UuidPlatform", "linuxArm64")
}

// Fuck! I hate this!
tasks.register("copyCommonTests") {
    group = "verification"

    copyLinuxTest("concurrent", "Test Reentrant Lock", "linuxArm64")
    copyLinuxTest("concurrent", "Test Reentrant Lock", "mingwX64")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
    dependsOn(tasks.named("copyCommonCBindings"))
}

tasks.filter { it.name.startsWith("compileTest") }.forEach {
    it.dependsOn(tasks.named("copyCommonTests"))
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
