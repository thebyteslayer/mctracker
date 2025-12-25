plugins {
    kotlin("jvm") version "1.9.24"
}

group = "com.thebyteslayer"
version = "1.0.1"
description = "A plugin that makes compasses point to players when named after them"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "21"
    }

    jar {
        archiveBaseName.set("tracker")
        archiveVersion.set("1.0.1")
        from(sourceSets.main.get().output)

        // Include Kotlin runtime
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
        })

        // Handle duplicates
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
