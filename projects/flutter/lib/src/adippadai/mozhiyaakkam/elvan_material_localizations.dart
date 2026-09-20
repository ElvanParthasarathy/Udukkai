import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'k.dart';
import 'mozhi_vazhanguthi.dart';

class ElvanMaterialLocalizationsDelegate extends LocalizationsDelegate<MaterialLocalizations> {
  const ElvanMaterialLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) => ['en', 'ta'].contains(locale.languageCode);

  @override
  Future<MaterialLocalizations> load(Locale locale) async {
    final String langCode = _getLangCode(locale);
    final MaterialLocalizations defaultLocalizations = await GlobalMaterialLocalizations.delegate.load(locale);
    return ElvanMaterialLocalizations(langCode, defaultLocalizations);
  }

  @override
  bool shouldReload(ElvanMaterialLocalizationsDelegate old) => false;

  String _getLangCode(Locale locale) {
    if (locale.languageCode == 'ta') {
      if (locale.scriptCode == 'Latn') return 'ta-Latn';
      return 'ta';
    }
    return 'en';
  }
}

class ElvanMaterialLocalizations implements MaterialLocalizations {
  final String langCode;
  final MaterialLocalizations defaultLoc;

  ElvanMaterialLocalizations(this.langCode, this.defaultLoc);

  @override
  String get openAppDrawerTooltip => defaultLoc.openAppDrawerTooltip;

  @override
  String get backButtonTooltip => defaultLoc.backButtonTooltip;

  @override
  String get clearButtonTooltip => defaultLoc.clearButtonTooltip;

  @override
  String get closeButtonTooltip => defaultLoc.closeButtonTooltip;

  @override
  String get deleteButtonTooltip => defaultLoc.deleteButtonTooltip;

  @override
  String get moreButtonTooltip => defaultLoc.moreButtonTooltip;

  @override
  String get nextMonthTooltip => defaultLoc.nextMonthTooltip;

  @override
  String get previousMonthTooltip => defaultLoc.previousMonthTooltip;

  @override
  String get firstPageTooltip => defaultLoc.firstPageTooltip;

  @override
  String get lastPageTooltip => defaultLoc.lastPageTooltip;

  @override
  String get nextPageTooltip => defaultLoc.nextPageTooltip;

  @override
  String get previousPageTooltip => defaultLoc.previousPageTooltip;

  @override
  String get showMenuTooltip => defaultLoc.showMenuTooltip;

  @override
  String aboutListTileTitle(String applicationName) => defaultLoc.aboutListTileTitle(applicationName);

  @override
  String get licensesPageTitle => defaultLoc.licensesPageTitle;

  @override
  String licensesPackageDetailText(int licenseCount) => defaultLoc.licensesPackageDetailText(licenseCount);

  @override
  String pageRowsInfoTitle(int firstRow, int lastRow, int rowCount, bool rowCountIsApproximate) => defaultLoc.pageRowsInfoTitle(firstRow, lastRow, rowCount, rowCountIsApproximate);

  @override
  String get rowsPerPageTitle => defaultLoc.rowsPerPageTitle;

  @override
  String tabLabel({required int tabIndex, required int tabCount}) => defaultLoc.tabLabel(tabIndex: tabIndex, tabCount: tabCount);

  @override
  String selectedRowCountTitle(int selectedRowCount) => defaultLoc.selectedRowCountTitle(selectedRowCount);

  @override
  String get cancelButtonLabel => K.kaividuPtn.trWithLang(langCode);

  @override
  String get closeButtonLabel => K.kaividuPtn.trWithLang(langCode);

  @override
  String get continueButtonLabel => K.thodarPtn.trWithLang(langCode);

  @override
  String get copyButtonLabel => defaultLoc.copyButtonLabel;

  @override
  String get cutButtonLabel => defaultLoc.cutButtonLabel;

  @override
  String get scanTextButtonLabel => defaultLoc.scanTextButtonLabel;

