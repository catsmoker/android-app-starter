"""Run bounded local checks; add --android explicitly for compile + selected unit tests.

No clean, assemble, device, cloud, publication or full test suite is invoked.
"""
import argparse
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]


def commands(android=False):
    result = [[sys.executable, str(ROOT / 'tools' / script)] for script in
              ['check_starter.py', 'test_configure.py', 'test_doctor.py', 'test_verify.py', 'doctor.py']]
    if android:
        wrapper = ROOT / ('gradlew.bat' if sys.platform == 'win32' else 'gradlew')
        result.append([str(wrapper), ':app:compileDebugKotlin', ':app:compileReleaseKotlin',
                       ':app:testDebugUnitTest', '--tests', '*SettingsViewModelTest',
                       '--tests', '*OnboardingStateTest', '--tests', '*PrivacyPolicyTest',
                       '--tests', '*AdConsentPolicyTest', '--tests', '*LanguageConfigurationTest', '--console=plain'])
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--android', action='store_true', help='Also compile debug/release and run the five listed test classes; needs JDK/SDK')
    args = parser.parse_args()
    for command in commands(args.android):
        print('Running '+Path(command[0]).name+' '+ ' '.join(command[1:]), flush=True)
        try:
            completed = subprocess.run(command, cwd=ROOT, check=False)
        except OSError:
            print('Cannot start check. Verify Python/JDK/Android SDK and wrapper availability.', file=sys.stderr)
            return 1
        if completed.returncode:
            return completed.returncode
    print('Requested checks passed. No APK build, device test or release approval was performed.')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
