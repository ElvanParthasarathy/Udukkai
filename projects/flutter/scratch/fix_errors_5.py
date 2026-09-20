import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"

# 1. Fix PattiyalUdhavi (Pattu & Kooli)
for mode in ['pattu', 'kooli']:
    udhavi_file = os.path.join(base_dir, "src", "cheyalpaadugal", f"niril_{mode}", "kaatchi", "thiruthi", "pattiyal", "koorugal", f"{mode}_pattiyal_udhavi.dart")
    
    if os.path.exists(udhavi_file):
        with open(udhavi_file, "r", encoding="utf-8") as f:
            content = f.read()

        # Replace finYear calculation
        content = content.replace("final finYear = PattiyalKalanjiyam.getCurrentFinYear();", 
"""final now = DateTime.now();
    final finYear = now.month >= 4 ? now.year : now.year - 1;""")

        # Fix the save section. We need to replace the entire if (editingEntry != null) block.
        # It's easier to find the `PattiyalTharavuru(` and `PattiyalTharavuru.insert(` blocks
        # and replace them with standard raw object creations.
        
        # Replace the `updatePattiyal` call block
        update_regex = r"await kalanjiyam\.updatePattiyal\(\s*editingEntry\.id,\s*PattiyalTharavuru\((.*?)\),\s*\);"
        def update_replacer(match):
            args = match.group(1)
            # Remove `Value(...)` wrappers
            args = re.sub(r"Value\((.*?)\)", r"\1", args)
            args = args.replace("const Value.absent()", "editingEntry.patrucheettuEn")
            args = args.replace("invNumChanged\n              ? finalBillNumber\n              : editingEntry.patrucheettuEn", "finalBillNumber")
            args = args.replace("invNumChanged ? finalBillNumber : editingEntry.patrucheettuEn", "finalBillNumber")
            # For update, we can just do editingEntry.copyWith if we have it, or construct full.
            # But we must include all required fields for PattiyalTharavuru.
            # PattiyalTharavuru requires id, vanakkam, finYear etc.
            # So let's construct a full PattiyalTharavuru using editingEntry's existing values for things not updated.
            return f"""await kalanjiyam.updatePattiyal(
        editingEntry.id,
        PattiyalTharavuru(
          id: editingEntry.id,
          vanakkam: editingEntry.vanakkam,
          finYear: editingEntry.finYear,
          patrucheettuEn: finalBillNumber,
          niruvanamId: state.selectedNiruvanamId,
          vaangunarId: state.selectedVaangunarId,
          vaangunarPeyar: state.selectedVaangunarPeyarMap,
          vaangunarMunvari: state.selectedVaangunarMunvariMap,
          pattiyalVagai: state.pattiyalVagai,
          pattiyalNaal: state.pattiyalNaal,
          tharavugal: PattiyalUthavigal.pattuListToJson(validItems),
          mothaThogai: totals.mothaMothangal,
          thallupadi: totals.thallupadiMothangal,
          variThogai: totals.variMothangal,
          variTharavugal: jsonEncode(totals.variToJson()),
          sonthaViruppangal: settingsJson,
          createdAt: editingEntry.createdAt,
          updatedAt: DateTime.now(),
          isDeleted: editingEntry.isDeleted,
          deletedAt: editingEntry.deletedAt,
          mothaEdai: editingEntry.mothaEdai,
          setharamGrams: editingEntry.setharamGrams,
          thabaalThogai: editingEntry.thabaalThogai,
          ahimsaPattuThogai: editingEntry.ahimsaPattuThogai,
          piravariVugal: editingEntry.piravariVugal,
          nibandhanaigal: editingEntry.nibandhanaigal,
          ullkurippu: editingEntry.ullkurippu,
          vangiTharavugal: editingEntry.vangiTharavugal,
        ),
      );"""
        content = re.sub(update_regex, update_replacer, content, flags=re.DOTALL)

        # Replace the `createPattiyal` call block
        insert_regex = r"await kalanjiyam\.createPattiyal\(\s*PattiyalTharavuru\.insert\((.*?)\),\s*\);"
        def insert_replacer(match):
            args = match.group(1)
            # Remove `Value(...)` wrappers
            args = re.sub(r"Value\((.*?)\)", r"\1", args)
            return f"""await kalanjiyam.createPattiyal(
        PattiyalTharavuru(
          id: 0,
          seyaliVagai: '{mode}',
          patrucheettuEn: finalBillNumber,
          finYear: finYear,
          vanakkam: vanakkam,
          niruvanamId: state.selectedNiruvanamId,
          vaangunarId: state.selectedVaangunarId,
          vaangunarPeyar: state.selectedVaangunarPeyarMap,
          vaangunarMunvari: state.selectedVaangunarMunvariMap,
          pattiyalVagai: state.pattiyalVagai,
          pattiyalNaal: state.pattiyalNaal,
          tharavugal: PattiyalUthavigal.pattuListToJson(validItems),
          mothaThogai: totals.mothaMothangal,
          thallupadi: totals.thallupadiMothangal,
          variThogai: totals.variMothangal,
          variTharavugal: jsonEncode(totals.variToJson()),
          sonthaViruppangal: settingsJson,
          createdAt: DateTime.now(),
          updatedAt: DateTime.now(),
          isDeleted: false,
          mothaEdai: 0.0,
          setharamGrams: 0.0,
          thabaalThogai: 0.0,
          ahimsaPattuThogai: 0.0,
          piravariVugal: '[]',
          nibandhanaigal: '',
          ullkurippu: '',
          vangiTharavugal: '{{}}',
        ),
      );"""
        content = re.sub(insert_regex, insert_replacer, content, flags=re.DOTALL)

        with open(udhavi_file, "w", encoding="utf-8") as f:
            f.write(content)

