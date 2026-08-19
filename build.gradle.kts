plugins {
    kotlin("jvm") version "2.1.21"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass.set("team.dreamapp.com.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "team.dreamapp.com.MainKt"
    }
}

// Configure Shadow JAR (fat JAR)
tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "team.dreamapp.com.MainKt"
    }
}

group = "team.dreamapp.com"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // =========================
    // Web Framework
    // =========================
    implementation("io.javalin:javalin:6.3.0") // Lightweight web framework

    // =========================
    // Logging
    // =========================
    implementation("org.slf4j:slf4j-api:2.0.13") // Logging API
    implementation("org.slf4j:slf4j-simple:2.0.16") // Simple SLF4J backend (optional if using logback)
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14") // Logback backend for SLF4J

    // =========================
    // JSON Serialization
    // =========================
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1") // Core JSON support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1") // Kotlin module for Jackson
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1") // Java time support

    // =========================
    // Firebase & Firestore
    // =========================
    implementation("com.google.firebase:firebase-admin:9.2.0") // Firebase Admin SDK (Firestore, Auth, etc.)
    implementation("com.google.cloud:google-cloud-firestore:3.17.1") // Optional direct access to Firestore

    // =========================
    // Kotlin & Coroutines
    // =========================
    implementation(kotlin("stdlib")) // Kotlin standard library
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0") // Coroutines core
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0") // Optional for Google Play Services

    // =========================
    // Networking
    // =========================
    implementation("com.squareup.okhttp3:okhttp:5.1.0") // HTTP client for REST or other web APIs

    // =========================
    // Database Connectivity & ORM
    // =========================
    implementation("org.postgresql:postgresql:42.7.13") // PostgreSQL JDBC driver
    implementation("com.zaxxer:HikariCP:6.3.0") // High-performance JDBC connection pool
    implementation("com.github.seratch:kotliquery:1.9.1") // Lightweight SQL and JDBC wrapper for Kotlin
    implementation("de.svenkubiak:jBCrypt:0.4.3") // Library for BCrypt password hashing
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
