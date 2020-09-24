/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

// copied from static-ipv6...

kotlin {
    val x64 = linuxX64()
    val aarch64 = linuxArm64()

    listOf(x64, aarch64).forEach {
        // remove useless test compilations
        it.compilations.removeIf { cmp -> cmp.name == "test" }

        val main by it.compilations.getting {
            val sourceSet = defaultSourceSetName
            val libPath = "src/$sourceSet/libmonocypher.a"

            // statically include the library .a file within the final klib
            val libFile = project.file(libPath)
            check(libFile.exists()) { "Missing static library: $libFile" }

            val path = libFile.absolutePath

            kotlinOptions {
                freeCompilerArgs = listOf("-include-binary", path)
            }
        }

        // enable the interop for this target, using the common sourceset
        val interop by main.cinterops.creating {
            defFile("src/cinterop/monocypher.def")
            includeDirs("src/cinterop")
            packageName = "tf.lotte.tinlok.interop.monocypher"
        }
    }


    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
    }
}

tasks.removeIf { it is Test }

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("A statically provided copy of the Monocypher library.")
            url.set("https://monocypher.org/")

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
}
