# androidx-mediarouter-compose

[![Last release](https://img.shields.io/github/v/release/SRGSSR/androidx-mediarouter-compose?label=Release)](https://github.com/SRGSSR/androidx-mediarouter-compose/releases)
[![Android min SDK](https://img.shields.io/badge/Android-21%2B-34A853)](https://github.com/SRGSSR/androidx-mediarouter-compose)
[![Build status](https://img.shields.io/github/actions/workflow/status/SRGSSR/androidx-mediarouter-compose/quality.yml?label=Build)](https://github.com/SRGSSR/androidx-mediarouter-compose/actions/workflows/quality.yml)
[![License](https://img.shields.io/github/license/SRGSSR/androidx-mediarouter-compose?label=License)](https://github.com/SRGSSR/androidx-mediarouter-compose/blob/main/LICENSE)

Simplify media routing in your [Compose][compose] app with this native Material
3 [MediaRouter][androidx-mediarouter] library. Enjoy easy integration, a pure Compose-friendly
approach, and no need for `AppCompatActivity` or `Theme.AppCompat.*`. Focus on what matters:
creating seamless media experiences for your users.

> [!WARNING]
>
> This library is under active development. While some features may be missing or unstable, your
> feedback is valuable.
> Please report any problems you encounter or feature requests you have
> by [opening an issue][new-issue].

## Getting started

This library is available as a [GitHub Package][github-packages]. Follow
GitHub's [documentation][using-github-package] to set up the repository in your project.

### Setting up the repository

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/SRGSSR/androidx-mediarouter-compose")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}
```

### Declaring the dependency

In your module `build.gradle(.kts)` file, add the following:

```kotlin
dependencies {
    implementation("ch.srgssr.androidx.mediarouter:mediarouter-compose:<version>")
}
```

### Display a `MediaRouteButton`

To enable users to connect to remote devices and control media playback, add the `MediaRouteButton`
to your screen:

```kotlin
MediaRouteButton()
```

By default, no routes are displayed because it uses an empty `MediaRouteSelector`. Configure this by
providing your own `MediaRouteSelector`:

```kotlin
MediaRouteButton(
    routeSelector = MediaRouteSelector.Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build(),
)
```

## Release

This library is automatically published and released to GitHub Packages whenever a new tag following
the `x.y.z` pattern is pushed.

## License

See the [license][license] file for more information.

[androidx-mediarouter]: https://developer.android.com/media/routing/mediarouter
[compose]: https://developer.android.com/compose
[github-packages]: https://github.com/orgs/SRGSSR/packages?repo_name=androidx-mediarouter-compose
[license]: https://github.com/SRGSSR/androidx-mediarouter-compose/blob/main/LICENSE
[new-issue]: https://github.com/SRGSSR/androidx-mediarouter-compose/issues/new/choose
[using-github-package]: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package
