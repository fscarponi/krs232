import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "com.github.fscarponi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val jsscVersion : String by project
    testImplementation(kotlin("test"))
    implementation("org.slf4j:slf4j-api:1.7.5")
    implementation("org.slf4j:slf4j-log4j12:1.7.5")
    implementation("io.github.java-native:jssc:$jsscVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
repositories {
    mavenCentral()
}
