rootProject.name = "sla-ping-monitor"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

org.gradle.parallel=true
org.gradle.workers.max=8
