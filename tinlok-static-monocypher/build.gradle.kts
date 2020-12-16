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

val LIB_NAME = "libmonocypher"

kotlin {
    val amd64 = linuxX64()
    val aarch64 = linuxArm64()
    val mingw64 = mingwX64()

    listOf(amd64, aarch64, mingw64).forEach {
        val main by it.compilations.getting {
            val sourceSet = defaultSourceSetName
            val libPath = "src/$sourceSet/$LIB_NAME.a"
            val staticLib = project.file(libPath)
            val path = staticLib.absolutePath

            kotlinOptions {
                freeCompilerArgs = listOf("-include-binary", path)
            }
        }


        val interop by main.cinterops.creating {
            defFile("src/cinterop/$LIB_NAME.def")
            includeDirs("src/cinterop")
            packageName = "tf.lotte.tinlok.interop.$LIB_NAME"
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

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("A statically provided copy of the Monocypher library.")
            url.set("https://github.com/LoupVaillant/Monocypher")

            licenses {
                license {
                    name.set("BSD-2-Clause")
                    url.set("https://opensource.org/licenses/BSD-2-Clause")
                }
            }

            developers {
                developer {
                    id.set("LoupVaillant")
                    name.set("Loup Vaillant")
                    url.set("https://github.com/LoupVaillant")
                }
            }
        }
    }

    repositories {
        maven {
            val ROOT = "https://api.bintray.com/maven/constellarise/tinlok"
            url = uri("$ROOT/tinlok-static-monocypher/;publish=1;override=1")

            credentials {
                username = System.getenv("BINTRAY_USERNAME")
                password = System.getenv("BINTRAY_KEY")
            }
        }
    }
}
