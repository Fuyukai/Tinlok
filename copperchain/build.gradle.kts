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

group = "tf.lotte.copperchain"

kotlin {
    // == JVM targets == //
    jvm()

    // == Native targets == //
    // = Linux = //
    linuxArm32Hfp()
    linuxX64()
    linuxArm64()
    linuxMips32()
    linuxMipsel32()

    // = Windows = //
    mingwX64()
    mingwX86()

    // = Darwin = //
    macosX64()
    iosArm32()
    iosArm64()
    iosX64()
    watchosArm32()
    watchosArm64()
    watchosX86()
    tvosX64()
    tvosArm64()

    sourceSets {
        val nonJvmShared by creating {
            dependsOn(commonMain.get())
        }

        // ALL non-jvm source sets depend on nonJvmShared
        val filtered = filterNot {
            it.name.startsWith("jvm") || it.name.startsWith("common") || it.name == "nonJvmShared"
        }
        configure(filtered) {
            dependsOn(nonJvmShared)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }

    sourceSets.all {
        dependencies {
            // stop intellij freaking out
            implementation(kotlin("stdlib"))
        }

        languageSettings.apply {
            useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("A common module for various interfaces and utilities.")
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

