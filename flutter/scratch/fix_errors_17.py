import os
import re

thannuru = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\koorugal\maeladukkugal\thannuru_maeladukkugal.dart"
with open(thannuru, "r", encoding="utf-8") as f:
    content = f.read()

# Replace .read(NiruvanaTharavugalListProvider.notifier) with .read(niruvanaTharavugalNotifierProvider)
content = re.sub(r"\.read\(NiruvanaTharavugalListProvider\s*\n?\s*\.notifier\)", ".read(niruvanaTharavugalNotifierProvider)", content)
content = content.replace(".read(NiruvanaTharavugalListProvider.notifier)", ".read(niruvanaTharavugalNotifierProvider)")

# We should also replace any remaining references to NiruvanaTharavugalListProvider
content = content.replace("NiruvanaTharavugalListProvider", "niruvanaTharavugalListProvider")
content = content.replace("NiruvanaTharavugalProvider", "niruvanaTharavugalProvider")

with open(thannuru, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed thannuru_maeladukkugal.dart providers.")