  @override
  String get okButtonLabel => K.chariPtn.trWithLang(langCode);

  @override
  String get pasteButtonLabel => defaultLoc.pasteButtonLabel;

  @override
  String get selectAllButtonLabel => K.anaithaiyumTheriPtn.trWithLang(langCode);

  @override
  String get lookUpButtonLabel => defaultLoc.lookUpButtonLabel;

  @override
  String get searchWebButtonLabel => defaultLoc.searchWebButtonLabel;

  @override
  String get shareButtonLabel => defaultLoc.shareButtonLabel;

  @override
  String get viewLicensesButtonLabel => defaultLoc.viewLicensesButtonLabel;

  @override
  String get anteMeridiemAbbreviation => K.murpagal.trWithLang(langCode);

  @override
  String get postMeridiemAbbreviation => K.pirpagal.trWithLang(langCode);

  @override
  String get timePickerHourModeAnnouncement => defaultLoc.timePickerHourModeAnnouncement;

  @override
  String get timePickerMinuteModeAnnouncement => defaultLoc.timePickerMinuteModeAnnouncement;

  @override
  String get modalBarrierDismissLabel => defaultLoc.modalBarrierDismissLabel;

  @override
  String get menuDismissLabel => defaultLoc.menuDismissLabel;

  @override
  String get drawerLabel => defaultLoc.drawerLabel;

  @override
  String get popupMenuLabel => defaultLoc.popupMenuLabel;

  @override
  String get menuBarMenuLabel => defaultLoc.menuBarMenuLabel;

  @override
  String get dialogLabel => defaultLoc.dialogLabel;

  @override
  String get alertDialogLabel => defaultLoc.alertDialogLabel;

  @override
  String get searchFieldLabel => K.thaeduga.trWithLang(langCode);

  @override
  String get currentDateLabel => defaultLoc.currentDateLabel;

  @override
  String get selectedDateLabel => defaultLoc.selectedDateLabel;

  @override
  String get scrimLabel => defaultLoc.scrimLabel;

  @override
  String get bottomSheetLabel => defaultLoc.bottomSheetLabel;

  @override
  String scrimOnTapHint(String modalRouteContentName) => defaultLoc.scrimOnTapHint(modalRouteContentName);

  @override
  TimeOfDayFormat timeOfDayFormat({bool alwaysUse24HourFormat = false}) => defaultLoc.timeOfDayFormat(alwaysUse24HourFormat: alwaysUse24HourFormat);

  @override
  ScriptCategory get scriptCategory => defaultLoc.scriptCategory;

  @override
  String formatDecimal(int number) => defaultLoc.formatDecimal(number);

  @override
  String formatHour(TimeOfDay timeOfDay, {bool alwaysUse24HourFormat = false}) => defaultLoc.formatHour(timeOfDay, alwaysUse24HourFormat: alwaysUse24HourFormat);

  @override
  String formatMinute(TimeOfDay timeOfDay) => defaultLoc.formatMinute(timeOfDay);

  @override
  String formatTimeOfDay(TimeOfDay timeOfDay, {bool alwaysUse24HourFormat = false}) => defaultLoc.formatTimeOfDay(timeOfDay, alwaysUse24HourFormat: alwaysUse24HourFormat);

  @override
  String formatYear(DateTime date) => defaultLoc.formatYear(date);

  @override
  String formatCompactDate(DateTime date) => defaultLoc.formatCompactDate(date);

  @override
  String formatShortDate(DateTime date) => defaultLoc.formatShortDate(date);

  @override
  String formatMediumDate(DateTime date) {
    if (langCode == 'en') return defaultLoc.formatMediumDate(date);
    return '${date.day} ${shortMonths[date.month - 1]}';
  }

  @override
  String formatFullDate(DateTime date) {
    if (langCode == 'en') return defaultLoc.formatFullDate(date);
    final weekday = shortWeekdays[date.weekday % 7];
    return '$weekday, ${date.day} ${monthsOfYear[date.month - 1]}, ${date.year}';
  }

