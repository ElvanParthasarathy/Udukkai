import os

thannuru = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\koorugal\maeladukkugal\thannuru_maeladukkugal.dart"
with open(thannuru, "r", encoding="utf-8") as f:
    content = f.read()

# Undo lowercase replacements
content = content.replace("niruvanaTharavugalListProvider", "NiruvanaTharavugalListProvider")
content = content.replace("niruvanaTharavugalProvider", "NiruvanaTharavugalProvider")

# But ensure niruvanaTharavugalNotifierProvider stays lowercase!
content = content.replace("NiruvanaTharavugalNotifierProvider", "niruvanaTharavugalNotifierProvider")

with open(thannuru, "w", encoding="utf-8") as f:
    f.write(content)

print("Restored provider names in thannuru_maeladukkugal.dart.")
