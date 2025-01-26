plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"
val ktorVersion = "3.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.5")
    implementation("org.seleniumhq.selenium:selenium-java:4.27.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.google.firebase:firebase-admin:9.4.2")
    testImplementation(kotlin("test"))

}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

tasks.shadowJar {
    mergeServiceFiles()
}


tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}