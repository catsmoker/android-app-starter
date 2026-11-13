"""Check existing build evidence without invoking Gradle. Not release certification."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--dependency-log', type=Path, required=True,
                    help='Successful Gradle releaseRuntimeClasspath dependency report')
args = parser.parse_args()
checks = []


def check(ok, description):
    checks.append({'check': description, 'passed': bool(ok)})


android = '{http://schemas.android.com/apk/res/android}'
identity = dict(line.split('=', 1) for line in (ROOT / 'starter.properties').read_text().splitlines()
                if '=' in line and not line.startswith('#'))
app_id = identity['applicationId']
allowed_permissions = {
    'android.permission.INTERNET', 'android.permission.ACCESS_NETWORK_STATE',
    'android.permission.POST_NOTIFICATIONS', 'com.nothing.ketchum.permission.ENABLE',
    'com.google.android.gms.permission.AD_ID', 'android.permission.ACCESS_ADSERVICES_AD_ID',
    'android.permission.ACCESS_ADSERVICES_ATTRIBUTION', 'android.permission.ACCESS_ADSERVICES_TOPICS',
    'com.android.vending.BILLING', 'android.permission.WAKE_LOCK',
    'com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE',
    app_id + '.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION',
}
manifest_permissions = {}
for variant in ('debug', 'release'):
    path = ROOT / f'app/build/intermediates/merged_manifests/{variant}/process{variant.title()}Manifest/AndroidManifest.xml'
    check(path.exists(), variant + ' merged manifest exists')
    if not path.exists():
        continue
    tree = ET.parse(path)
    check(tree.getroot().get('package') == app_id, variant + ' independent identity')
    permissions = {p.get(android + 'name') for p in tree.findall('uses-permission')}
    manifest_permissions[variant] = sorted(permissions)
    check(permissions == allowed_permissions, variant + ' permission allowlist (including SDK requirements)')
    names = [e.get(android + 'name', '') for e in tree.findall('.//*')]
    check(not any(re.search(r'catsmoker|shizuku|xposed|startapp|spoof|gaming|VpnService|SystemForegroundService|FirebaseInitProvider', n, re.I)
                  for n in names), variant + ' no excluded components or Firebase auto-init')
    launchers = tree.findall('.//activity/intent-filter/category[@' + android + "name='android.intent.category.LAUNCHER']")
    check(len(launchers) == 1, variant + ' one launcher')
    check(not tree.findall('.//queries/package[@' + android + "name='com.roblox.client']"), variant + ' no game package queries')
    metadata = {m.get(android + 'name'): m.get(android + 'value') for m in tree.findall('./application/meta-data')}
    check(metadata.get('firebase_analytics_collection_enabled') == 'false', variant + ' analytics default off')
    if variant == 'debug':
        check(metadata.get('firebase_analytics_collection_deactivated') == 'true', 'Debug analytics SDK permanently deactivated')
        check(metadata.get('com.google.android.gms.ads.APPLICATION_ID') == 'ca-app-pub-3940256099942544~3347511713', 'Debug official test AdMob app ID')

deps = args.dependency_log.read_text(encoding='utf-8-sig')
check('BUILD SUCCESSFUL' in deps and 'releaseRuntimeClasspath' in deps, 'Successful resolved dependency evidence')
check(not re.search(r'libsu|com\.github\.topjohnwu|rikka\.shizuku|de\.robv\.android|startapp|startio|xposed|magisk', deps, re.I), 'No full-only/privileged resolved dependencies')
for coordinate in ('com.google.android.gms:play-services-ads:', 'com.google.firebase:firebase-analytics:',
                   'com.google.android.ump:user-messaging-platform:', 'com.android.billingclient:billing:'):
    check(coordinate in deps, 'Resolved built-in ' + coordinate)

apk = ROOT / 'app/build/outputs/apk/debug/app-debug.apk'
check(apk.exists(), 'Debug installable artifact exists')
apk_sha256 = None
if apk.exists():
    apk_sha256 = hashlib.sha256(apk.read_bytes()).hexdigest()
    with zipfile.ZipFile(apk) as archive:
        dexes = [p for p in archive.namelist() if re.fullmatch(r'classes\d*\.dex', p)]
        check(bool(dexes), 'APK contains compiled code')
        banned = [b'Lcom/catsmoker/', b'Lcom/startapp/', b'Lcom/topjohnwu/',
                  b'Lrikka/shizuku/', b'Lde/robv/android/xposed/']
        contents = [archive.read(p) for p in dexes]
        for marker in banned:
            check(not any(marker in dex for dex in contents), 'No excluded APK class descriptor ' + marker.decode())
        check('assets/NOTHING-SDK-LICENSE.md' in archive.namelist(), 'APK packages retained SDK EULA')

report = {'passed': all(c['passed'] for c in checks), 'checks': checks,
          'permissions': manifest_permissions, 'debug_apk_sha256': apk_sha256,
          'limitations': ['Release was compiled and manifest-processed, not shrunk, signed or bundled.',
                          'Binary descriptor scan is bounded; this is not a vulnerability/license/policy audit.']}
(ROOT / 'tools/artifact-report.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))
sys.exit(not report['passed'])
