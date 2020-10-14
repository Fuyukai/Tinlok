/*
 * Copyright (C) 2020 Charlotte Skye.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

val LIB_NAME = "libtls"

kotlin {
    val t = targets.filterIsInstance<KotlinNativeTarget>()
    configure(t) {
        val main by compilations.getting
        val interop by main.cinterops.creating {
            defFile("src/cinterop/$LIB_NAME.def")
            includeDirs("src/cinterop")
            packageName = "tf.lotte.tinlok.interop.$LIB_NAME"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":tinlok-core"))
            }
        }
    }
}

// https://youtrack.jetbrains.com/issue/KT-41887
afterEvaluate {
    configurations.configureEach {
        if (name == "metadataCompileClasspath") {
            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named("kotlin-metadata"))
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("TLS powered by libtls.")
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
            url = uri("$ROOT/tinlok-tls-libtls/;publish=1;override=1")

            credentials {
                username = System.getenv("BINTRAY_USERNAME")
                password = System.getenv("BINTRAY_KEY")
            }
        }
    }
}


