plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"
val ktorVersion = "3.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.seleniumhq.selenium:selenium-java:4.25.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}