  @override
  String formatMonthYear(DateTime date) {
    if (langCode == 'en') return defaultLoc.formatMonthYear(date);
    return '${monthsOfYear[date.month - 1]} ${date.year}';
  }

  @override
  String formatShortMonthDay(DateTime date) {
    if (langCode == 'en') return defaultLoc.formatShortMonthDay(date);
    return '${date.day} ${shortMonths[date.month - 1]}';
  }

  @override
  DateTime? parseCompactDate(String? inputString) => defaultLoc.parseCompactDate(inputString);

  @override
  List<String> get narrowWeekdays {
    if (langCode == 'ta') {
      return ['ஞா', 'தி', 'செ', 'அ', 'வி', 'வெ', 'கா'];
    } else if (langCode == 'ta-Latn') {
      return ['Ny', 'Th', 'Ch', 'Ar', 'Vi', 'Ve', 'Ka'];
    }
    return ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
  }

  @override
  int get firstDayOfWeekIndex => defaultLoc.firstDayOfWeekIndex;

  @override
  String get dateSeparator => defaultLoc.dateSeparator;

  @override
  String get dateHelpText => '';

  @override
  String get selectYearSemanticsLabel => defaultLoc.selectYearSemanticsLabel;

  @override
  String get unspecifiedDate => defaultLoc.unspecifiedDate;

  @override
  String get unspecifiedDateRange => defaultLoc.unspecifiedDateRange;

  @override
  String get dateInputLabel => '';

  @override
  String get dateRangeStartLabel => defaultLoc.dateRangeStartLabel;

  @override
  String get dateRangeEndLabel => defaultLoc.dateRangeEndLabel;

  @override
  String dateRangeStartDateSemanticLabel(String formattedDate) => defaultLoc.dateRangeStartDateSemanticLabel(formattedDate);

  @override
  String dateRangeEndDateSemanticLabel(String formattedDate) => defaultLoc.dateRangeEndDateSemanticLabel(formattedDate);

  @override
  String get invalidDateFormatLabel => defaultLoc.invalidDateFormatLabel;

  @override
  String get invalidDateRangeLabel => defaultLoc.invalidDateRangeLabel;

  @override
  String get dateOutOfRangeLabel => defaultLoc.dateOutOfRangeLabel;

  @override
  String get saveButtonLabel => K.chaemiPtn.trWithLang(langCode);

  @override
  String get datePickerHelpText => '';

  @override
  String get dateRangePickerHelpText => defaultLoc.dateRangePickerHelpText;

  @override
  String get calendarModeButtonLabel => defaultLoc.calendarModeButtonLabel;

  @override
  String get inputDateModeButtonLabel => defaultLoc.inputDateModeButtonLabel;

  @override
  String get timePickerDialHelpText => defaultLoc.timePickerDialHelpText;

  @override
  String get timePickerInputHelpText => defaultLoc.timePickerInputHelpText;

  @override
  String get timePickerHourLabel => defaultLoc.timePickerHourLabel;

  @override
  String get timePickerMinuteLabel => defaultLoc.timePickerMinuteLabel;

  @override
  String get invalidTimeLabel => defaultLoc.invalidTimeLabel;

  @override
  String get dialModeButtonLabel => defaultLoc.dialModeButtonLabel;

  @override
  String get inputTimeModeButtonLabel => defaultLoc.inputTimeModeButtonLabel;

  @override
  String get signedInLabel => defaultLoc.signedInLabel;

  @override
  String get hideAccountsLabel => defaultLoc.hideAccountsLabel;

  @override
  String get showAccountsLabel => defaultLoc.showAccountsLabel;

  @override
  String get reorderItemToStart => defaultLoc.reorderItemToStart;

  @override
  String get reorderItemToEnd => defaultLoc.reorderItemToEnd;

