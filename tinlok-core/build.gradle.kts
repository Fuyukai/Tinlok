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

kotlin {
    linuxX64() {
        val linuxX64Main by sourceSets.getting {
            dependencies {
                implementation(project(":tinlok-static-ipv6"))
                implementation(project(":tinlok-static-monocypher"))
            }
        }
    }

    linuxArm64() {
        val linuxMain by sourceSets.getting
        val linuxArm64Main by sourceSets.getting {
            dependsOn(linuxMain)

            dependencies {
                implementation(project(":tinlok-static-ipv6"))
                implementation(project(":tinlok-static-monocypher"))
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("The core components for Tinlok.")
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
}
