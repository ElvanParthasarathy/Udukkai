import os
import re

def fix_file(filepath, app_mode):
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        return
        
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Replace *Entry? product with *Tharavuru? product
    content = re.sub(r'PorulEntry\??', r'PorulTharavuru?', content)
    content = re.sub(r'VaangunarEntry\??', r'VaangunarTharavuru?', content)
    content = re.sub(r'PatrucheettuEntry\??', r'PattiyalTharavuru?', content)
    content = re.sub(r'PatrugalEntry\??', r'PatrugalTharavuru?', content)

    # 2. Fix savePorul in Pattu and Kooli
    porul_save = f"""    final tharavuru = PorulTharavuru(
      id: widget.product?.id ?? 0,
      porulPeyar: _porulPeyar,
      hsnCode: _hsnCodeController.text.trim(),
      vilai: double.tryParse(_vilaiController.text.trim()) ?? 0.0,
      variVeetham: double.tryParse(_variVeethamController.text.trim()) ?? 0.0,
      alavuVagai: _alavuVagai,
      alagu: _alagu,
      createdAt: widget.product?.createdAt ?? DateTime.now(),
      updatedAt: DateTime.now(),
      isDeleted: widget.product?.isDeleted ?? false,
    );

    kalanjiyam.savePorul(tharavuru).then"""
    
    old_porul = r"""    kalanjiyam.savePorul\(\s*id:\s*_isEditing \? widget\.product!\.id : null,\s*seyaliVagai:\s*'[a-z]+',\s*porulPeyar:\s*_porulPeyar,\s*hsnCode:\s*_hsnCodeController\.text\.trim\(\),\s*vilai:\s*double\.tryParse\(_vilaiController\.text\.trim\(\)\) \?\? 0\.0,\s*variVeetham:\s*double\.tryParse\(_variVeethamController\.text\.trim\(\)\) \?\? 0\.0,\s*alavuVagai:\s*_alavuVagai,\s*alagu:\s*_alagu,\s*\)\.then"""
    
    content = re.sub(old_porul, porul_save, content)

    # 3. Fix saveVaangunar
    vaangunar_save = f"""    final tharavuru = VaangunarTharavuru(
      id: widget.merchant?.id ?? 0,
      peyar: _peyar,
      mugavari: _mugavari,
      oor: _oor,
      maavattam: _maavattam,
      maanilam: _maanilam,
      naadu: _naadu,
      velinaadMugavari: _velinaadMugavari,
      anjalKuriyeedu: _anjalKuriyeeduController.text.trim(),
      gstin: _gstinController.text.trim(),
      minnanjal: _minnanjalController.text.trim(),
      tholaipaesi: _tholaipaesiController.text.trim(),
      createdAt: widget.merchant?.createdAt ?? DateTime.now(),
      updatedAt: DateTime.now(),
      isDeleted: widget.merchant?.isDeleted ?? false,
    );
    
    kalanjiyam.saveVaangunar(tharavuru).then"""

    old_vaangunar = r"""    kalanjiyam.saveVaangunar\(\s*id:\s*_isEditing \? widget\.merchant!\.id : null,\s*seyaliVagai:\s*'[a-z]+',\s*peyar:\s*_peyar,\s*mugavari:\s*_mugavari,\s*oor:\s*_oor,\s*maavattam:\s*_maavattam,\s*maanilam:\s*_maanilam,\s*naadu:\s*_naadu,\s*velinaadMugavari:\s*_velinaadMugavari,\s*anjalKuriyeedu:\s*_anjalKuriyeeduController\.text\.trim\(\),\s*gstin:\s*_gstinController\.text\.trim\(\),\s*minnanjal:\s*_minnanjalController\.text\.trim\(\),\s*tholaipaesi:\s*_tholaipaesiController\.text\.trim\(\),\s*\)\.then"""
    
    content = re.sub(old_vaangunar, vaangunar_save, content)

    # Add uruvugal import if not present
    if 'adippadai/tharavuru/uruvugal.dart' not in content:
        content = content.replace("import 'package:flutter/material.dart';", "import 'package:flutter/material.dart';\nimport 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
        print(f"Fixed {filepath}")

base = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal'

fix_file(os.path.join(base, r'niril_pattu\kaatchi\thiruthi\porul\niril_pattu_porul_thiruthi.dart'), 'silk')
fix_file(os.path.join(base, r'niril_kooli\kaatchi\thiruthi\porul\niril_kooli_porul_thiruthi.dart'), 'coolie')
fix_file(os.path.join(base, r'niril_pattu\kaatchi\thiruthi\vaangunar\niril_pattu_vaangunar_thiruthi.dart'), 'silk')
fix_file(os.path.join(base, r'niril_kooli\kaatchi\thiruthi\vaangunar\niril_kooli_vaangunar_thiruthi.dart'), 'coolie')

print("Fixed porul and vaangunar")
