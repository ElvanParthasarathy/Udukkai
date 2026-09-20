import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'navigation_destination.dart';
import '../../cheyalpaadugal/niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../cheyalpaadugal/niril_podhu/kalanjiyam/patru_nilaimai.dart';
import '../../cheyalpaadugal/niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import '../../cheyalpaadugal/niril_podhu/kalanjiyam/porul_nilaimai.dart';

/// The single source of truth for all navigation state in the app.
///
/// This replaces:
/// - `_currentTab` (int) in ShellDemoScreen
/// - `_isSettingsOpen`, `_isReportsOpen`, `_isGstReturnsOpen` (3 bools)
/// - `uruvakkuSegmentProvider` (int)
/// - All 8 search query providers
///
/// Both mobile and desktop layouts read from [nirilNavigationProvider]
/// and render accordingly. No more disconnected state.
class NirilNavigationState {
  final NirilDestination destination;
  final NirilDestination _lastPrimaryTab;
  final String searchQuery;

  const NirilNavigationState({
    this.destination = NirilDestination.mugappu,
    NirilDestination? lastPrimaryTab,
    this.searchQuery = '',
  }) : _lastPrimaryTab = lastPrimaryTab ?? NirilDestination.mugappu;

  /// The last primary tab before switching to a custom view.
  /// Used by [goBack] to return to the correct tab.
  NirilDestination get lastPrimaryTab => _lastPrimaryTab;

  /// Whether we are on a primary tab (not settings/reports/gst).
  bool get isOnPrimaryTab => destination.isPrimaryTab;

  /// Whether we are on a custom view (settings, reports, GST).
  /// Used by desktop layout for state-driven content replacement.
  bool get isCustomView => destination.isCustomView;

  /// The mobile tab index for the current (or last) primary tab.
  /// When on a custom view, returns the last primary tab's index.
  int get mobileTabIndex {
    if (destination.isPrimaryTab) {
      return destination.mobileTabIndex;
    }
    return _lastPrimaryTab.mobileTabIndex;
  }

  /// The desktop sidebar index for the current destination.
  /// Returns -1 when on a custom view (settings/reports/gst).
  int get desktopSidebarIndex => destination.desktopSidebarIndex;

  /// The Uruvakku segment (0 = invoices, 1 = receipts).
  int get uruvakkuSegment {
    if (destination == NirilDestination.raseethu) return 1;
    if (_lastPrimaryTab == NirilDestination.raseethu) return 1;
    return 0;
  }

  NirilNavigationState copyWith({
    NirilDestination? destination,
    NirilDestination? lastPrimaryTab,
    String? searchQuery,
  }) {
    return NirilNavigationState(
      destination: destination ?? this.destination,
      lastPrimaryTab: lastPrimaryTab ?? _lastPrimaryTab,
      searchQuery: searchQuery ?? this.searchQuery,
    );
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is NirilNavigationState &&
          runtimeType == other.runtimeType &&
          destination == other.destination &&
          _lastPrimaryTab == other._lastPrimaryTab &&
          searchQuery == other.searchQuery;

  @override
  int get hashCode =>
      destination.hashCode ^ _lastPrimaryTab.hashCode ^ searchQuery.hashCode;
}

/// The navigation notifier that manages all navigation state.
///
/// Usage:
/// ```dart
/// // Read current state
/// final navState = ref.watch(nirilNavigationProvider);
///
/// // Navigate
/// ref.read(nirilNavigationProvider.notifier).goTo(NirilDestination.settings);
/// ```
class NirilNavigationNotifier extends Notifier<NirilNavigationState> {
  @override
  NirilNavigationState build() {
    return const NirilNavigationState();
  }

  /// Navigate to a destination.
  ///
  /// If switching to a custom view (settings/reports/gst), the current
  /// primary tab is saved so [goBack] can return to it.
  /// Search query is reset when switching destinations.
  void goTo(NirilDestination dest) {
    if (dest == state.destination) return;

    _clearSelectionStates();

    if (dest.isCustomView) {
      // Going to a custom view — save the current primary tab
      state = state.copyWith(
        destination: dest,
        lastPrimaryTab:
            state.isOnPrimaryTab ? state.destination : state.lastPrimaryTab,
        searchQuery: '',
      );
    } else {
      // Going to a primary tab
      state = state.copyWith(
        destination: dest,
        lastPrimaryTab: dest,
        searchQuery: '',
      );
    }
  }

  /// Smart back navigation.
  ///
  /// Priority:
  /// 1. If on a custom view → return to last primary tab
  /// 2. If on receipts (mobile uruvakku) → go to invoices
  /// 3. If on any non-home primary tab → go to mugappu (home)
  /// 4. If already on home → allow system pop (return true)
  ///
  /// Returns true if the system should handle the pop (i.e., exit app).
  bool goBack() {
    _clearSelectionStates();

    // 1. Custom view → return to last primary tab
    if (state.isCustomView) {
      state = state.copyWith(
        destination: state.lastPrimaryTab,
        searchQuery: '',
      );
      return false;
    }

    // 2. Receipts → Invoices (mobile uruvakku segment switch)
    if (state.destination == NirilDestination.raseethu) {
      state = state.copyWith(
        destination: NirilDestination.pattiyal,
        lastPrimaryTab: NirilDestination.pattiyal,
        searchQuery: '',
      );
      return false;
    }

    // 3. Non-home tab → Home
    if (state.destination != NirilDestination.mugappu) {
      state = state.copyWith(
        destination: NirilDestination.mugappu,
        lastPrimaryTab: NirilDestination.mugappu,
        searchQuery: '',
      );
      return false;
    }

    // 4. Already on home — let system handle pop
    return true;
  }

  /// Update the search query for the current destination.
  void setSearchQuery(String query) {
    state = state.copyWith(searchQuery: query);
  }

  /// Clear search query.
  void clearSearch() {
    state = state.copyWith(searchQuery: '');
  }

  /// Clears all selection states across all list views when navigating away.
  void _clearSelectionStates() {
    ref.read(pattiyalSelectionModeProvider.notifier).state = false;
    ref.read(selectedPattiyalIdsProvider.notifier).state = {};
    
    ref.read(patruSelectionModeProvider.notifier).state = false;
    ref.read(selectedPatruIdsProvider.notifier).state = {};
    
    ref.read(vaangunarSelectionModeProvider.notifier).state = false;
    ref.read(selectedVaangunarIdsProvider.notifier).state = {};
    
    ref.read(porulSelectionModeProvider.notifier).state = false;
    ref.read(selectedPorulIdsProvider.notifier).state = {};
  }
}

/// The global navigation provider.
///
/// This is the single source of truth for all navigation state.
/// Both mobile and desktop layouts watch this provider.
final nirilNavigationProvider =
    NotifierProvider<NirilNavigationNotifier, NirilNavigationState>(() {
  return NirilNavigationNotifier();
});
