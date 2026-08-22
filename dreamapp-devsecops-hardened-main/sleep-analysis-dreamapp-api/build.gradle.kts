plugins {
    kotlin("jvm") version "2.1.21"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("org.owasp.dependencycheck") version "10.0.4"
    id("jacoco")
}

application {
    mainClass.set("team.dreamapp.com.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "team.dreamapp.com.MainKt"
    }
}

tasks.shadowJar {
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
    implementation("io.javalin:javalin:6.3.0")

    // =========================
    // Logging
    // =========================
    implementation("org.slf4j:slf4j-api:2.0.13")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.4")

    // =========================
    // JSON Serialization
    // =========================
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.22.2")

    // =========================
    // Firebase & Firestore
    // =========================
    implementation("com.google.firebase:firebase-admin:9.2.0")
    implementation("com.google.cloud:google-cloud-firestore:3.17.1")

    // =========================
    // Kotlin & Coroutines
    // =========================
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // =========================
    // Networking
    // =========================
    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    // =========================
    // Database Connectivity & ORM
    // =========================
    implementation("org.postgresql:postgresql:42.7.13")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("com.github.seratch:kotliquery:1.9.1")
    implementation("de.svenkubiak:jBCrypt:0.4.3")

    // =========================
    // Testing
    // =========================
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.3")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                // Ratchet pattern: current coverage is ~12%. This gate prevents regression.
                // Raise this value incrementally as test coverage improves (target: 0.60).
                minimum = "0.10".toBigDecimal()
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(rootProject.file("detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt> {
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
    }
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
}

kotlin {
    jvmToolchain(17)
}
