plugins {
    id("fabric-loom") version "1.7.4"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://maven.nucleoid.xyz/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.luckperms.net/snapshots/")
    maven("https://repo.luckperms.net/releases/")
}

fun DependencyHandlerScope.includeMod(dep: String) {
    include(modImplementation(dep)!!)
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    includeMod("eu.pb4:sgui:${project.property("sgui_version")}")

    // Caffeine para caché inteligente (alta concurrencia y expiración automática)
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    include("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // LuckPerms API (compileOnly — viene del servidor en runtime)
    compileOnly("net.luckperms:api:${project.property("luckperms_version")}")
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) {
        expand(props)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}
