import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    id("org.jetbrains.kotlin.multiplatform").version("1.4.0").apply(false)
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")

    group = "tf.lotte.kste"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    configure<KotlinMultiplatformExtension> {
        explicitApi = ExplicitApiMode.Strict

        jvm {
            val main by compilations.getting
            val test by compilations.getting

            listOf(main, test).forEach {
                it.kotlinOptions {
                    jvmTarget = "13"
                    useIR = true
                    freeCompilerArgs = listOf("-Xjvm-default=all", "-Xemit-jvm-type-annotations")
                }
            }
        }

        linuxX64()

        sourceSets {
            val commonMain by getting
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                }
            }

            val jvmMain by getting {
                dependencies {
                    // prevents weird errors...
                    implementation(kotlin("reflect"))
                }
            }

            val jvmTest by getting {
                dependencies {
                    implementation(kotlin("test-junit"))
                }
            }

        }
    }
}
