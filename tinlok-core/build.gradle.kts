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
    val x64 = linuxX64() {
        val linuxX64Main by sourceSets.getting {
            dependencies {
                implementation(project(":tinlok-static-ipv6"))
                implementation(project(":tinlok-static-monocypher"))
            }
        }
    }

    val arm64 = linuxArm64() {
        val linuxMain by sourceSets.getting
        val linuxArm64Main by sourceSets.getting {
            dependsOn(linuxMain)

            dependencies {
                implementation(project(":tinlok-static-ipv6"))
                implementation(project(":tinlok-static-monocypher"))
            }
        }
    }

    listOf(x64, arm64).forEach {
        val main = it.compilations.getByName("main")
        val uuid by main.cinterops.creating {
            defFile(project.file("src/linuxMain/cinterop/uuid.def"))
        }
    }

    mingwX64() {
        val mingwX64Main by sourceSets.getting {
            dependencies {
                implementation(project(":tinlok-static-ipv6"))
                implementation(project(":tinlok-static-monocypher"))
            }
        }

        val main by compilations.getting {
            val ddk by cinterops.creating {
                val path = project.file("src/mingwX64Main/cinterop/ddk.def")
                defFile(path)
            }
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

    copy {
        from("src/linuxX64Main/kotlin/tf/lotte/tinlok/net/IPAddressUtil.kt")
        into("src/linuxArm64Main/kotlin/tf/lotte/tinlok/net/")
    }

    copy {
        from("src/linuxX64Main/kotlin/tf/lotte/tinlok/net/IPAddressUtil.kt")
        into("src/mingwX64Main/kotlin/tf/lotte/tinlok/net/")
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
