"""Exports the React app's company images and print settings into KMP seed resources.

Reads  archive/tharavu (React data)  ->  writes  kmp/composeApp/src/commonMain/composeResources/files/seed/
Run:   python -I kmp/tools/export_react_seed.py   (from the repository root)
"""
import json
import pathlib
import sys

root = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path.cwd()
src = root / 'archive' / 'tharavu'
out = root / 'kmp' / 'composeApp' / 'src' / 'commonMain' / 'composeResources' / 'files' / 'seed'
out.mkdir(parents=True, exist_ok=True)


def load(p):
    return json.loads(p.read_text(encoding='utf-8'))


def write_image(name, value):
    if value:
        (out / f'{name}.txt').write_text(value, encoding='utf-8')
        return f'seed:{name}'
    return ''


settings = {}

# GST / Textiles company (React "thannilai")
for f in (src / 'thannilai').glob('*.json'):
    p = load(f)
    key = p.get('shortBusinessName') or f.stem
    k = key.lower()
    settings[key] = {
        'logo': write_image(f'{k}_logo', p.get('logo')),
        'wideLogo': write_image(f'{k}_wide_logo', p.get('wideLogo')),
        'signature': write_image(f'{k}_signature', p.get('signature')),
        'print': {
            'logoHeight': p.get('logoHeight', 120),
            'wideLogoX': p.get('wideLogoX', 0),
            'wideLogoY': p.get('wideLogoY', 0),
            'wideLogoScale': p.get('wideLogoScale', 1),
            'invoiceDisplayOptions': load(src / 'meta.json').get('invoiceDisplayOptions', {}),
        },
    }

# Coolie / Factory companies (React "coolie_thannilai")
for f in (src / 'coolie_thannilai').glob('*.json'):
    p = load(f)
    key = p.get('shortBusinessName') or f.stem
    k = key.lower()
    settings[key] = {
        'logo': write_image(f'{k}_logo', p.get('logo')),
        'wideLogo': '',
        'signature': write_image(f'{k}_signature', p.get('signature')),
        'print': {
            'receiptLanguage': p.get('receiptLanguage', 'ta'),
            'defaultPrintLanguage': p.get('defaultPrintLanguage', 'ta'),
        },
    }

(out / 'react_companies.json').write_text(json.dumps(settings, ensure_ascii=False, indent=1), encoding='utf-8')
print('exported', ', '.join(sorted(settings)), '->', out)
