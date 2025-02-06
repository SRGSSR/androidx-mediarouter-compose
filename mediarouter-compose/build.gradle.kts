plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "ch.srgssr.androidx.mediarouter.compose"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        resValues = false
        shaders = false
    }

    lint {
        disable.add("RestrictedApi")
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.core)
    implementation(libs.androidx.mediarouter)
}

publishing {
    publications {
        register<MavenPublication>("GitHubPackages") {
            group = "ch.srgssr.androidx.mediarouter"
            version = providers.environmentVariable("VERSION_NAME").getOrElse("dev")

            afterEvaluate {
                from(components["release"])
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/SRGSSR/androidx-mediarouter-compose")

            val gitHubUser = providers.gradleProperty("gpr.user")
                .orElse(providers.environmentVariable("USERNAME"))
            val gitHubPassword = providers.gradleProperty("gpr.key")
                .orElse(providers.environmentVariable("GITHUB_TOKEN"))

            credentials {
                if (gitHubUser.isPresent) {
                    username = gitHubUser.get()
                }

                if (gitHubPassword.isPresent) {
                    password = gitHubPassword.get()
                }
            }
        }
    }
}
