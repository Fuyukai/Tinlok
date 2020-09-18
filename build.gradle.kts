import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    id("org.jetbrains.kotlin.multiplatform").version("1.4.10").apply(false)
    id("org.jetbrains.dokka").version("1.4.0").apply(true)
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }
}

subprojects {
    // ignore all -static projects, we configure K/N ourselves
    if (this.name.startsWith("tinlok-static")) return@subprojects

    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "org.jetbrains.dokka")

    group = "tf.lotte.tinlok"
    version = "1.0.0"

    configure<KotlinMultiplatformExtension> {
        explicitApi = ExplicitApiMode.Strict

        // linux targets
        linuxX64()
        linuxArm64()

        // temp disabled
        // darwin targets
        // macosX64()

        // windows
        // mingwX64()


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

            all {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlin.RequiresOptIn")
                }
            }
        }
    }


    tasks.named<DokkaTask>("dokkaHtml") {
        dokkaSourceSets {
            configureEach {
                includeNonPublic.set(true)
            }
        }
    }
}

val clean = tasks.register<Exec>("sphinxClean") {
    group = "documentation"
    workingDir = project.rootDir.resolve("docs")
    commandLine("poetry run make clean".split(" "))
}

val sphinxCopy = tasks.register<Sync>("sphinxCopy") {
    group = "documentation"
    dependsOn(tasks.named("dokkaHtmlMultiModule"), clean)

    from("${project.buildDir}/dokka/htmlMultiModule")
    into("${project.rootDir}/docs/_external/_dokka")
}

tasks.register<Exec>("sphinxBuild") {
    group = "documentation"
    dependsOn(sphinxCopy)
    workingDir = project.rootDir.resolve("docs")
    commandLine("poetry run make html".split(" "))
}
