# Small app, explicit ownership

One application module and main source set, ordinary debug/release types. Internal Kotlin namespace: `com.example.androidstarter`. Public app identity: `starter.properties`.

| Owner | Responsibility |
|---|---|
| `system/` | Application/activity entry points, startup, locale wrapping, theme and lifecycle |
| `application/` | Navigation, appearance preferences, consent, startup, general links and DI |
| `screens/<destination>/` | Screen/route, ViewModel/state and private adapters, e.g. Settings/Glyph |
| `ui/` | Reusable presentation, typography, theme and SDK-free preview annotation |
| `integrations/ads/` | Required AdMob/UMP, Play purchase/acknowledgement policies/repository |
| `logging/` | Local own-app logs/crash/export; not automatic report upload |

No blanket Data/Domain/Presentation scaffold or placeholder network/database layer. Add a module only for an actual ownership, isolation or measured build-cost need.

## Settings as a testable vertical slice

```text
SettingsRoute (Hilt + lifecycle + localized messages + service controls)
    -> SettingsScreen (plain values/callbacks; state down, actions up)
    -> SettingsViewModel (StateFlow, transient events, share re-entry/cleanup)
    -> SettingsEnvironment (screen-owned Android boundary)
    -> AndroidSettingsEnvironment
       -> AppearanceStore / AnalyticsConsent / OS notifications / AppApkSharer
```

Hilt binds the Android implementation. Tests construct the ViewModel directly with an in-memory fake, not an Activity, SDK client or Android mocks. `MainDispatcherRule` replaces/resets Main with a queued test dispatcher; `runTest` advances virtual time without sleeps. Production dispatchers are unchanged; APK copying already uses IO in its adapter.

Preferences are durable. Event/message channels are buffered **single-consumer**, transient delivery—not persisted jobs or exactly-once across process death. The route collects once. Messages are localized when displayed instead of storing old-locale strings in the ViewModel. Synchronous locale reads before Hilt remain; language writes commit before deliberate process restart.

## Add a screen without rebuilding the architecture

1. Use `screens/yourdestination/`, matching package/directory. Reuse the scaffold, cards, spacing and theme roles.
2. Register a route/destination in `application/navigation/AppNavHost.kt`. The offline guard checks references/destinations.
3. Add a screen ViewModel only for real stateful/asynchronous behavior; expose immutable state and collect with lifecycle awareness. Static screens do not need repositories/use cases.
4. Introduce a narrow platform/provider boundary when meaningful tests need it. Bind with Hilt, test with fakes; never call live services from unit tests.
5. Keep SDK calls out of previews. Use `@StarterPreviews` on a state/callback composable; inspect small width, dark, RTL and large text, including content below the fold.
6. Translate new strings or explicitly document English fallback. Give meaningful headings/actions semantics; decorative icons remain unannounced. Update `LanguageConfigurationTest` when changing available locales.
7. Run offline checks and relevant test classes; compile both build types after Kotlin changes. Preview rendering, device behavior and hardware/service tests require separate evidence.

## Boundaries to preserve

- AdMob, UMP, Firebase Analytics and Billing stay built in. Preview slots/fakes do not make runtime inclusion optional.
- Independent analytics opt-in, UMP gating, debug/unconfigured Firebase safeguards and official debug ad IDs remain enforced.
- Glyph/Matrix own device checks, in-app rendering, errors and teardown. No background lights/gaming triggers.
- No full-variant, root/Shizuku/Xposed, games, self-updater or arbitrary process execution for capabilities.
- Keep licenses/attribution. Compilation and architectural patterns do not establish rights, policy compliance or release readiness.
