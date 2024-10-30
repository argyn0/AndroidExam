// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
// build.gradle.kts (Project-level)
buildscript {
    repositories {
        google()
        mavenCentral()  // Уже добавленный центральный репозиторий Maven
        maven { url = uri("maven.google.com") }  // Добавление кастомного Maven репозитория
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        // Другие classpath зависимости
    }
}



