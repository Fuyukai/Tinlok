import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.kotlin.multiplatform").version("1.4.0").apply(false)
    id("org.jetbrains.dokka").version("1.4.0").apply(false)
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "org.jetbrains.dokka")

    group = "tf.lotte.knste"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    configure<KotlinMultiplatformExtension> {
        explicitApi = ExplicitApiMode.Strict

        // linux targets
        linuxX64()
        linuxArm64()

        /* temp disabled
        // darwin targets
        macosX64()

        // windows
        mingwX64()
        */

        sourceSets {
            val commonMain by getting {
                dependencies {
                    // required to stop intellij from flipping out
                    implementation(kotlin("stdlib"))
                    implementation(kotlin("reflect"))
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                }
            }

            // linux sourcesets all share a sourceset
            val linuxMain by creating { dependsOn(commonMain) }
            val linuxX64Main by getting { dependsOn(linuxMain) }
            val linuxArm64Main by getting { dependsOn(linuxMain) }

            all {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlin.RequiresOptIn")
                    //useExperimentalAnnotation("tf.lotte.knste.util.Unsafe")
                }
            }
        }
    }

    tasks.named<DokkaTask>("dokkaHtml") {
        outputDirectory.set(buildDir.resolve("dokka"))

        dokkaSourceSets {
            configureEach {
                includeNonPublic.set(true)
            }
        }
    }
}
