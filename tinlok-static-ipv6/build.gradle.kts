// This is a hack module to build libipv6-parse.a into our klib, because .def files don't support
// relative paths and you can't add it in the cinterop configuration.

// https://github.com/Dominaezzz/kgl/blob/master/kgl-glfw-static/build.gradle.kts

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
}

kotlin {
    val x64 = linuxX64()
    val aarch64 = linuxArm64()

    listOf(x64, aarch64).forEach {
        val main by it.compilations.getting {
            val sourceSet = defaultSourceSetName
            val libPath = "src/$sourceSet/libipv6-parse.a"

            // statically include the platform-specific ipv6 parser .a file within the final klib
            val ipv6StaticLib = project.file(libPath)
            val path = ipv6StaticLib.absolutePath

            kotlinOptions {
                freeCompilerArgs = listOf("-include-binary", path)
            }
        }

        // enable the interop for this target, using the common sourceset
        val ipv6Interop by main.cinterops.creating {
            defFile("src/cinterop/ipv6.def")
            includeDirs("src/cinterop")
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

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set(project.name)
            description.set("A statically provided copy of the ipv6-parse library.")
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
