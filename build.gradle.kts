plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.13.1"  // note: different plugin ID in v2
}

group = "com.monikode"
version = "2.1.1"

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

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
    testImplementation("org.assertj:assertj-core:3.24.1")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
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
    named("test") {
        java {
            srcDir("tests")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    publishPlugin {
        token.set(providers.gradleProperty("intellijPlatformPublishingToken"))
    }
}