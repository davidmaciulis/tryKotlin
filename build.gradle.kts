import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.21"
    application
}

group = "me.davidmaciulis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral();
}

val exposedVersion: String by project
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1");
    implementation("junit:junit:4.12");
    implementation(files("lib/exposed-core-0.32.1.jar"))
    implementation(files("lib/exposed-dao-0.32.1.jar"))
    implementation(files("lib/exposed-jdbc-0.32.1.jar"))
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("org.slf4j:slf4j-api:1.7.20")
    implementation("org.slf4j:slf4j-simple:1.7.20")
    implementation("org.jetbrains.exposed:exposed-java-time:0.30.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("com.github.holgerbrandl:krangl:0.17")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}