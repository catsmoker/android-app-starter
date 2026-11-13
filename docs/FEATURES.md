# Extraction and feature inventory

## Retained actual implementations

- Compose theme modes (system/light/dark/dynamic), palette/shape/spacing infrastructure, Ndot typography, edge-to-edge/cutout/IME handling, animated dot-matrix startup.
- Reusable buttons/press feedback, section cards/layouts, information bottom sheets, scroll/animation/math helpers, loading/error patterns and accessible switch/checkbox rows.
- Language picker, locale wrapping/restart persistence, Arabic RTL and existing generic translations. Draft onboarding survives language-triggered restarts.
- Appearance and agreement onboarding, terms/privacy dialogs, opt-in analytics choice; optional Android notifications step.
- Settings StateFlow/ViewModel persistence, theme/language dialogs, notification status/system-settings fallback, analytics explanation/confirmation, privacy options, existing billing status/price/purchase and restoration.
- Recurring support dialog (every fifth launch, never during onboarding; do-not-ask option from second showing), honest Play rating with browser fallback, support destination.
- About/read more/version clipboard + accessible action, legal layouts/dialogs, licenses, developer/source/website/contact links.
- Own-UID logcat live buffer/auto-tail, copy/share/clear confirmation, bounded crash records, user-requested HTML/ZIP diagnostic export. No device-wide log permission, background report upload, root or shell privileges.
- User-requested installed **base APK** share via scoped FileProvider cache, with existing split-APK warning. No install-packages permission or self-updater.
- AdMob anchored adaptive banner and UMP consent/privacy options; Firebase Analytics current-version explicit consent; existing `remove_ads` one-time Play product, pending/purchased handling, acknowledgement retry worker and cached entitlement reconciliation.
- Nothing SDK 2.0 strip progress/fallback, Matrix percentage text, hardware model/API/manufacturer checks, fill/completion/drain/cancel and safe teardown. The generic dot-matrix loading animation is also retained, independently of hardware.

## Generalized or repaired

- Neutral launcher/logo, teal accent, app identity, support/contact links and home navigation. No hardcoded CatSmoker store/profile URLs or advertising accounts.
- Nothing controls moved out of the former gaming folder into `screens/glyph`. No game/session triggers. Matrix uses the current **in-app** API (`setAppMatrixFrame`/`closeAppMatrix`) rather than replacing an active toy. Binding/registration/frame failures surface status, timeout safely, and release on screen stop/disposal.
- Optional notification onboarding requests only the notification permission; denial does not block app use. No game inventory/usage consent.
- Support screen keeps the reusable polished card/layout style but replaces production crypto/payment destinations with honest rating, existing Play ad-removal purchase and developer contact. No purchase products invented.
- No ad-view solicitation existed in the source Play support dialog; none was invented. No click/rating incentives, forced ratings or five-star promises. Banner sits apart from controls, pauses/resumes with lifecycle and is destroyed on removal.
- All legal bodies are visibly marked sample; no stale claims about removed tools. New/changed text falls back to English until reviewed translations exist.
- AdMob official test identifiers forced in debug; Firebase never initialized in debug or without this app's own configuration. Integrations remain mandatory dependencies.
- Reference-template improvements: fake-tested Settings Android boundary, resilient share busy/error/cancellation handling, buffered language-restart events and current-locale messages. French/British English selection and normalization repaired; picker entries match Android locale advertising.
- Five SDK-free Settings previews (small/dark/tablet/Arabic/large text), accessibility heading semantics, identity dry-run/atomic write, offline setup doctor and bounded verification entry point. See `TEMPLATE_COMPARISON.md` for adopted/rejected patterns.

## Completely excluded, not merely hidden

All `src/full`, GitHub distribution behavior, flavor plumbing, Start.io and its consent/ads resources, self-updater/download/install/deep-link infrastructure; gaming mode/tools/game grids and registries; game-file editing/assets/storage/AIDL; telemetry/performance overlays/crosshairs; device spoofing; native bridge/NDK/CMake; root/libsu; Xposed/LSPosed/Magisk; Shizuku; VPN; privileged shell/settings writes; boot gaming recovery and sweep workers; game-specific permissions, services, package queries and resources. No full-only implementation required extraction: both real Nothing implementations were in shared code and did not depend on those capabilities.

Source layouts/provenance hashes are in `tools/extraction.json`; retained resource counts/fallbacks in `tools/resource-inventory.json`. Attribution mentions are intentional exceptions to brand removal, not product branding. Some reusable components have no current home-screen call site; they remain as the requested reusable UI toolkit, not gaming features.
