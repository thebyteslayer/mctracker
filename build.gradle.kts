plugins {
    kotlin("jvm") version "1.9.24"
}

group = "com.thebyteslayer"
version = "1.2.0"
val minecraftVersion = "1.21.4"
val minecraftVersionDisplay = "1.21.x"
description = "A plugin to track players via compasses named after them."

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
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "21"
    }

    jar {
        archiveBaseName.set("tracker-paper")
        archiveVersion.set("${version}+mc${minecraftVersionDisplay}")
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
