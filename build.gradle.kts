/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlinx.kover)
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

    afterEvaluate {
        if (pluginManager.hasPlugin("org.jetbrains.dokka")) {
            dokka {
                moduleName = if (path == ":") "AndroidX MediaRouter Compose" else name
                moduleVersion = providers.environmentVariable("VERSION_NAME").orElse("main")

                pluginsConfiguration.html {
                    customStyleSheets.from(layout.settingsDirectory.file("config/dokka/androidx-mediarouter-compose.css"))
                    footerMessage = "© SRG SSR"
                }

                dokkaSourceSets.findByName("main")?.apply {
                    includes.from(layout.settingsDirectory.file("docs/${this@allprojects.name}.md"))

                    externalDocumentationLinks.register("kotlinx.coroutines") {
                        url("https://kotlinlang.org/api/kotlinx.coroutines")
                        packageListUrl("https://kotlinlang.org/api/kotlinx.coroutines/package-list")
                    }

                    sourceLink {
                        localDirectory.set(layout.projectDirectory.dir("src"))
                        remoteUrl("https://github.com/SRGSSR/androidx-mediarouter-compose/tree/${moduleVersion.get()}/${project.name}/src")
                    }
                }
            }
        }
    }
}

dokka {
    dokkaPublications.html {
        includes.from(layout.settingsDirectory.file("docs/${project.name}.md"))
    }
}

dependencies {
    dokka(project(":mediarouter-compose"))

    kover(project(":mediarouter-compose"))
}
