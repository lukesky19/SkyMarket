plugins {
    java
}

group = "com.github.lukesky19"
version = "3.0.0.0"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }

    maven("https://jitpack.io") {
        name = "Vault Repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.5.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    javadoc {
        source = sourceSets["main"].allJava
        classpath = files() + configurations["compileClasspath"]

        (options as StandardJavadocDocletOptions).apply {
            tags("apiNote:a:API Note:")
            addStringOption("sourcepath", "")
        }
    }

    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        archiveClassifier.set("")
    }

    build {
        dependsOn(javadoc)
    }
}