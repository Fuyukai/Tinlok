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

val LIB_NAME = "libtls"

kotlin {
    val amd64 = linuxX64()
    val aarch64 = linuxArm64()

    listOf(amd64, aarch64).forEach {
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
