"""Shared, offline identity validation. No Gradle, accounts, network or file writes."""
import json
import re

JAVA_KEYWORDS = set('abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for goto if implements import instanceof int interface long native new package private protected public return short static strictfp super switch synchronized this throw throws transient try void volatile while true false null'.split())


def identity_errors(name, application_id):
    errors = []
    if not re.fullmatch(r'[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*){2,}', application_id) or len(application_id) > 255:
        errors.append('Use at least three lowercase ID components, e.g. com.yourcompany.yourapp (maximum 255 characters)')
    elif JAVA_KEYWORDS.intersection(application_id.split('.')):
        errors.append('Application ID components cannot be Java reserved words')
    if not name.strip() or not re.fullmatch(r'[\w .()-]{1,60}', name, re.ASCII):
        errors.append('Use 1–60 simple ASCII letters, numbers, spaces or .()- for the app name')
    return errors


def firebase_matches(path, application_id):
    """Validate only Android client identity; never print Firebase configuration values."""
    try:
        data = json.loads(path.read_text(encoding='utf-8-sig'))
        packages = [client.get('client_info', {}).get('android_client_info', {}).get('package_name')
                    for client in data['client']]
    except (OSError, ValueError, KeyError, TypeError, AttributeError) as error:
        raise ValueError('Firebase configuration is unreadable or malformed. Download a new Android google-services.json.') from error
    return application_id in packages


def read_properties(path):
    """Read the starter's simple unescaped key=value config, not arbitrary Java properties."""
    result = {}
    if path.exists():
        for line in path.read_text(encoding='utf-8-sig').splitlines():
            if line.strip() and not line.lstrip().startswith(('#', '!')):
                key, separator, value = line.partition('=')
                if not separator:
                    raise ValueError(f'{path.name}: expected key=value')
                key = key.strip()
                if key in result:
                    raise ValueError(f'{path.name}: duplicate key {key}')
                result[key] = value.strip()
    return result
