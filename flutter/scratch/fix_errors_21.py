import os
import re

pattu_kalanjiyam = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kalanjiyam\pattu_pattiyal_kalanjiyam.dart"
with open(pattu_kalanjiyam, "r", encoding="utf-8") as f:
    content = f.read()

fin_year_method = """  static int getCurrentFinYear() {
    final now = DateTime.now();
    if (now.month >= 4) {
      return now.year;
    }
    return now.year - 1;
  }"""

if "getCurrentFinYear" not in content:
    content = re.sub(
        r"class PattuPattiyalKalanjiyam implements PattiyalKalanjiyamInterface \{",
        f"class PattuPattiyalKalanjiyam implements PattiyalKalanjiyamInterface {{\n{fin_year_method}",
        content
    )
    with open(pattu_kalanjiyam, "w", encoding="utf-8") as f:
        f.write(content)

kooli_kalanjiyam = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kalanjiyam\kooli_pattiyal_kalanjiyam.dart"
with open(kooli_kalanjiyam, "r", encoding="utf-8") as f:
    content2 = f.read()

if "getCurrentFinYear" not in content2:
    content2 = re.sub(
        r"class KooliPattiyalKalanjiyam implements PattiyalKalanjiyamInterface \{",
        f"class KooliPattiyalKalanjiyam implements PattiyalKalanjiyamInterface {{\n{fin_year_method}",
        content2
    )
    with open(kooli_kalanjiyam, "w", encoding="utf-8") as f:
        f.write(content2)

print("Added getCurrentFinYear to kalanjiyams.")
