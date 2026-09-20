/// All primary destinations in the Niril app.
///
/// These represent every top-level screen the user can navigate to.
/// Both mobile and desktop layouts read the active destination from
/// [NirilNavigationNotifier] and render accordingly.
enum NirilDestination {
  mugappu, // Home / Dashboard
  pattiyal, // Invoices
  raseethu, // Receipts
  vaangunar, // Merchants
  porul, // Products / Inventory
  settings, // Settings (desktop state-driven only)
  reports, // Reports (desktop state-driven only)
  gstReturns, // GST Returns (desktop state-driven only)
}

/// Editor types that can be opened as subpages.
///
/// These are the only screens that get pushed into a nested navigator
/// (desktop) or the root navigator (mobile). Primary destinations
/// are always state-driven, never pushed.
enum NirilEditor {
  invoice,
  receipt,
  merchant,
  item,
}

/// Extension helpers for [NirilDestination].
extension NirilDestinationX on NirilDestination {
  /// The 5 primary tabs.
  static const primaryTabs = [
    NirilDestination.mugappu,
    NirilDestination.pattiyal,
    NirilDestination.raseethu,
    NirilDestination.porul,
    NirilDestination.vaangunar,
  ];

  /// Whether this destination is a primary tab (not settings/reports/gst).
  bool get isPrimaryTab => primaryTabs.contains(this);

  /// Whether this destination is a custom view (settings, reports, GST).
  /// Only used by the desktop layout for state-driven content replacement.
  bool get isCustomView => !isPrimaryTab;

  /// Maps this destination to the mobile tab index (0-4).
  /// Invoices and Receipts both map to tab 1 (Uruvakku).
  int get mobileTabIndex => switch (this) {
        NirilDestination.mugappu => 0,
        NirilDestination.pattiyal => 1,
        NirilDestination.raseethu => 1,
        NirilDestination.porul => 2,
        NirilDestination.vaangunar => 3,
        // Custom views don't have a tab — return 0 (home) as fallback
        _ => 0,
      };

  /// Maps this destination to the desktop sidebar index (0-5).
  int get desktopSidebarIndex => switch (this) {
        NirilDestination.mugappu => 0,
        NirilDestination.pattiyal => 1,
        NirilDestination.raseethu => 2,
        NirilDestination.porul => 3,
        NirilDestination.vaangunar => 4,
        // Custom views don't have a sidebar item, return -1
        _ => -1,
      };

  /// The Uruvakku segment index for this destination (used by mobile layout).
  /// 0 = Invoices, 1 = Receipts
  int get uruvakkuSegment => this == NirilDestination.raseethu ? 1 : 0;
}
