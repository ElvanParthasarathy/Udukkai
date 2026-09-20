import sys

def replace_in_file(filepath, search_str, replace_str):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    if search_str in content:
        content = content.replace(search_str, replace_str)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Replaced in {filepath}")
    else:
        print(f"Not found in {filepath}")

kooli_file = r"d:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_kooli\kaatchi\paarvai\kooli_pattiyal_paarvai.dart"
pattu_file = r"d:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\paarvai\pattu_pattiyal_paarvai.dart"

old_str = '''    final profileJsonConverted = await PaarvaiUdhavi.convertProfileImagesToBase64(profile);
    await _printChannel.invokeMethod('printInvoice', {
      'invoiceJson': jsonEncode(pattiyalJson),
      'profileJson': jsonEncode(profileJsonConverted),'''

new_str = '''    final profileJsonConverted = profile != null 
        ? (await PaarvaiUdhavi.convertProfileImagesToBase64(profile)).toJson() 
        : <String, dynamic>{};
    await _printChannel.invokeMethod('printInvoice', {
      'invoiceJson': jsonEncode(pattiyalJson),
      'profileJson': jsonEncode(profileJsonConverted),'''

replace_in_file(kooli_file, old_str, new_str)
replace_in_file(pattu_file, old_str, new_str)
