/*
 * Copyright (C) 2020-2021 Lura Skye Revuwution.
 *
 * This file is part of Tinlok.
 *
 * Tinlok is dually released under the GNU Lesser General Public License,
 * Version 3 or later, or the Mozilla Public License 2.0.
 */

import de.undercouch.gradle.tasks.download.Download

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
    id("de.undercouch.download")
}

val LIBS = listOf("libssl", "libcrypto")

kotlin {
    val amd64 = linuxX64()
    val aarch64 = linuxArm64()
    val mingw64 = mingwX64()

    listOf(amd64, mingw64).forEach {
        val main by it.compilations.getting {
            val sourceSet = defaultSourceSetName
            val options = mutableListOf<String>()
            for (library in LIBS) {
                val libPath = "src/$sourceSet/$library.a"
                val staticLib = project.file(libPath)
                val path = staticLib.absolutePath
                options.add("-include-binary")
                options.add(path)
            }

            kotlinOptions {
                freeCompilerArgs = options
            }
        }

    }
    listOf(amd64, aarch64, mingw64).forEach {
        val main = it.compilations["main"]
        val interop by main.cinterops.creating {
            defFile("src/cinterop/openssl.def")
            includeDirs(rootProject.file("vendor"))
            packageName = "external.openssl"
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

// bundle our own CA certs
tasks.register<Download>("downloadCACerts") {
    src("https://curl.haxx.se/ca/cacert.pem")
    dest(project.buildDir.resolve("certs.pem"))
    onlyIfModified(true)
}

tasks.register("createCAFile") {
    dependsOn(tasks.named("downloadCACerts"))
    val out = project.file("src/commonMain/kotlin/CaCerts.kt")

    onlyIf {
        val parent = tasks.named("downloadCACerts").get()
        !out.exists() || !parent.state.upToDate
    }

    doLast {
        out.parentFile.mkdirs()

        val certsIn = project.buildDir.resolve("certs.pem")
        val content = certsIn.readText(Charsets.UTF_8)
        val template = "/* This file was automatically generated. Do not edit! */\n" +
            "package external.openssl.certs\n" +
            "public const val CA_BUNDLE = \"\"\"$content\"\"\""

        out.writeText(template)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
    dependsOn(tasks.named("createCAFile"))
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("A statically provided copy of the OpenSSL library.")
            url.set("https://www.openssl.org/")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
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
