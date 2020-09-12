// Fuck you JetBrains.
// It is very clear to me whilst working on this that desktop support is clearly incidental at
// best. It would be better for the entirety of K/N if you stop fucking pretending that you care
// about building on desktop platforms. I've had to do multiple hacks to get things working
// (which, when I looked into issues, other projects, etc, you just simply ignore so you KNOW
// these hacks exist). Just drop any pretense that K/N is for anything other than iOS development
// and come back with a proper desktop version if (when???) you are ready to SUPPORT it properly
// instead of it being a fucking oversight.

// This is a hack module to build libipv6-parse.a into our klib, because .def files don't support
// relative paths, you can't add it in the cinterop configuration, and YouTrack is a fucking
// black hole.

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
            packageName = "tf.lotte.knste.interop.ipv6"
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
