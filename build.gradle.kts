plugins {
    kotlin("jvm") version "1.9.24"
}

group = "com.thebyteslayer"
version = "1.0.0"
description = "A plugin that makes compasses point to players when named after them"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    jar {
        archiveBaseName.set("tracker")
        archiveVersion.set("1.0.0")
        from(sourceSets.main.get().output)
    }
}
