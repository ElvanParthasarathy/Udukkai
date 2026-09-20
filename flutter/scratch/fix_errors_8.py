import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kalanjiyam"
files = ["kooli_patru_kalanjiyam.dart", "pattu_patru_kalanjiyam.dart"]

for filename in files:
    filepath = os.path.join(base_dir, filename)
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    # 1. parivarthanaiEn: entry.parivarthanaiEn -> parivarthanaiEn: entry.parivarthanaiEn ?? ''
    content = re.sub(r"parivarthanaiEn:\s*entry\.parivarthanaiEn,", r"parivarthanaiEn: entry.parivarthanaiEn ?? '',", content)

    # 2. ullkurippu: entry.ullkurippu -> ullkurippu: entry.ullkurippu ?? ''
    content = re.sub(r"ullkurippu:\s*entry\.ullkurippu,", r"ullkurippu: entry.ullkurippu ?? '',", content)

    # 3. finYear string vs int parsing
    # Map to domain: finYear: entry.finYear, -> finYear: int.tryParse(entry.finYear) ?? DateTime.now().year,
    content = re.sub(r"finYear:\s*entry\.finYear,", r"finYear: int.tryParse(entry.finYear.toString()) ?? DateTime.now().year,", content)

    # Map to companion: finYear: Value(data.finYear), -> finYear: Value(data.finYear.toString()),
    content = re.sub(r"finYear:\s*Value\(data\.finYear\),", r"finYear: Value(data.finYear.toString()),", content)

    # 4. Map casting: vaangunarPeyar: Value(data.vaangunarPeyar), -> Value(data.vaangunarPeyar.cast<String, String>()),
    content = re.sub(r"vaangunarPeyar:\s*Value\(data\.vaangunarPeyar\),", r"vaangunarPeyar: Value(data.vaangunarPeyar.cast<String, String>()),", content)

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)

print("Patru Kalanjiyam fixed.")
