plugins {
    `java-library`
    `maven-publish`
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api(libs.org.ow2.asm.asm)
    api(libs.org.ow2.asm.asm.tree)
    api(libs.org.apache.poi.poi.ooxml)
    api(libs.org.openjfx.javafx.controls)
    api(libs.org.openjfx.javafx.fxml)
    api(libs.org.controlsfx.controlsfx)
}

group = "org.eimerarchive"
version = "1.0-SNAPSHOT"
description = "MCVersionChecker"
java.sourceCompatibility = JavaVersion.VERSION_25

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}