  @override
  String get reorderItemUp => defaultLoc.reorderItemUp;

  @override
  String get reorderItemDown => defaultLoc.reorderItemDown;

  @override
  String get reorderItemLeft => defaultLoc.reorderItemLeft;

  @override
  String get reorderItemRight => defaultLoc.reorderItemRight;

  @override
  String get expandedIconTapHint => defaultLoc.expandedIconTapHint;

  @override
  String get collapsedIconTapHint => defaultLoc.collapsedIconTapHint;

  @override
  String get expansionTileExpandedHint => defaultLoc.expansionTileExpandedHint;

  @override
  String get expansionTileCollapsedHint => defaultLoc.expansionTileCollapsedHint;

  @override
  String get expansionTileExpandedTapHint => defaultLoc.expansionTileExpandedTapHint;

  @override
  String get expansionTileCollapsedTapHint => defaultLoc.expansionTileCollapsedTapHint;

  @override
  String get expandedHint => defaultLoc.expandedHint;

  @override
  String get collapsedHint => defaultLoc.collapsedHint;

  @override
  String remainingTextFieldCharacterCount(int remaining) => defaultLoc.remainingTextFieldCharacterCount(remaining);

  @override
  String get refreshIndicatorSemanticLabel => defaultLoc.refreshIndicatorSemanticLabel;

  @override
  String get keyboardKeyAlt => defaultLoc.keyboardKeyAlt;

  @override
  String get keyboardKeyAltGraph => defaultLoc.keyboardKeyAltGraph;

  @override
  String get keyboardKeyBackspace => defaultLoc.keyboardKeyBackspace;

  @override
  String get keyboardKeyCapsLock => defaultLoc.keyboardKeyCapsLock;

  @override
  String get keyboardKeyChannelDown => defaultLoc.keyboardKeyChannelDown;

  @override
  String get keyboardKeyChannelUp => defaultLoc.keyboardKeyChannelUp;

  @override
  String get keyboardKeyControl => defaultLoc.keyboardKeyControl;

  @override
  String get keyboardKeyDelete => defaultLoc.keyboardKeyDelete;

  @override
  String get keyboardKeyEject => defaultLoc.keyboardKeyEject;

  @override
  String get keyboardKeyEnd => defaultLoc.keyboardKeyEnd;

  @override
  String get keyboardKeyEscape => defaultLoc.keyboardKeyEscape;

  @override
  String get keyboardKeyFn => defaultLoc.keyboardKeyFn;

  @override
  String get keyboardKeyHome => defaultLoc.keyboardKeyHome;

  @override
  String get keyboardKeyInsert => defaultLoc.keyboardKeyInsert;

  @override
  String get keyboardKeyMeta => defaultLoc.keyboardKeyMeta;

  @override
  String get keyboardKeyMetaMacOs => defaultLoc.keyboardKeyMetaMacOs;

  @override
  String get keyboardKeyMetaWindows => defaultLoc.keyboardKeyMetaWindows;

  @override
  String get keyboardKeyNumLock => defaultLoc.keyboardKeyNumLock;

  @override
  String get keyboardKeyNumpad1 => defaultLoc.keyboardKeyNumpad1;

  @override
  String get keyboardKeyNumpad2 => defaultLoc.keyboardKeyNumpad2;

  @override
  String get keyboardKeyNumpad3 => defaultLoc.keyboardKeyNumpad3;

  @override
  String get keyboardKeyNumpad4 => defaultLoc.keyboardKeyNumpad4;

  @override
  String get keyboardKeyNumpad5 => defaultLoc.keyboardKeyNumpad5;

  @override
  String get keyboardKeyNumpad6 => defaultLoc.keyboardKeyNumpad6;

  @override
  String get keyboardKeyNumpad7 => defaultLoc.keyboardKeyNumpad7;

  @override
  String get keyboardKeyNumpad8 => defaultLoc.keyboardKeyNumpad8;

