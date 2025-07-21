plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cc.vastsea"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    
    shadowJar {
        archiveClassifier.set("")
        relocate("kotlin", "cc.vastsea.vanillacmdbridge.libs.kotlin")
    }
    
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}