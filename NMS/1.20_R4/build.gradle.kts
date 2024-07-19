plugins {
    id("io.github.patrick.remapper") version "1.+"
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.20.5-R0.1-SNAPSHOT:remapped-mojang")

    compileOnly(project(":NMS:Wrapper"))
}

tasks.remap {
    version.set("1.20.5")
}

tasks.build {
    dependsOn(tasks.remap)
}

java {
    toolchain {
        //sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_17
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}