"""Identity setup tests use disposable copies, never the real project/configuration."""
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest


class ConfigureTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='starter-configure-')
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root / 'tools').mkdir()
        shutil.copyfile(Path(__file__).with_name('configure.py'), self.root / 'tools/configure.py')
        shutil.copyfile(Path(__file__).with_name('starter_config.py'), self.root / 'tools/starter_config.py')
        self.identity = self.root / 'starter.properties'
        self.identity.write_text('appName=Original\napplicationId=com.example.original\n')

    def run_setup(self, name='My App', app_id='org.example.myapp', extra=()):
        return subprocess.run([sys.executable, str(self.root / 'tools/configure.py'),
                               '--name', name, '--application-id', app_id, *extra],
                              capture_output=True, text=True)

    def test_valid_identity(self):
        result = self.run_setup()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('appName=My App\napplicationId=org.example.myapp\n', self.identity.read_text())

    def test_invalid_identity_does_not_write(self):
        before = self.identity.read_bytes()
        for name, app_id in [('My App', 'bad'), ('Bad\nName', 'org.example.myapp'),
                             ('   ', 'org.example.myapp'), ("John's App", 'org.example.myapp'),
                             ('My App', 'org.class.app')]:
            with self.subTest(name=name, app_id=app_id):
                self.assertNotEqual(self.run_setup(name, app_id).returncode, 0)
                self.assertEqual(before, self.identity.read_bytes())

    def test_mismatched_firebase_does_not_write(self):
        (self.root / 'app').mkdir()
        (self.root / 'app/google-services.json').write_text(json.dumps({'client': [
            {'client_info': {'android_client_info': {'package_name': 'org.example.other'}}}]}))
        before = self.identity.read_bytes()
        self.assertNotEqual(self.run_setup().returncode, 0)
        self.assertEqual(before, self.identity.read_bytes())

    def test_dry_run_does_not_write(self):
        before = self.identity.read_bytes()
        result = self.run_setup(extra=['--dry-run'])
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('no files changed', result.stdout)
        self.assertEqual(before, self.identity.read_bytes())

    def test_malformed_firebase_does_not_write_or_dump_config(self):
        (self.root / 'app').mkdir()
        firebase = self.root / 'app/google-services.json'
        before = self.identity.read_bytes()
        for value in ['private_invalid_json', '{"client": null}', '{"client": [42]}', '[]']:
            with self.subTest(value=value):
                firebase.write_text(value)
                result = self.run_setup()
                self.assertNotEqual(result.returncode, 0)
                self.assertIn('unreadable or malformed', result.stderr)
                self.assertNotIn('Traceback', result.stderr)
                self.assertNotIn(value, result.stderr)
                self.assertEqual(before, self.identity.read_bytes())

    def test_matching_firebase_allows_rename_and_no_temporary_file_remains(self):
        (self.root / 'app').mkdir()
        (self.root / 'app/google-services.json').write_text(json.dumps({'client': [
            {'client_info': {'android_client_info': {'package_name': 'org.example.myapp'}}}]}))
        result = self.run_setup()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertFalse(list(self.root.glob('.identity-*')))


if __name__ == '__main__':
    unittest.main()
