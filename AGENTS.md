# AGENTS.md

Android home-screen App Widget that periodically shows a random Quran ayah. Single Gradle module (`:app`), Kotlin, no ViewModel/DI frameworks.

## Commands

```bash
./gradlew assembleDebug        # build APK
./gradlew installDebug         # build + install to connected device/emulator
./gradlew lint                 # Android Lint
./gradlew test                 # JVM unit tests (see caveat below)
./gradlew connectedAndroidTest # instrumented tests, needs a device/emulator
./gradlew testDebugUnitTest --tests org.jihedamine.ayahwidget.SomeTest  # single test
```

## Toolchain quirks (easy to get wrong)

- **AGP 9.0.0 + built-in Kotlin.** `app/build.gradle.kts` applies only `com.android.application`. Kotlin compiles via `android.builtInKotlin=true` in `gradle.properties`. Do **not** add the `kotlin-android` plugin — the `libs.plugins.kotlin.android` alias exists in `libs.versions.toml` but is intentionally unused.
- Java 17 source/target; `compileSdk = 36`, `minSdk = 21`. Keep new APIs guarded for API 21.
- All deps go through the version catalog `gradle/libs.versions.toml` (not inline versions).

## Testing reality

- **No tests exist yet** (`app/src/test` and `app/src/androidTest` are absent) even though JUnit/Espresso deps are declared. `./gradlew test` passes with nothing to run. Create the source dirs when adding tests.

## Architecture (what's not obvious from filenames)

Package: `org.jihedamine.ayahwidget`. Widget update flow:

1. `AyahWidgetProvider` (`AppWidgetProvider`) — entry point; `onUpdate` and the custom `ACTION_AUTO_UPDATE` broadcast both funnel into `AyahWidgetService.updateWidgetAyah`.
2. `WidgetNotification` — schedules the repeating refresh via `AlarmManager.setInexactRepeating` (default 30 min). Alarm is cancelled in `onDeleted`/`onDisabled`.
3. `AyahWidgetService` (Kotlin `object`) — picks a random ayah, writes it to prefs, renders `RemoteViews`.
4. `AyahRepository` — loads `app/src/main/assets/ayahs.json` (array of `{ayahContent, ayahName, ayahNumbers}`).

Activities: `WidgetConfigActivity` (launched on widget add/reconfigure), `AyahListActivity`.

## Conventions

- **Per-widget prefs:** all settings live in SharedPreferences named `"WidgetPrefs"`, keyed `"widget_<setting>_$appWidgetId"` (e.g. `widget_text_size_$appWidgetId`) to support multiple widget instances. Defaults live in `ConfigDefaults`.
- Ayah text is stored/round-tripped as a JSON string in prefs; UI sizing uses `SpannableString` + `AbsoluteSizeSpan` in `AyahWidgetService.getAyahSpannableString`.
- Widget background/text color resolve Material 3 dynamic attrs (`colorSecondaryContainer` / `colorOnSecondaryContainer`) at runtime, falling back to resources.
- Arabic/RTL is first-class: `supportsRtl=true`, `res/xml/locale_config.xml`, and `values-ar` / `values-fr` string resources. Keep UI strings localized.

## More detail

`.github/copilot-instructions.md` has a fuller architecture/workflow writeup (adding config options, editing appearance, updating `ayahs.json`). Reconcile any changes here with that file.
