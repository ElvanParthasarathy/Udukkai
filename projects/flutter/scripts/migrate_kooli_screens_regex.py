import os
import re

base_dir = r"d:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_kooli\kaatchi\thiraigal"

files = [
    {
        "name": "kooli_pattiyalgal_thirai.dart",
        "imports": "import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';",
        "replacement": """  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(coolieInvoicesSearchQueryProvider).toLowerCase();
    final pattiyalgalAsync = ref.watch(pattiyalgalProvider);
    final profilesAsync = ref.watch(kooliNiruvanaTharavugalListProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final primaryLang = ref.watch(kooliAchuMozhiProvider);
    final secondaryLang = primaryLang == 'Tamil' ? 'English' : 'Tamil';

    return ElvanKooliTharavuPattiyal<PattiyalTharavuru>(
      dataAsync: pattiyalgalAsync,
      searchQuery: query,
      onFilter: (p, q) {
        final en = p.patrucheettuEn.toLowerCase();
        final peyarPrimary = (p.vaangunarPeyar[primaryLang] ?? '').toLowerCase();
        final peyarSecondary = (p.vaangunarPeyar[secondaryLang] ?? '').toLowerCase();
        return en.contains(q) || 
               peyarPrimary.contains(q) || 
               peyarSecondary.contains(q);
      },
      emptyIcon: CupertinoIcons.doc_text,
      emptyTitle: K.pattiyalgalIllai.tr(context, ref),
      emptySubtitle: K.pudhiyaPattiyalPtn.tr(context, ref),
      itemId: (p) => p.id,
      selectionModeProvider: pattiyalSelectionModeProvider,
      selectedIdsProvider: selectedPattiyalIdsProvider,
      onItemTap: (context, p) {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => CoolieInvoiceEditor(
              editingEntry: p,
            ),
          ),
        );
      },
      groupBy: (p) => p.niruvanamId,
      groupHeaderBuilder: (context, niruvanamId) {
        final profiles = profilesAsync.valueOrNull ?? [];
        String sectionName;
        if (niruvanamId == null) {
          sectionName = 'General';
        } else {
          final profile = profiles
              .where((p) => p.id == niruvanamId)
              .firstOrNull;
          sectionName = profile?.kurumPeyar ?? 'General';
        }
        return SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.only(top: 16, bottom: 8),
            child: Text(
              sectionName,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                letterSpacing: 0.5,
                color: isDark
                    ? Colors.white.withOpacity(0.5)
                    : Colors.black.withOpacity(0.4),
              ),
            ),
          ),
        );
      },
      cardBuilder: (context, ref, p, index, isSelecting, isSelected, onTap, onLongPress) {
        return _CooliePatrucheettuCard(
          index: index,
          pattiyal: p,
          isDark: isDark,
          isSelecting: isSelecting,
          isSelected: isSelected,
          dateFormat: _dateFormat,
          currencyFormat: _currencyFormat,
          primaryLang: primaryLang,
          secondaryLang: secondaryLang,
          onTap: onTap,
          onLongPress: onLongPress,
        );
      },
    );
  }
}"""
    },
    {
        "name": "kooli_patrucheettugal_thirai.dart",
        "imports": "import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';",
        "replacement": """  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(coolieReceiptsSearchQueryProvider).toLowerCase();
    final patrugalAsync = ref.watch(patrugalProvider);
    final profilesAsync = ref.watch(kooliNiruvanaTharavugalListProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final primaryLang = ref.watch(kooliAchuMozhiProvider);
    final secondaryLang = primaryLang == 'Tamil' ? 'English' : 'Tamil';

    return ElvanKooliTharavuPattiyal<PatrugalTharavuru>(
      dataAsync: patrugalAsync,
      searchQuery: query,
      onFilter: (p, q) {
        final en = p.patruEn.toLowerCase();
        final peyarPrimary = (p.vaangunarPeyar[primaryLang] ?? '').toLowerCase();
        final peyarSecondary = (p.vaangunarPeyar[secondaryLang] ?? '').toLowerCase();
        final vagai = p.seluthumMurai.toLowerCase();
        return en.contains(q) ||
            peyarPrimary.contains(q) ||
            peyarSecondary.contains(q) ||
            vagai.contains(q);
      },
      emptyIcon: CupertinoIcons.doc_text,
      emptyTitle: K.patrucheettugalIllai.tr(context, ref),
      emptySubtitle: K.pudhiyaPatrucheettuPtn.tr(context, ref),
      itemId: (p) => p.id,
      selectionModeProvider: patruSelectionModeProvider,
      selectedIdsProvider: selectedPatruIdsProvider,
      onItemTap: (context, p) {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => CoolieReceiptEditor(editingEntry: p),
          ),
        );
      },
      groupBy: (p) => p.niruvanamId,
      groupHeaderBuilder: (context, niruvanamId) {
        final profiles = profilesAsync.valueOrNull ?? [];
        String sectionName;
        if (niruvanamId == null) {
          sectionName = 'General';
        } else {
          final profile = profiles
              .where((p) => p.id == niruvanamId)
              .firstOrNull;
          sectionName = profile?.kurumPeyar ?? 'General';
        }
        return SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.only(top: 16, bottom: 8),
            child: Text(
              sectionName,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                letterSpacing: 0.5,
                color: isDark
                    ? Colors.white.withOpacity(0.5)
                    : Colors.black.withOpacity(0.4),
              ),
            ),
          ),
        );
      },
      cardBuilder: (context, ref, p, index, isSelecting, isSelected, onTap, onLongPress) {
        return Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: _PatruCard(
            patru: p,
            isDark: isDark,
            isSelecting: isSelecting,
            isSelected: isSelected,
            dateFormat: _dateFormat,
            currencyFormat: _currencyFormat,
            primaryLang: primaryLang,
            secondaryLang: secondaryLang,
            onTap: onTap,
            onLongPress: onLongPress,
          ),
        );
      },
    );
  }
}"""
    },
    {
        "name": "kooli_porutkal_thirai.dart",
        "imports": "import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';",
        "replacement": """  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(coolieItemsSearchQueryProvider).toLowerCase();
    final porulgalAsync = ref.watch(porulgalProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanKooliTharavuPattiyal<PorulTharavuru>(
      dataAsync: porulgalAsync,
      searchQuery: query,
      onFilter: (p, q) {
        final primary = (p.porulPeyar[primaryLang] ?? '').toLowerCase();
        final secondary = (p.porulPeyar[secondaryLang] ?? '').toLowerCase();
        return primary.contains(q) || secondary.contains(q);
      },
      emptyIcon: CupertinoIcons.cube_box,
      emptyTitle: K.porulgalIllai.tr(context, ref),
      emptySubtitle: K.porulaiChaerkkavum.tr(context, ref),
      itemId: (p) => p.id,
      selectionModeProvider: porulSelectionModeProvider,
      selectedIdsProvider: selectedPorulIdsProvider,
      onItemTap: (context, p) {
        NirilNav.push(
          context,
          CoolieItemEditor(product: p),
        );
      },
      childAspectRatio: 3.5,
      mobileItemHeight: 72,
      cardBuilder: (context, ref, p, index, isSelecting, isSelected, onTap, onLongPress) {
        return _CooliePorulCard(
          index: index,
          porul: p,
          primaryLang: primaryLang,
          secondaryLang: secondaryLang,
          isDark: isDark,
          isSelecting: isSelecting,
          isSelected: isSelected,
          onTap: onTap,
          onLongPress: onLongPress,
        );
      },
    );
  }
}"""
    },
    {
        "name": "kooli_vaangunargal_thirai.dart",
        "imports": "import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';",
        "replacement": """  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(coolieMerchantsSearchQueryProvider).toLowerCase();
    final vaangunargalAsync = ref.watch(vaangunargalProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanKooliTharavuPattiyal<VaangunarTharavuru>(
      dataAsync: vaangunargalAsync,
      searchQuery: query,
      onFilter: (v, q) {
        final pName = (v.peyar[primaryLang] ?? '').toLowerCase();
        final sName = (v.peyar[secondaryLang] ?? '').toLowerCase();
        final pCity = (v.oor[primaryLang] ?? '').toLowerCase();
        final sCity = (v.oor[secondaryLang] ?? '').toLowerCase();
        return pName.contains(q) ||
            sName.contains(q) ||
            pCity.contains(q) ||
            sCity.contains(q);
      },
      emptyIcon: CupertinoIcons.person_3,
      emptyTitle: K.vaangunargalIllai.tr(context, ref),
      emptySubtitle: K.vaangunaraiChaerkkavum.tr(context, ref),
      itemId: (v) => v.id,
      selectionModeProvider: vaangunarSelectionModeProvider,
      selectedIdsProvider: selectedVaangunarIdsProvider,
      onItemTap: (context, v) {
        NirilNav.push(
          context,
          CoolieMerchantEditor(vaangunar: v),
        );
      },
      childAspectRatio: 3.5,
      mobileItemHeight: 80,
      cardBuilder: (context, ref, v, index, isSelecting, isSelected, onTap, onLongPress) {
        return _CoolieVaangunarCard(
          index: index,
          vaangunar: v,
          primaryLang: primaryLang,
          secondaryLang: secondaryLang,
          isDark: isDark,
          isSelecting: isSelecting,
          isSelected: isSelected,
          onTap: onTap,
          onLongPress: onLongPress,
        );
      },
    );
  }
}"""
    }
]

