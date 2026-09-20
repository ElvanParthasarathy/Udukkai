import 'package:flutter_riverpod/legacy.dart';

/// A global provider to synchronize the expanded/collapsed state of the One UI header.
/// This allows independent ScrollViews (tabs or subpages) to start in the same
/// header state as the previously viewed page.
final headerExpandedProvider = StateProvider<bool>((ref) => true);
