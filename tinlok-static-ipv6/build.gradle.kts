// This is a hack module to build libipv6-parse.a into our klib, because .def files don't support
// relative paths and you can't add it in the cinterop configuration.

// https://github.com/Dominaezzz/kgl/blob/master/kgl-glfw-static/build.gradle.kts

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    linuxX64() {
        val main by compilations.getting {
            val ipv6StaticLib = project.file("src/linuxX64Main/interop/libipv6-parse.a")
            val path = ipv6StaticLib.absolutePath
            kotlinOptions {
                freeCompilerArgs = listOf("-include-binary", path)
            }
        }

        // libipv6-parse
        val ipv6Interop by main.cinterops.creating {
            defFile("src/linuxX64Main/interop/ipv6.def")
            includeDirs("src/linuxX64Main/interop")
            packageName = "tf.lotte.tinlok.interop.ipv6"
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
