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

    sourceSets.all {
        dependencies {
            implementation(project(":tinlok-core"))
        }
    }
}

