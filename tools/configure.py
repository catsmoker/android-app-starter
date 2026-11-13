"""Safe app identity setup. Run from any directory; never touches the source project."""
import argparse
import os
from pathlib import Path
import tempfile
from starter_config import firebase_matches, identity_errors

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--name', required=True, help='Visible app name')
parser.add_argument('--application-id', required=True, help='Unique reverse-domain Play identity')
parser.add_argument('--dry-run', action='store_true', help='Validate and preview without writing anything')
args = parser.parse_args()
errors = identity_errors(args.name, args.application_id)
if errors:
    parser.error('; '.join(errors))
args.name = args.name.strip()
if (ROOT / 'app/google-services.json').exists():
    try:
        matches = firebase_matches(ROOT / 'app/google-services.json', args.application_id)
    except ValueError as error:
        parser.error(str(error))
    if not matches:
        parser.error('Existing Firebase config does not match this ID. Supply a config for the new ID first.')
content = (
    f'# Public app identity. Namespace stays com.example.androidstarter by design.\n'
    f'appName={args.name}\napplicationId={args.application_id}\n')
if args.dry_run:
    print('Validated preview; no files changed:\n'+content)
    raise SystemExit(0)
# Same-directory replacement prevents a partially written identity on interruption.
pending = None
try:
    with tempfile.NamedTemporaryFile(mode='w', encoding='ascii', dir=ROOT, prefix='.identity-', delete=False) as output:
        pending = Path(output.name)
        output.write(content)
    os.replace(pending, ROOT / 'starter.properties')
finally:
    if pending is not None:
        pending.unlink(missing_ok=True)
print('Updated starter.properties. No Kotlin moves required. Play links and provider authorities follow the ID.')
print('Next: edit res/values/brand.xml, icons/colors/fonts, service config and sample legal text. Rebuild.')
