plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.13.1"  // note: different plugin ID in v2
}

group = "com.monikode"
version = "1.1.0"

java {
    toolchain {
        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(17)) // match IDE's JVM
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2023.2")
        bundledPlugin("com.intellij.java")
        pluginVerifier()
        zipSigner()
    }
}

sourceSets {
    named("main") {
        java {
            srcDir("src")
        }
        resources {
            srcDir("resources")
        }
    }
}

tasks {
    publishPlugin {
        token.set(providers.gradleProperty("intellijPlatformPublishingToken"))
    }
}