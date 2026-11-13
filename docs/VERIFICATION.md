# Verification record

This is bounded engineering evidence, **not** release approval, license clearance, a security audit or a Google Play compliance certificate.

## Template-comparison improvements — 2026-11-14

The source-backed comparison and scope decisions are in [TEMPLATE_COMPARISON.md](TEMPLATE_COMPARISON.md). The original sibling was not modified. No dependency upgrades, module/flavor migrations, commits, uploads, new accounts or device actions were performed for this improvement pass.

| Check | Observed result |
|---|---|
| `python tools/verify.py --android` | Passed: offline checks plus one Gradle invocation compiling debug/release Kotlin and running the five selected test classes below |
| Current offline structural guard | **1,370 checks passed**, `tools/check-report.json` |
| Identity/configuration Python tests | **6 passed**: valid/invalid identity, unchanged-on-failure, matching/mismatched/malformed Firebase config, no config dump, dry-run and temporary-file cleanup |
| Doctor Python tests | **8 passed**: development notes, release blockers, Firebase identity/format checks, placeholder/test-ad warnings, malformed XML and duplicate properties |
| Verification-plan Python tests | **2 passed**: default Python-only plan; Android plan limited to one compile/selected-test invocation without clean/assemble/device/publish |
| Selected Kotlin tests | **27 passed, zero failures/errors**, confirmed from five JUnit XML reports: SettingsViewModelTest **13**, LanguageConfigurationTest **6**, OnboardingStateTest **5**, PrivacyPolicyTest **2**, AdConsentPolicyTest **1** |
| `python tools/doctor.py --release` | Expected exit **1**: placeholder ID, absent own service configuration/key, placeholder developer/public links and sample legal content remain intentionally unresolved |

The Settings tests cover saved initial state, persisted theme/analytics choices, OS notification refresh, normalized language/restart delivery, no-op language changes, sharing busy/re-entry protection, success/split/failure/exception/retry and cancellation cleanup. Language tests include French/British English normalization and picker/Android locale-list equality. These are fake-driven state/policy tests, **not real preference-disk, SDK, APK-share-target or process-restart tests**.

An initial compile found a public-boundary/internal-outcome visibility mismatch; that was fixed before the successful combined check. Existing adaptive-banner, task-description, icon and Gradle deprecation warnings remain. Python unittest progress redirected through Windows PowerShell appears as `NativeCommandError` text in the local combined log because unittest writes stderr; all unittest results are `OK` and the combined command/Gradle completed successfully.

**Not repeated in this pass:** APK assembly/install, artifact/dependency checks, device smoke, live provider tests, preview rendering/screenshot comparison, TalkBack or supported Nothing hardware. The five preview configurations compile; visual layout and accessibility behavior still require inspection. The APK/artifact report and device observations below belong to the earlier extraction build and do not validate this newer source. Latest Gradle XML results now describe these 27 selected tests, replacing the earlier result files.

## Initial extraction evidence — 2026-11-13

### Project isolation

- Created a separate sibling `android-app-starter`, never over the original project. All 71 extraction-inventory source file hashes still matched the original at the final comparison; no original-source edits were made.
- Independent Git repository initialized on `main`; no source history copied, commits, remotes, push or publication.
- No signing keys, passwords, service-account files, production Firebase configuration, production advertising IDs or machine configuration copied. Ignore rules were checked for service/Google configuration, machine files, build outputs and local AI guidance.
- Source brand mentions remain only where required for attribution/provenance, not the application identity, account configuration, links or packages.

### Passed build and targeted checks

| Check | Observed result |
|---|---|
| Debug Kotlin and ordinary release Kotlin compilation | Passed on the retained toolchain, Java target 17, Android SDK 37 |
| Installable debug APK | Built and installed successfully as `com.example.androidstarter`, independently of the original app |
| Debug/release manifest processing | Passed; no original/full-only components, privileged permissions, Firebase auto-initializer or unneeded foreground service |
| Resolved release runtime dependencies | Required AdMob, UMP, Firebase Analytics and Billing present; Start.io, libsu, Shizuku and Xposed absent |
| Targeted JUnit selection | **53 tests in 11 classes; zero failures/errors**. Subsequent edited parser fixture rechecked separately: **4 tests; zero failures/errors** |
| Identity setup script | **3 Python tests passed**, covering valid updates, invalid names/IDs without writes, and mismatched Firebase configuration without writes |
| Offline structural/resource guard | **1,361 checks passed** in `tools/check-report.json`; includes route closure/uniqueness, package layout, strings/translations, required integrations, removed APIs and development privacy safeguards |
| Built evidence guard | `tools/artifact-report.json`; **30 checks passed**, including resolved dependencies, both merged manifests, debug analytics deactivation, test AdMob app ID, excluded APK class descriptors and packaged SDK EULA |