# 2. Fix PatruThiruthi
patru_thiruthi = os.path.join(base_dir, "src", "cheyalpaadugal", "niril_podhu", "kaatchi", "thiruthi", "patru_thiruthi.dart")
if os.path.exists(patru_thiruthi):
    with open(patru_thiruthi, "r", encoding="utf-8") as f:
        content = f.read()

    # PatruThiruthi constructs `PatrugalTharavuru` instead of `PatrugalTableCompanion` because of fix_ui_types.py
    # We need to strip `Value()` wrappers and use a full constructor for PatrugalTharavuru.
    # The code looks like: `PatrugalTharavuru( id: Value(widget.patru!.id), ...)`
    
    update_regex_patru = r"PatrugalTharavuru\((.*?)\)"
    
    def patru_replacer(match):
        args = match.group(1)
        if "id: 0" in args or "id: widget.patru!.id" in args:
            return match.group(0) # Already fixed?
        if "niruvanamId" not in args:
            return match.group(0)
            
        args = re.sub(r"Value\((.*?)\)", r"\1", args)
        args = args.replace("const Value.absent()", "''") # simplified

        return f"""PatrugalTharavuru(
          id: widget.patru?.id ?? 0,
          niruvanamId: _niruvanamId,
          vaangunarId: _selectedVaangunarId,
          vaangunarPeyar: _selectedVaangunarPeyar,
          vaangunarMunvari: _selectedVaangunarMunvari,
          patruEn: _receiptEnController.text,
          vanakkam: widget.patru?.vanakkam ?? 0,
          finYear: widget.patru?.finYear ?? DateTime.now().year,
          patruNaal: _patruNaal,
          thogai: thogai,
          seluthumMurai: _paymentMethod,
          vangiPeyar: _vangiPeyar,
          parivarthanaiEn: _suttruEnController.text,
          ullkurippu: _ullkurippuController.text,
          createdAt: widget.patru?.createdAt ?? DateTime.now(),
          updatedAt: DateTime.now(),
          isDeleted: widget.patru?.isDeleted ?? false,
        )"""

    # We might have `PatrugalTharavuru.insert` due to earlier script or just `PatrugalTharavuru(`
    # Let's just do a specific replace for the block where kalanjiyam.savePatru is called.
    # Actually, in kalanjiyam.savePatru(_isEditing ? widget.patru!.id : null, ...) 
    
    # Let's read the file and manually fix the save portion.
    # Since regex is risky for large blocks without clear bounds.

with open(os.path.join(base_dir, "scratch", "fix_patru_manual.dart"), "w") as f:
    f.write("Need manual fix for patru_thiruthi if not covered.")

print("Udhavi files fixed.")
