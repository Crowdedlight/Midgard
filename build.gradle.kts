import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "tech.grimm"
description = "Discord bot"
version = "0.1"

plugins {
    application
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

application {
    mainClass.set("tech.grimm.midgard.MidgardKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("me.jakejmattson:DiscordKt:0.23.4")
    implementation("com.google.apis:google-api-services-youtube:v3-rev20220719-2.0.0")

    implementation("com.ibasco.agql", "agql-source-query", "1.0.7")

    implementation("org.jetbrains.exposed", "exposed-core", "0.39.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.39.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.39.1")
    implementation("org.jetbrains.exposed", "exposed-java-time", "0.39.1")
    implementation("org.xerial", "sqlite-jdbc", "3.30.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    dependsOn("writeProperties")
}

tasks.register<WriteProperties>("writeProperties") {
    dependsOn("processResources")

    property("name", project.name)
    property("description", project.description.toString())
    property("version", version.toString())
    property("url", "https://github.com/nullx27/midgard")
    setOutputFile("src/main/resources/bot.properties")
}

tasks.withType<Jar> {
    dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources"))

    archiveFileName.set("midgard.jar")

    manifest {
        attributes["Main-Class"] = application.mainClass
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) } +
            sourceSets.main.get().output)
}