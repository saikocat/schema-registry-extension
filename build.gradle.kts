plugins {
    kotlin("jvm") version DepVersions.kotlin
    id("com.diffplug.spotless") version DepVersions.spotless
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        maven { url = uri("https://packages.confluent.io/maven/") }
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            // by default the target is every '.kt' and '.kts` file in the java sourcesets
            ktlint()
            ktfmt().dropboxStyle()
            // prettier(mapOf("prettier-plugin-kotlin" to "2.0.0")).config(mapOf("parser" to "kotlin"))
            // licenseHeaderFile("${rootProject.projectDir}/LICENSE_HEADER")
            licenseHeaderFile(rootProject.file("LICENSE_HEADER"))
        }

        kotlinGradle {
            target("*.gradle.kts")
            // ktlint()
            // ktfmt()
            // prettier()
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}
