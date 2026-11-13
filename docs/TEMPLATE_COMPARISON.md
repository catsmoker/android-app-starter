# Improvements informed by four starters

Reviewed 2026-11-14 against the pinned revisions below. We compared READMEs, build layouts and the concrete files listed—not just repository descriptions. General patterns were independently implemented; no reference code, artwork or provider configuration was copied.

| Reference | Useful evidence | Applied here | Not imported |
|---|---|---|---|
| [Drjacky/MVVMTemplate](https://github.com/Drjacky/MVVMTemplate/tree/e169f3782b32fd50b2711333d1c2b919bc1a2d1e) | Constructor-injected state owners, fake-driven coroutine tests | Screen-owned `SettingsEnvironment`, in-memory fake, main-dispatcher rule; initial/loading/success/failure/cancellation tests | Product/network/Paging demo, RxJava alongside coroutines, domain boilerplate and many modules |
| [blocoio/android-template](https://github.com/blocoio/android-template/tree/92c498854aae02a809d70666f7c7d0f254022c53) | Explicit renaming/setup, coroutine tests, quality workflows | Tested identity setup, safe preview/atomic update, one bounded local verification entry point | Its older JDK/action/dependency versions, Ktor demo, separate Data/Domain modules and CI uploads |
| [android/nowinandroid](https://github.com/android/nowinandroid/tree/2982e63cb691ad49cd009aab43dc206fee6699c2) | Fakes rather than extensive mocks, multipreview design review, documented state flow and named test tasks | Small platform boundary, SDK-free Settings multipreviews, ownership/test guide and bounded Gradle filters | News/database/sync infrastructure, demo/prod flavors, benchmark/catalog modules and large convention setup |
| [DevAtrii/Kmp-Starter-Template](https://github.com/DevAtrii/Kmp-Starter-Template/tree/9e98a87c31d4efa1b46bf448839666b430e0dd5d) | Configurable identity, temporary-filesystem generation tests, multi-device previews, service configuration isolation | Disposable configuration tests, offline setup doctor, explicit publication-placeholder warnings | KMP/iOS, Koin migration, RevenueCat/Mixpanel/remote-config infrastructure and removable Google integrations. Its custom license also argues against copying implementation. |

Bloco's reviewed default branch last changed in 2023; the other reviewed heads are from 2026. Useful ideas do not justify transplanting older executable setup or upgrading this project's retained toolchain.

## Implemented changes

- **Testable Settings:** Android persistence, notifications and sharing are behind one Hilt boundary. The actual screen, preference stores and mandatory service controls remain intact. No global repository/domain framework was introduced.
- **Safer sharing:** unexpected exceptions cannot leave the spinner stuck; cancellation propagates and cleanup runs in `finally`; repeated taps cannot launch parallel shares. Messages are localized by the screen when displayed, not cached with an old context.
- **Reliable language state:** normalized persisted tags are reflected before a restart event is queued for a temporarily absent screen collector. This is transient, single-consumer delivery, **not** process-death persistence; preferences remain the durable truth.
- **Translation consistency fixed:** shipped French was absent from the picker/Android locale list. Advertised British English was collapsed to English. Both now have consistent selection/normalization, with a regression test comparing the picker against Android's locale configuration.
- **Bounded design review:** five Settings previews cover small phone, dark phone, tablet, Arabic RTL and 200% fonts without constructing Hilt/billing/Firebase clients. These are inspection aids, **not passed screenshot tests**. Reusable section/card titles now have accessibility heading semantics.
- **Safer setup:** identity dry-run, atomic replacement and useful errors for malformed/mismatched Firebase config, without exposing config values. Tests operate only on disposable projects.
- **Readable diagnostics:** `doctor.py` explains missing identity, service, branding and legal setup offline; `--release` fails on detected placeholders. It cannot verify account consoles, legal adequacy, licensing, signing or Play compliance.
- **Repeatable checks:** `verify.py` defaults to offline source/config tests. `--android` explicitly adds one debug/release compile and five selected unit-test classes. It does not hide a full suite, clean, assembly, device action, upload or publication.

## Why retain one module and synchronous preferences?

The app reads its saved locale during Application/Activity `attachBaseContext`, before Hilt/ViewModels exist. Wholesale asynchronous DataStore migration needs a startup/migration design and risks locale/theme flashes. Keep the existing stores; a future data-heavy feature can justify another store.

The finished screens do not need a backend demo or a multi-module Clean Architecture scaffold. Add a boundary where coupling warrants it, as Settings demonstrates—not an interface/use case/module per method. Built-in Google integrations and all privacy safeguards remain mandatory. No architecture change clears the source, Nothing SDK or font-rights blockers.

## Concrete source inspected

Along with each README and build/file layout:

- Drjacky: `feature/products/src/test/kotlin/app/web/drjackycv/feature/products/productlist/ProductsListViewModelTest.kt`.
- Bloco: `template/androidApp/src/test/java/io/bloco/template/ViewModelAndCoroutinesExampleUnitTest.kt`, `.github/workflows/lint.yml`.
- Now in Android: testing documentation; `core/ui/src/main/kotlin/com/google/samples/apps/nowinandroid/core/ui/DevicePreviews.kt`, `core/testing/src/main/kotlin/com/google/samples/apps/nowinandroid/core/testing/util/MainDispatcherRule.kt`.
- KMP Starter: `starter/ui/utils/src/commonMain/kotlin/com/kmpstarter/ui_utils/compose/AllDevicePreview.kt`, `generator/data/src/jvmTest/kotlin/com/kmpstarter/generator_data/GenerateProjectTest.kt`.

Implementation syntax was checked against current official [coroutines test docs](https://github.com/Kotlin/kotlinx.coroutines/blob/1.11.0/kotlinx-coroutines-test/README.md), [Compose previews](https://developer.android.com/develop/ui/compose/tooling/previews) and [heading semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics). See [verification evidence](VERIFICATION.md) and [the architecture guide](ARCHITECTURE.md).
