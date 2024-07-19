// Fix not working with java 21

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.github.patrick.remapper") version "1.+"
    id("maven-publish")
    java
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "io.github.goooler.shadow")

    dependencies {
        compileOnly("org.jetbrains:annotations:24.1.0")
    }

    group = "com.github.Outspending"
    version = "0.0.2"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    // needs to be in after evaluate, otherwise the project's reobfJar task (if it exists)
    // won't be there.
    afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    groupId = "io.github.paulem"
                    artifactId = project.name
                    from(components["java"])
                }
            }
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

            // As of Gradle 5.1, you can limit this to only those
            // dependencies you expect from it
            content {
                includeGroup ("org.bukkit")
                includeGroup ("org.spigotmc")
            }
        }
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
    }
}

tasks.remap {
    version.set("1.18")
}

tasks.build {
    dependsOn(tasks.remap)
}

val nmsVersions = listOf("1.19_R1", "1.19_R2", "1.19_R3", "1.20_R1", "1.20_R2", "1.20_R3", "1.20_R4", "1.21_R1")
// 1.19 - 1.20.6
dependencies {
    compileOnly("org.spigotmc:spigot:1.18-R0.1-SNAPSHOT:remapped-mojang")

    // NMS Implementations
    implementation(project(":NMS:Wrapper"))
    for (version in nmsVersions) {
        implementation(project(path = ":NMS:${version}", configuration = "shadow"))
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        mergeServiceFiles()
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        //languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks.wrapper {
    gradleVersion = "8.9"
    distributionType = Wrapper.DistributionType.ALL
}