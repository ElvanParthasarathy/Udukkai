import 'package:flutter_riverpod/legacy.dart';

// ─────────────────────────────────────────────────────────────────────────────
// INDEPENDENT SEARCH STATES
// ─────────────────────────────────────────────────────────────────────────────
// Each domain and mode has its own dedicated search query state so that
// typing in one search bar does not accidentally filter another list.

// Coolie Providers
final coolieMerchantsSearchQueryProvider = StateProvider<String>((ref) => '');
final coolieItemsSearchQueryProvider = StateProvider<String>((ref) => '');
final coolieInvoicesSearchQueryProvider = StateProvider<String>((ref) => '');
final coolieReceiptsSearchQueryProvider = StateProvider<String>((ref) => '');

// Silk Providers
final silkMerchantsSearchQueryProvider = StateProvider<String>((ref) => '');
final silkItemsSearchQueryProvider = StateProvider<String>((ref) => '');
final silkInvoicesSearchQueryProvider = StateProvider<String>((ref) => '');
final silkReceiptsSearchQueryProvider = StateProvider<String>((ref) => '');
