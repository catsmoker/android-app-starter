# Service setup and publication checklist

Run `python tools/doctor.py` for a readable local checklist. `python tools/doctor.py --release` treats detected placeholders as failures, but cannot inspect live consoles, signing, legal adequacy, licensing or Play declarations. Neither command transmits configuration or keys. Missing configuration is expected for development; integrations remain built in.

These integrations are always built in. Missing account configuration is handled safely; it is not a setup option to remove ads or analytics.

## AdMob and UMP

1. Choose your final application ID and create **your own** Android AdMob app/banner unit. Never reuse the source app's identifiers.
2. Copy `services.properties.example` to ignored `services.properties`; replace `ADMOB_APP_ID` (`~`) and `ADMOB_BANNER_ID` (`/`) with your own values for release. Debug ignores both and forces Google's official sample app/test adaptive banner IDs.
3. Configure appropriate privacy messages in AdMob **Privacy & messaging** for your app/regions/audience. Set up authorized test devices and UMP geography testing using official guidance; don't force a production user's geography or manually fabricate consent.
4. Consent information is requested after onboarding each Activity session. The starter fails closed on consent-update/form errors, waits for SDK initialization and suppresses ads for a cached/validated ad-removal entitlement. Required privacy options appear in Settings/Support. No forced/consent-bypassing ad display.
5. The only ad format currently implemented is an adaptive banner on Home, separated from controls. No rewarded support action existed in the source Play implementation. Do not ask users to click or repeatedly view ads to support you; never tie ads to ratings. Review placement and refresh settings in the provider console.

Diagnostics describe consent setup, not guaranteed delivery. Test/no-fill, connectivity, audience and provider errors can leave the slot absent. Development can contact Google's ad/consent services using official test settings; the **no-production-transmission safeguard applies to Firebase Analytics**, not a claim of zero network traffic.

## Firebase Analytics

1. Create your own Firebase project and register the exact new application ID. Download its Android `google-services.json` to `app/google-services.json` (ignored).
2. Gradle applies Google Services processing only when this configuration exists. Analytics/BoM dependencies remain unconditional. A mismatched JSON fails processing instead of quietly selecting CatSmoker's project.
3. Firebase's automatic initialization provider is removed. Manifest collection and all consent defaults are denied. Debug and unconfigured builds return **before any Firebase initialization**, even after opt-in. Only a configured non-debug build with explicit current-version consent initializes/enables Analytics; advertising consent types stay denied independently.
4. Disabling analytics persists the choice and disables SDK collection. When you add events or change data use, update actual disclosures and consent version as appropriate; don't add another auto-initializer or direct `FirebaseAnalytics.getInstance` call that bypasses the owner.

No Firebase Admin credentials, service accounts or production project files are supplied. The ordinary Android client config is not a private service-account key, but it must still be your own configuration and reviewed before sharing. Real analytics delivery and withdrawal need account-backed release testing; a debug test cannot prove them.

## Play billing/ad removal

The existing product ID is **`remove_ads`**, a one-time non-consumable. No subscription, tip or new product was invented. In your Play Console create/configure that product (or deliberately update `RemoveAdsPolicy.PRODUCT_ID` and tests), upload a signed internal-testing build, add license testers and test purchase, cancellation, pending, acknowledgement, reinstall/restore and revocation. Sideloaded/unpublished apps may show unavailable pricing/Billing errors: the rest of the app still works.

Only `PURCHASED` grants entitlement; pending never grants or acknowledges. Existing WorkManager retries query fresh owned purchases and acknowledge rather than persisting raw tokens as work inputs. Queries run on connection, Settings resume and Activity resume. No app server/real-time developer notifications or independent server-side purchase verification has been added; evaluate fraud/revocation requirements for your product before shipping. No automatic policy-compliance claim is made.

## Nothing Glyph and Glyph Matrix

- Actual shared GDK implementation retained, including model-to-channel selection. Known Nothing models are checked **before touching the SDK** and API 34+ is required, conservatively above its AAR minSdk 33. Unknown/non-Nothing devices show unavailable text and no controls that invoke lights.
- Matrix is the actual Nothing hardware integration, not guessed branding: Phone (3) uses 25×25, Phone (4a) Pro 13×13. Strip models retain the source mapping and the no-progress fallback. A matching model does not prove firmware/service availability: binding, registration, developer settings and permission can still fail visibly.
- Use your own approved `NOTHING_API_KEY` in ignored `services.properties` for distribution. The default `test` requires the Nothing device's Glyph developer/debug setting according to Nothing's instructions. **It does not grant a production license.** Follow Nothing's current key registration process for your exact package/signing identity.
- Matrix in-app use requires appropriate firmware (Nothing documents 20250801+), does not register a toy service, yields to toys, and closes when leaving the screen. Phone (4a) Pro does not have the Phone (3) Glyph Touch button. No touch/toy behavior is promised by this starter.
- Supported-device light rendering, firmware fallback and key registration remain unverified unless exercised on the corresponding real hardware. A Poco smoke test only proves safe unsupported-device behavior.
- Read the proprietary [SDK EULA](../notices/NOTHING-SDK-LICENSE.md), especially sections 2.1(c) and 2.2, **before redistributing the AAR or commercial use**. Obtain written clearance; check font rights separately.

## Before any Play publication

Resolve source/SDK/font rights; choose identity/icons/developer/contact; replace sample legal text and links with real public documents; configure and test your own services; choose signing/versioning; complete accurate contains-ads, audience, content rating, Data safety and permission declarations. Review the **merged** release manifest (SDKs add their own necessary permissions/components) and actual data flows. Run release shrink/sign/bundle and account/hardware testing separately. Build success does not establish Play compliance.

Official guidance checked during implementation (2026-11-13; re-check before release):

- [AdMob Android privacy/UMP](https://developers.google.com/admob/android/privacy) and [adaptive banners/test ID](https://developers.google.com/admob/android/banner/anchored-adaptive)
- [AdMob placement policies](https://support.google.com/admob/answer/2936217)
- [Firebase Analytics collection/consent](https://firebase.google.com/docs/analytics/configure-data-collection?platform=android)
- [Play Billing integration/purchase states](https://developer.android.com/google/play/billing/integrate)
- [Google Play app-content/privacy declarations](https://support.google.com/googleplay/android-developer/answer/9859455)
- [Official Nothing Matrix SDK/API/key/firmware documentation](https://github.com/Nothing-Developer-Programme/GlyphMatrix-Developer-Kit)
