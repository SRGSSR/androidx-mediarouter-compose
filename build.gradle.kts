import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

allprojects {
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        basePath = projectDir.absolutePath
        buildUponDefaultConfig = true
        config.setFrom(rootDir.resolve("config/detekt.yml"))
        ignoredBuildTypes = listOf("release")
        parallel = true
    }
}
