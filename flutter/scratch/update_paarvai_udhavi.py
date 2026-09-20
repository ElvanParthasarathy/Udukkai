import sys
import re

def update_file(filepath, invoice_type):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Add import
    import_statement = "import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/paarvai_udhavi.dart';"
    if import_statement not in content:
        content = content.replace("import 'dart:convert';", "import 'dart:convert';\n" + import_statement)

    if invoice_type == 'patrucheettu':
        # Replace:
        # final profileJson = _adaptProfileForReact(profile);
        # With:
        # final baseProfileJson = await PaarvaiUdhavi.convertProfileImagesToBase64(profile);
        # final profileJson = _adaptProfileForReact(NiruvanaTharavugal.fromMap(baseProfileJson));
        old_str = "final profileJson = _adaptProfileForReact(profile);"
        new_str = "final baseProfileJson = await PaarvaiUdhavi.convertProfileImagesToBase64(profile);\n          final profileJson = _adaptProfileForReact(NiruvanaTharavugal.fromMap(baseProfileJson));"
        content = content.replace(old_str, new_str)
    else:
        # In kooli and pattu, it uses _handlePrint which takes profile directly.
        # Find _handlePrint and make it use base64 conversion
        if "Future<void> _handlePrint(" in content:
            # We want to insert `final profileJson = await PaarvaiUdhavi.convertProfileImagesToBase64(profile);`
            # and replace `jsonEncode(profile)` with `jsonEncode(profileJson)`
            old_str = "await _printChannel.invokeMethod('printInvoice', {"
            new_str = "final profileJsonConverted = await PaarvaiUdhavi.convertProfileImagesToBase64(profile);\n    await _printChannel.invokeMethod('printInvoice', {"
            content = content.replace(old_str, new_str)
            
            content = content.replace("'profileJson': jsonEncode(profile),", "'profileJson': jsonEncode(profileJsonConverted),")
            
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)


files = {
    "d:/Projects/Elvan Niril/flutter/lib/src/cheyalpaadugal/niril_kooli/kaatchi/paarvai/kooli_pattiyal_paarvai.dart": "pattiyal",
    "d:/Projects/Elvan Niril/flutter/lib/src/cheyalpaadugal/niril_pattu/kaatchi/paarvai/pattu_pattiyal_paarvai.dart": "pattiyal",
    "d:/Projects/Elvan Niril/flutter/lib/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/patrucheettu_paarvai.dart": "patrucheettu"
}

for fp, type_ in files.items():
    update_file(fp, type_)

print("Files updated successfully")
