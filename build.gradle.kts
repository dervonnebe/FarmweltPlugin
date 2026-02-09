plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "top.jaxlabs"
version = "1.4.4"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.mvplugins.multiverse.core:multiverse-core:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "top.jaxlabs.farmweltplugin.utils.bstats")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("project" to mapOf("version" to project.version))
        }
    }
}
