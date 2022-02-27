/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
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
    val linuxMain by sourceSets.getting {
        dependencies {
            implementation(project(":tinlok-static-monocypher"))
            implementation(project(":tinlok-static-libuuid"))
        }
    }

    linuxX64 {
        val main = compilations.getByName("main")
        main.cinterops.create("extra") {
            defFile(project.file("src/linuxMain/cinterop/linux_extra.def"))
        }
    }

    linuxArm64 {
        val main = compilations.getByName("main")
        main.cinterops.create("extra") {
            defFile(project.file("src/linuxMain/cinterop/linux_extra.def"))
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

        main.cinterops.create("winsock2") {
            val path = project.file("src/mingwX64Main/cinterop/sockwrapper.def")
            defFile(path)
        }
    }
}

/**
 * Copies from linuxX64Main to the specified sourceset.
 */
fun Task.copyLinuxMain(dir: String, file: String, setTo: String) = copy {
    from("src/linuxX64Main/kotlin/tf/veriny/tinlok/$dir/${file}.kt")
    into("src/${setTo}Main/kotlin/tf/veriny/tinlok/$dir")
}

/**
 * Copies from linuxX64Test to the specified sourceset.
 */
fun Task.copyLinuxTest(dir: String, file: String, setTo: String) = copy {
    from("src/linuxX64Test/kotlin/tf/veriny/tinlok/$dir/${file}.kt")
    into("src/${setTo}Test/kotlin/tf/veriny/tinlok/$dir")
}

// Fuck! I hate this!
tasks.register("copyCommonTests") {
    group = "verification"

    copyLinuxTest("concurrent", "Test Reentrant Lock", "linuxArm64")
    copyLinuxTest("concurrent", "Test Reentrant Lock", "mingwX64")
}


tasks.filter { it.name.startsWith("compileTest") }.forEach {
    it.dependsOn(tasks.named("copyCommonTests"))
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("The core Tinlok module.")
            url.set("https://tinlok.veriny.tf")

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
                    id.set("Fuyukai")
                    name.set("Lura Skye")
                    url.set("https://veriny.tf")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://maven.veriny.tf/releases")

            credentials {
                username = project.properties["verinyUsername"] as? String
                password = project.properties["verinyPassword"] as? String
            }
        }
    }
}
