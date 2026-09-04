plugins {
    kotlin("jvm") version "2.2.20"

    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("com.google.devtools.ksp") version "2.3.5"
}

group = "com.chaosnote"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    maven("https://jitpack.io")
    google()
    mavenCentral()
}

dependencies {
    implementation("com.github.chaosnoteapp:chaosnote-api:1.0.0")
    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.components.uiToolingPreview)
    implementation(compose.components.uiToolingPreview)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.0")

    // PF4J runtime dependency so this plugin can be discovered by the host
    implementation("org.pf4j:pf4j:3.15.0")

    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "com.chaosnote.plugin.MainKt"

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "Chaosnote"
            packageVersion = "1.0.0"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

val copyPluginJar by tasks.registering(Copy::class) {
    dependsOn("jar") // спершу збираємо JAR
    val pluginOutputDir = file("C:\\Users\\Anastasiia\\AppData\\Roaming\\Chaosnote\\plugins")
    from(tasks.named("jar")) {
        // джарка, яку згенерує task 'jar'
    }
    into(pluginOutputDir)
    rename { "${project.name}-${project.version}.jar" } // можна змінити ім'я
}

// Зробимо так, щоб copyPluginJar запускалось після build
tasks.named("build") {
    finalizedBy(copyPluginJar)
}