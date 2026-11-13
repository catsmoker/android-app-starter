"""Configuration diagnostics use synthetic files and never read the user's service config."""
import json
from pathlib import Path
import tempfile
import unittest
from doctor import inspect
from starter_config import read_properties


class DoctorTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='starter-doctor-')
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root / 'app/src/main/res/values').mkdir(parents=True)
        (self.root / 'starter.properties').write_text('appName=My App\napplicationId=org.acme.myapp\n')
        self.brand = self.root / 'app/src/main/res/values/brand.xml'
        self.brand.write_text('<resources>'+''.join(
            f'<string name="{key}">{value}</string>' for key, value in {
                'developer_name': 'Acme', 'url_webpage': 'https://acme.test',
                'url_contact': 'mailto:dev@acme.test', 'url_support': 'https://acme.test/support',
                'url_legal': 'https://acme.test/privacy', 'url_terms': 'https://acme.test/terms'
            }.items())+'</resources>')
        self.legal = self.root / 'app/src/main/res/values/strings.xml'
        self.legal.write_text('<resources><string name="legal">SAMPLE content</string></resources>')

    def firebase(self, package='org.acme.myapp'):
        (self.root / 'app/google-services.json').write_text(json.dumps({'client': [
            {'client_info': {'android_client_info': {'package_name': package}}}]}))

    def test_unconfigured_development_is_usable_but_explains_setup(self):
        findings = inspect(self.root)
        self.assertFalse(any(level == 'FAIL' for level, _ in findings))
        self.assertTrue(any(level == 'NOTE' and 'google-services' in text for level, text in findings))

    def test_unconfigured_release_is_blocked(self):
        findings = inspect(self.root, release=True)
        self.assertTrue(any(level == 'FAIL' and 'SAMPLE' in text for level, text in findings))
        self.assertTrue(any(level == 'FAIL' and 'ADMOB' in text for level, text in findings))

    def test_matching_firebase_is_accepted(self):
        self.firebase()
        self.assertIn(('PASS', 'Firebase Android client matches application ID'), inspect(self.root))

    def test_wrong_firebase_identity_is_a_development_error(self):
        self.firebase('org.acme.other')
        self.assertTrue(any(level == 'FAIL' and 'Firebase Android client' in text
                            for level, text in inspect(self.root)))

    def test_malformed_firebase_does_not_expose_content(self):
        (self.root / 'app/google-services.json').write_text('private_invalid_config')
        findings = inspect(self.root)
        self.assertTrue(any(level == 'FAIL' for level, _ in findings))
        self.assertNotIn('private_invalid_config', str(findings))

    def test_placeholder_brand_and_test_ad_ids_block_release(self):
        self.brand.write_text('<resources><string name="developer_name">Your Developer Name</string></resources>')
        (self.root / 'services.properties').write_text(
            'ADMOB_APP_ID=ca-app-pub-3940256099942544~3347511713\n'
            'ADMOB_BANNER_ID=ca-app-pub-3940256099942544/9214589741\n')
        findings = inspect(self.root, release=True)
        self.assertTrue(any(level == 'FAIL' and 'developer_name' in text for level, text in findings))
        self.assertFalse(any(level == 'PASS' and 'ADMOB_' in text for level, text in findings))

    def test_malformed_brand_xml_is_reported(self):
        self.brand.write_text('<resources>')
        self.assertTrue(any(level == 'FAIL' for level, _ in inspect(self.root)))

    def test_duplicate_properties_are_not_silently_accepted(self):
        path = self.root / 'services.properties'
        path.write_text('NOTHING_API_KEY=one\nNOTHING_API_KEY=two\n')
        with self.assertRaises(ValueError):
            read_properties(path)
        self.assertTrue(any(level == 'FAIL' for level, _ in inspect(self.root)))


if __name__ == '__main__':
    unittest.main()
