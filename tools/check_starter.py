"""Small offline structural/privacy guard; not a vulnerability or Play compliance audit."""
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
errors = []
checks = []
def check(ok, description):
    checks.append({'check': description, 'passed': bool(ok)})
    if not ok: errors.append(description)

sets = {p.name for p in (ROOT / 'app/src').iterdir() if p.is_dir()}
check(sets <= {'main', 'test', 'androidTest', 'debug', 'release'}, 'Only single-app source sets')
gradle = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf8')
catalog = (ROOT / 'gradle/libs.versions.toml').read_text(encoding='utf8')
check(not re.search(r'productFlavors|flavorDimensions|externalNativeBuild|cmake|libsu|shizuku|xposed|startio|startapp', gradle+catalog, re.I), 'No flavor/native/privileged/exclusive dependencies')
for dep in ['play.services.ads', 'firebase.analytics', 'billing.ktx', 'ump']:
    check(f'implementation(libs.{dep})' in gradle, f'Built-in {dep}')
check('ca-app-pub-3940256099942544/9214589741' in gradle, 'Official development banner ID')
sources = list((ROOT / 'app/src/main/java').rglob('*.kt'))
navigation = (ROOT / 'app/src/main/java/com/example/androidstarter/application/navigation/AppNavHost.kt').read_text(encoding='utf8')
routes = re.findall(r'const val (\w+) = "([^"]+)"', navigation)
destinations = set(re.findall(r'composable\(Routes\.(\w+)\)', navigation))
references = set(re.findall(r'Routes\.(\w+)', '\n'.join(p.read_text(encoding='utf8') for p in sources)))
check(references == destinations == {name for name, _ in routes}, 'Every referenced route has a destination')
check(len({value for _, value in routes}) == len(routes), 'Route values are unique')
for p in sources:
    text = p.read_text(encoding='utf8')
    expected = '.'.join(p.parent.relative_to(ROOT / 'app/src/main/java').parts)
    check(text.startswith('package '+expected+'\n'), 'Package matches '+str(p.relative_to(ROOT)))
    check(not re.search(r'com\.catsmoker|com\.topjohnwu|rikka\.|de\.robv|com\.startapp|screens\.gaming|features\.spoof|ShellRunner', text), 'No removed API imports in '+p.name)
manifest = ROOT / 'app/src/main/AndroidManifest.xml'
tree = ET.parse(manifest)
android = '{http://schemas.android.com/apk/res/android}'
perms = {e.get(android+'name') for e in tree.findall('uses-permission') if e.get('{http://schemas.android.com/tools}node') != 'remove'}
check(perms == {'android.permission.INTERNET', 'android.permission.ACCESS_NETWORK_STATE',
                'android.permission.POST_NOTIFICATIONS', 'com.nothing.ketchum.permission.ENABLE'}, 'Minimal declared app permissions')
check(not [e for e in tree.findall('.//service') if e.get('{http://schemas.android.com/tools}node') != 'remove'], 'No app-owned background or privileged services')
analytics = (ROOT / 'app/src/main/java/com/example/androidstarter/application/privacy/AnalyticsConsent.kt').read_text()
check('BuildConfig.DEBUG || !com.example.androidstarter.BuildConfig.FIREBASE_CONFIGURED' in analytics, 'Debug/unconfigured analytics safeguard')
provider = tree.find(".//provider[@"+android+"name='com.google.firebase.provider.FirebaseInitProvider']")
check(provider is not None and provider.get('{http://schemas.android.com/tools}node') == 'remove', 'Firebase auto initialization removed')
base = {}
for p in (ROOT / 'app/src/main/res/values').glob('*.xml'):
    for e in ET.parse(p).getroot():
        if e.tag == 'string': base[e.get('name')] = ''.join(e.itertext())
required = set(re.findall(r'R\.string\.(\w+)', '\n'.join(p.read_text(encoding='utf8') for p in sources)))
check(not (required - base.keys() - {'app_name'}), 'All referenced default strings exist')
for p in (ROOT / 'app/src/main/res').glob('values-*/strings.xml'):
    for e in ET.parse(p).getroot():
        text = ''.join(e.itertext())
        key = e.get('name')
        check(not re.search(r'catsmoker|ca-app-pub-|AIza|pubg|magisk|shizuku|vpn', text, re.I), 'Neutral translation '+str(p.parent.name)+':'+str(key))
        check(set(re.findall(r'%\d+\$[sd]', text)) == set(re.findall(r'%\d+\$[sd]', base.get(key, ''))), 'Translation format '+str(p.parent.name)+':'+str(key))
for p in (ROOT / 'app/src').rglob('*'):
    if p.suffix in {'.kt', '.xml'}:
        text = p.read_text(encoding='utf8')
        check(not re.search(r'com\.catsmoker|https?://[^\s"<]*catsmoker|ca-app-pub-(?!3940256099942544)[0-9]+|AIza[\w-]{20,}', text, re.I), 'No production identity '+str(p.relative_to(ROOT)))
report = {'passed': not errors, 'checks': len(checks), 'failures': errors,
          'limitations': ['Source checks only; inspect resolved dependencies and merged manifest separately.',
                          'No license clearance, security certification or policy compliance assertion.']}
(ROOT / 'tools/check-report.json').write_text(json.dumps(report, indent=2)+'\n', encoding='utf8')
print(json.dumps(report, indent=2))
sys.exit(bool(errors))
