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