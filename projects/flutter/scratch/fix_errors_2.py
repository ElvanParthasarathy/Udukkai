import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"
path = os.path.join(base_dir, "src", "adippadai", "tharavuthalam", "migration_udhavi.dart")

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Fix niruvanamPeyar -> niruvanathinPeyar
content = content.replace("niruvanamPeyar:", "niruvanathinPeyar:")
content = content.replace("entry.niruvanamPeyar", "entry.niruvanathinPeyar")

# Fix missing imports
if "import '../../cheyalpaadugal/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';" not in content:
    content = content.replace("import 'seyali_tharavuthalam.dart';", "import 'seyali_tharavuthalam.dart';\nimport '../../cheyalpaadugal/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';\nimport '../../cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';")

# Fix all entry fields to be Value(entry.xxx) unless already Value
# Let's just use regex to replace `<something>: entry.<field>,` with `<something>: Value(entry.<field>),`
content = re.sub(r'(\w+):\s*entry\.(\w+),', r'\1: Value(entry.\2),', content)

# But what about the PatrugalEntry missing getters?
# Let's remove them from migration if they didn't exist in the old db.
# Wait, did old db PatrugalTable have finYear?
# If the new table requires them, but old didn't have them, what to do?
# We can just put a default or omit them if they are nullable or have defaults.
# The error said: "The getter 'finYear' isn't defined for the type 'PatrugalEntry'."
# Let's replace Value(entry.finYear) with something else if it's complaining about PatrugalEntry.
# Actually, I'll just comment them out if they cause an error. But wait, we need to know if they are required.

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("Migration udhavi fixed (pass 1).")
