"""Explain missing starter configuration offline; --release treats placeholders as blockers.

This validates local configuration, not policy compliance, service delivery or legal rights.
It never displays keys, writes files, calls Gradle or contacts a service.
"""
import argparse
from pathlib import Path
import re
import xml.etree.ElementTree as ET
from starter_config import firebase_matches, identity_errors, read_properties

ROOT = Path(__file__).resolve().parents[1]


def inspect(root, release=False):
    findings = []

    def record(ok, description, required=False):
        level = 'PASS' if ok else ('FAIL' if required or release else 'NOTE')
        findings.append((level, description))

    try:
        identity = read_properties(root / 'starter.properties')
        services = read_properties(root / 'services.properties')
        name, app_id = identity.get('appName', ''), identity.get('applicationId', '')
        record(not identity_errors(name, app_id), 'Valid app name and application ID in starter.properties', required=True)
        record(not app_id.startswith(('com.example.', 'org.example.', 'com.yourcompany.')),
               'Replace the example application ID before publication')
        firebase = root / 'app/google-services.json'
        if firebase.exists():
            record(firebase_matches(firebase, app_id), 'Firebase Android client matches application ID', required=True)
        else:
            record(False, 'Supply your own app/google-services.json for release analytics; debug remains safely off')
        for key, separator in [('ADMOB_APP_ID', '~'), ('ADMOB_BANNER_ID', '/')]:
            value = services.get(key, '')
            valid = bool(re.fullmatch(r'ca-app-pub-\d{16}'+re.escape(separator)+r'\d{10}', value))
            record(valid and not value.startswith('ca-app-pub-3940256099942544'),
                   f'Supply your own {key} in services.properties; debug always uses official test IDs')
        record(services.get('NOTHING_API_KEY', '') not in ('', 'test'), 'Supply your approved Nothing SDK key')
        brands = {item.get('name'): ''.join(item.itertext()) for item in
                  ET.parse(root / 'app/src/main/res/values/brand.xml').getroot() if item.tag == 'string'}
        for key in ['developer_name', 'url_webpage', 'url_contact', 'url_support', 'url_legal', 'url_terms']:
            value = brands.get(key, '')
            record(bool(value) and not re.search(r'example\.(com|invalid)|Your Developer Name', value, re.I),
                   f'Customize {key} in res/values/brand.xml')
        legal = (root / 'app/src/main/res/values/strings.xml').read_text(encoding='utf8')
        record('SAMPLE' not in legal, 'Replace SAMPLE legal documents with reviewed app-specific content')
    except (OSError, ValueError, ET.ParseError) as error:
        # The helpers never include configuration values in their error messages.
        findings.append(('FAIL', 'Configuration cannot be read; check file formats and required paths ('+type(error).__name__+')'))
    return findings


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--release', action='store_true', help='Fail for publication placeholders, not just malformed configuration')
    args = parser.parse_args()
    for level, description in (findings := inspect(ROOT, args.release)):
        print(f'{level}: {description}')
    print('MANUAL: Source, Nothing SDK and font rights; service-console setup, real public links/legal review, Play declarations and signing are NOT verified.')
    print('Development can run without service configuration. A successful doctor check is not release approval.')
    return int(any(level == 'FAIL' for level, _ in findings))


if __name__ == '__main__':
    raise SystemExit(main())