for file_info in files:
    path = os.path.join(base_dir, file_info["name"])
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find where to add the import
    if "import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';" not in content:
        # Add import after the last import statement
        lines = content.splitlines()
        last_import = 0
        for i, line in enumerate(lines):
            if line.startswith("import "):
                last_import = i
        lines.insert(last_import + 1, file_info["imports"])
        content = "\\n".join(lines) + "\\n"

    # We want to replace the `build` method inside the stateful/stateless widget up to the closing brace.
    # We will use regex to find the start of `Widget build` and the end of `_toggleSelection` method or just the class closing brace.
    
    # Let's match from `  @override\n  Widget build...` all the way to `\n\n}` (or equivalent) before the `// ──` comments.
    
    pattern = r"  @override\s+Widget build\(BuildContext context, WidgetRef ref\) \{.*?\n\}"
    
    # Wait, the class also contains `void _toggleSelection(...) { ... }` which we need to remove.
    # The class ends right before `// ──` or similar. Let's find the closing brace of the class.
    
    match = re.search(r"  @override\s+Widget build\(BuildContext context(?:, WidgetRef ref)?\).*?\n\}(?=\s*// ──|\s*class)", content, re.DOTALL)
    
    if match:
        before = content[:match.start()]
        after = content[match.end():]
        
        new_content = before + file_info["replacement"] + after
        new_content = new_content.replace('withOpacity(', 'withValues(alpha: ')

        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {file_info['name']}")
    else:
        print(f"Failed to find block via regex in {file_info['name']}")
        
