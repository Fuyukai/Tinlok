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
version = "1.0.0"

kotlin {
    // == JVM targets == //
    jvm()

    // == JS targets == //
    js() {
        browser()
        nodejs()
    }

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

    // = Misc = //
    wasm32()

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