Targeted JUnit classes: `PrivacyPolicyTest`, `SupportLinkTest`, `OnboardingStateTest`, `GlyphHardwareTest`, `GlyphDrainProgressTest`, `SquigglyMathTest`, `LogcatTailBufferTest`, `LogcatEntryParserTest`, `RemoveAdsPolicyTest`, `BillingAcknowledgementPolicyTest`, `AdConsentPolicyTest`. This was a selected set, not an exhaustive suite. Later single-class execution replaces Gradle's latest XML results; the 53-test result refers to the earlier successful selected run.

Artifact checks inspect the merged manifests and debug APK, not just source files. The report records the APK SHA-256. SDK-added permissions are explicitly enumerated there: network, optional notifications, Nothing integration, advertising identifiers/AdServices, Billing, install referrer, WorkManager wake lock and a signature-only AndroidX receiver permission. No broad package visibility, overlay, usage access, storage, microphone, install-package, VPN or privileged-settings permission remains.

### Device observations

Device: connected Poco/Xiaomi `M2007J20CG`, Android 15/API 35; **not a Nothing phone**. Only the new starter package was installed/tested. No accounts, ad clicks, purchases, payment flows or external production links were exercised.

- First-run appearance/agreement onboarding and required terms acceptance reached Home. Skipping optional notifications did not block completion.
- Home, unsupported Glyph/Matrix explanation, Settings, theme chooser, Dark selection, support destination, ad-removal card, About and license destination passed local assertions.
- Dark preference remained after stopping/relaunching the app.
- Reopened optional permissions returned to Settings correctly; Privacy and Terms opened from About, displayed their sample-content warning, and returned to Home. A stale-start-destination edge case was fixed so revisiting permissions after initial onboarding uses the current back stack.
- Home's development service safeguard was visible. No publisher Firebase configuration was installed. This is not evidence of successful live ads or live analytics.
- Language changed to Arabic, Settings labels and persisted Dark choice were verified, and the RTL screenshot was visually inspected (right-aligned text/icons and mirrored navigation). Arabic survived app stop/relaunch. The recurring support dialog appeared and was dismissed in Arabic. English was restored afterward.
- Local flows are checked by Maestro 2.12.1. Its CLI analytics/update checks/analysis notification were disabled with `MAESTRO_CLI_NO_ANALYTICS=true`, `MAESTRO_DISABLE_UPDATE_CHECK=true` and `MAESTRO_CLI_ANALYSIS_NOTIFICATION_DISABLED=true`; no cloud test or upload used.

The first automation attempt saw an asleep display; a later attempt used a nonmatching `Theme` selector. Waking the device and matching the actual `Theme: …` label resolved these automation failures. Early build integration errors were fixed before the successful results above. No current build/test failure is being hidden by those earlier attempts.

Device artifacts/raw logs remain local in the approved temporary directory, not in the reusable repository. Initial Maestro relaunches used its default permission behavior; the new app's notification permission was subsequently returned to denied, and saved flows explicitly leave it unset. No original-app permissions/preferences were changed. The installed development app is left for inspection with Dark/English preferences.

### Unverified / requires your setup

- **Physical Glyph/Matrix operation**, supported model/firmware variations, approved SDK key, binding/error behavior and release shrinking on real Nothing hardware. Unit model/drain tests plus safe unsupported-device behavior do not establish hardware compatibility.
- Real AdMob UMP messages/region withdrawal and ad delivery, new Firebase configured-release delivery/opt-out, and Play purchase/cancel/pending/restore/revocation/acknowledgement on a signed internal testing track. The required integrations are present; account-backed end-to-end behavior is not claimed.
- Release R8 shrinking/resource shrinking, signed APK/AAB, Play upload/review, minimum-API device, tablets, TalkBack, other locales and notification allow/deny prompt behavior beyond the skippable step.
- App-log export/APK-share destinations and external configured links were retained/compiled but not clicked end to end in this smoke test. Samples intentionally cannot establish your real legal/developer links.
- Complete transitive-license/security review and **source, Nothing SDK redistribution/commercial-use, and font rights**. The SDK EULA and source license are material publication blockers until you establish permission.

Known non-fatal build warnings: retained adaptive-banner sizing API and older Android task-description/icon API usage; Gradle reports deprecated features ahead of Gradle 10. No dependency upgrades or clean builds were used to mask warnings.

## Reproducing bounded checks

For the current offline/selected-test entry point use `python tools/verify.py` or, explicitly, `python tools/verify.py --android`. Do not repeat its Gradle tasks immediately afterward. For a narrower changed capability, use the individual named test filter instead. `python tools/doctor.py --release` intentionally fails for the unconfigured starter.

```powershell
python tools/check_starter.py
python tools/test_configure.py
.\gradlew.bat :app:compileDebugKotlin :app:compileReleaseKotlin
# Only when refreshing dependency/manifest evidence:
.\gradlew.bat :app:processDebugManifest :app:processReleaseManifest :app:dependencies --configuration releaseRuntimeClasspath --console=plain 2>&1 | Out-File -Encoding utf8 dependency-report.log
python tools/check_artifacts.py --dependency-log dependency-report.log
```

The last command also needs an existing debug APK. Assemble once when needed; it does not build automatically. Keep your local dependency/device logs out of Git. The saved artifact report describes the actual tested APK, not a future personalized build.
