plugins {
    id("io.github.patrick.remapper") version "1.+"
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.21-R0.1-SNAPSHOT:remapped-mojang")

    compileOnly(project(":NMS:Wrapper"))
}

tasks.remap {
    version.set("1.21")
}

tasks.build {
    dependsOn(tasks.remap)
}