  @override
  String get keyboardKeyNumpad9 => defaultLoc.keyboardKeyNumpad9;

  @override
  String get keyboardKeyNumpad0 => defaultLoc.keyboardKeyNumpad0;

  @override
  String get keyboardKeyNumpadAdd => defaultLoc.keyboardKeyNumpadAdd;

  @override
  String get keyboardKeyNumpadComma => defaultLoc.keyboardKeyNumpadComma;

  @override
  String get keyboardKeyNumpadDecimal => defaultLoc.keyboardKeyNumpadDecimal;

  @override
  String get keyboardKeyNumpadDivide => defaultLoc.keyboardKeyNumpadDivide;

  @override
  String get keyboardKeyNumpadEnter => defaultLoc.keyboardKeyNumpadEnter;

  @override
  String get keyboardKeyNumpadEqual => defaultLoc.keyboardKeyNumpadEqual;

  @override
  String get keyboardKeyNumpadMultiply => defaultLoc.keyboardKeyNumpadMultiply;

  @override
  String get keyboardKeyNumpadParenLeft => defaultLoc.keyboardKeyNumpadParenLeft;

  @override
  String get keyboardKeyNumpadParenRight => defaultLoc.keyboardKeyNumpadParenRight;

  @override
  String get keyboardKeyNumpadSubtract => defaultLoc.keyboardKeyNumpadSubtract;

  @override
  String get keyboardKeyPageDown => defaultLoc.keyboardKeyPageDown;

  @override
  String get keyboardKeyPageUp => defaultLoc.keyboardKeyPageUp;

  @override
  String get keyboardKeyPower => defaultLoc.keyboardKeyPower;

  @override
  String get keyboardKeyPowerOff => defaultLoc.keyboardKeyPowerOff;

  @override
  String get keyboardKeyPrintScreen => defaultLoc.keyboardKeyPrintScreen;

  @override
  String get keyboardKeyScrollLock => defaultLoc.keyboardKeyScrollLock;

  @override
  String get keyboardKeySelect => defaultLoc.keyboardKeySelect;

  @override
  String get keyboardKeyShift => defaultLoc.keyboardKeyShift;

  @override
  String get keyboardKeySpace => defaultLoc.keyboardKeySpace;

  List<String> get shortMonths => [
    K.janKurugiya.trWithLang(langCode),
    K.fibKurugiya.trWithLang(langCode),
    K.maarKurugiya.trWithLang(langCode),
    K.aepKurugiya.trWithLang(langCode),
    K.maeKurugiya.trWithLang(langCode),
    K.joonKurugiya.trWithLang(langCode),
    K.joolKurugiya.trWithLang(langCode),
    K.aagKurugiya.trWithLang(langCode),
    K.sepKurugiya.trWithLang(langCode),
    K.akKurugiya.trWithLang(langCode),
    K.navKurugiya.trWithLang(langCode),
    K.disKurugiya.trWithLang(langCode),
  ];

  List<String> get monthsOfYear => [
    K.janavari.trWithLang(langCode),
    K.fibravari.trWithLang(langCode),
    K.maarch.trWithLang(langCode),
    K.aepral.trWithLang(langCode),
    K.mae.trWithLang(langCode),
    K.joon.trWithLang(langCode),
    K.joolai.trWithLang(langCode),
    K.aagast.trWithLang(langCode),
    K.septambar.trWithLang(langCode),
    K.aktobar.trWithLang(langCode),
    K.navambar.trWithLang(langCode),
    K.disambar.trWithLang(langCode),
  ];

  List<String> get shortWeekdays => [
    K.nyaayiru.trWithLang(langCode),
    K.thingal.trWithLang(langCode),
    K.chevvaay.trWithLang(langCode),
    K.arivan.trWithLang(langCode),
    K.viyaazhan.trWithLang(langCode),
    K.velli.trWithLang(langCode),
    K.kaari.trWithLang(langCode),
  ];

}
