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
    val mingw64 = mingwX64()

    listOf(x64, aarch64, mingw64).forEach {
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
            url.set("https://github.com/jrepp/ipv6-parse")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("jrepp")
                    name.set("Jacob Repp")
                    url.set("https://github.com/jrepp")
                }
            }
        }
    }

    repositories {
        maven {
            val ROOT = "https://api.bintray.com/maven/constellarise/tinlok"
            url = uri("$ROOT/tinlok-static-ipv6/;publish=1;override=1")

            credentials {
                username = System.getenv("BINTRAY_USERNAME")
                password = System.getenv("BINTRAY_KEY")
            }
        }
    }
}
