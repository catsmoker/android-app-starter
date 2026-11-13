# Android App Starter

A separate, neutral Android app extracted from CatSmoker's **Play Store implementation and selected shared code**. The original project is untouched. This is one app with ordinary `debug` and `release` builds: no product flavors, privileged tools, or hidden GitHub-only implementation.

## Start here

1. Open **this folder** in Android Studio (not the original CatSmoker folder).
2. Use JDK 17 or newer and install Android SDK Platform 37 through SDK Manager. The Gradle wrapper and dependency versions are retained from the source project; no upgrades are required.
3. Let Gradle sync, select the `app` configuration and run it on Android 8.1+ (API 27+).

**No service account, advertising account or Firebase file is needed to launch the debug app.** It uses Google's official test AdMob identifiers. Analytics dependencies are always included, but debug builds never initialize Firebase—even if you supply a real Firebase file or enable the analytics preference. Home shows setup/consent diagnostics. Without a configured UMP message, consent updating may fail: ads remain off rather than bypassing consent.

The placeholder application ID is **`com.example.androidstarter`**. Do not publish under it. It is independent of CatSmoker and installs separately.

## Important: not cleared for publication

- The original source is **CC BY-NC-SA 4.0**, not a permissive commercial starter license. Original attribution remains in `LICENSE`, `NOTICE` and About/Licenses. Obtain appropriate rights from its owner before commercial reuse; changing the name does not change the license.
- Nothing's **proprietary SDK EULA restricts redistribution and requires prior written permission for commercial use**. See `notices/NOTHING-SDK-LICENSE.md` and contact `GDKsupport@nothing.tech`. The local SDK was preserved to keep functionality, not to assert redistribution rights. Confirm rights before sharing the starter or shipping an ad-supported app.
- The retained Ndot fonts need font redistribution/commercial-use clearance too. Replace them with appropriately licensed fonts if you cannot establish permission.
- Privacy and terms screens deliberately say **SAMPLE**. Replace them and publish your own working privacy webpage. This project does not certify Play policy compliance.

## What you get

The actual reusable Compose screens/components, not simplified lookalikes: responsive theme/language onboarding, four appearance modes, typography and animated startup, multilingual settings, accessible controls, information sheets, recurring support dialog, honest rating links, About/read-more/version copy, legal screens, Play ad-removal billing, consent-gated adaptive AdMob banners, opt-in Firebase Analytics, app-only live logs/crash export and APK sharing. General-purpose Nothing Glyph strip/Matrix progress controls have their own destination and release device services on leaving the screen. Unsupported phones get an explanation instead of SDK calls.

See [the feature inventory](docs/FEATURES.md), [service configuration](docs/SERVICES.md), and [verification record](docs/VERIFICATION.md).

Also see [the four-template comparison and implemented improvements](docs/TEMPLATE_COMPARISON.md) and [the small architecture guide](docs/ARCHITECTURE.md). This remains one Android app, not a multi-module/KMP rewrite.

## Customize your app

Run from this folder (Python 3):

```powershell
python tools/configure.py --name "My App" --application-id com.yourcompany.myapp --dry-run
python tools/configure.py --name "My App" --application-id com.yourcompany.myapp
```

This validates the identity and updates `starter.properties`. The visible app name, onboarding title, About title, launcher label, Play rating links and provider authorities follow it. **The internal Kotlin namespace stays `com.example.androidstarter` deliberately**: a store ID does not require moving every source file. Decide the final application ID before creating Firebase/AdMob apps, billing products or a Play listing; changing it later creates a different app and does not migrate purchases/preferences.

The first command is a preview and changes nothing. Invalid identities or malformed/mismatched Firebase files do not overwrite your identity. `python tools/doctor.py` gives a readable setup checklist; `python tools/doctor.py --release` intentionally fails while publication placeholders remain. This checks local configuration, not legal rights or live provider accounts.

In the table below, `res/` means `app/src/main/res/`; Kotlin paths such as `ui/theme/` are under `app/src/main/java/com/example/androidstarter/`.

| Change | Edit |
|---|---|
| Developer name, contact, website, source, support and public legal links | `app/src/main/res/values/brand.xml` (placeholder links show a setup message until replaced) |
| Privacy and terms document bodies/dates | `res/values/strings.xml`, `legal_*`; add reviewed translations and bump `application/privacy/PrivacyPolicy.VERSION` when renewed analytics consent is necessary |
| Launcher and About logo | `res/drawable/ic_launcher_foreground.xml`, `res/mipmap-anydpi-v26/ic_launcher.xml`; Android Studio Image Asset can replace these |
| Colors, shapes, typography, reusable controls | `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `ui/components/` |
| Custom fonts | `res/font/`, `ui/theme/Type.kt`; retain required licenses for replacement fonts |
| App version | `app/build.gradle.kts`: `versionCode`, `versionName` |
| Play listing/rating link | Automatically derived from `applicationId` in `application/distribution/DistributionSupportAction.kt` |
| AdMob/Firebase/Nothing SDK/billing | [docs/SERVICES.md](docs/SERVICES.md) |

Existing generic translations are retained for Arabic, British English, Spanish, French, Japanese, Brazilian Portuguese and Simplified Chinese, plus default English. Language selection and RTL handling remain intact. **New neutral/device/sample-legal text falls back to English until translated**; old app-specific translations were removed rather than left misleading. See `tools/resource-inventory.json`. Resource names are stable; translate their values, not their keys.

## Small verification commands

```powershell
# Set these only for your own machine; do not commit local.properties.
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
python tools/verify.py           # Offline source/configuration checks; no Android build
python tools/verify.py --android # Also compile both build types + five selected test classes
# For a narrowly affected capability instead of the combined command:
.\gradlew.bat :app:testDebugUnitTest --tests '*GlyphHardwareTest'
# Only when an installable APK is needed:
.\gradlew.bat :app:assembleDebug
```

No release signing keys are provided. Use Android Studio's **Generate Signed Bundle / APK** with your own new key and protect it separately. Do not upload a debug APK, sample legal text, test-ad release, or this starter's placeholder identity.

In Android Studio, open `screens/settings/SettingsScreen.kt` and show the `SettingsPreview` Design/Split panel for small-phone, dark-phone, tablet, Arabic RTL and large-text configurations. They do not create service clients. Compiling these previews is not a screenshot/accessibility test; inspect them visually.

Local device smoke flows are in `tools/smoke*.yaml`. They target the documented placeholder identity and English UI; update their app ID/name after customization. Run only on a device you authorize, with Maestro's analytics/update checks disabled as described in the verification record. They never purchase anything or click ads. The language flow expects onboarding completed and Dark selected, switches to Arabic to check persistence, and restores English. The unsupported-device Glyph assertion is intentionally for non-Nothing phones; do not use it to validate supported hardware.

## Git and privacy

This starter has independent Git history; the original project's history was not copied. Machine files, Firebase config, service properties, keys and build outputs are ignored. Review staged changes before every commit; `AGENTS.md` is intentionally local-only. The source extraction inventory records file paths/hashes, not private configuration. Do not add credentials to it. Public availability does not change the source license or grant downstream users additional rights to bundled third-party SDKs/fonts; review the notices and obtain any permissions your use requires.
