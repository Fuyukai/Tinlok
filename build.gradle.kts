import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform").version("1.4.10").apply(false)
}

subprojects {
    // ignore all -static projects, we configure K/N ourselves
    if (this.name.startsWith("tinlok-static")) return@subprojects

    apply(plugin = "org.jetbrains.kotlin.multiplatform")

    group = "tf.lotte.tinlok"
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

        // temp disabled
        // darwin targets
        //macosX64()

        // windows
        mingwX64()


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

            // native main sourceset, allows us access to cinterop
            val nativeMain by creating {
                dependsOn(commonMain)
            }

            // linux sourcesets all share a sourceset
            val linuxMain by creating { dependsOn(nativeMain) }
            val linuxX64Main by getting { dependsOn(linuxMain) }
            val linuxArm64Main by getting { dependsOn(linuxMain) }

            val mingwX64Main by getting {
                dependsOn(nativeMain)
            }

            all {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlin.RequiresOptIn")
                    //useExperimentalAnnotation("tf.lotte.knste.util.Unsafe")
                }
            }
        }
    }
}
