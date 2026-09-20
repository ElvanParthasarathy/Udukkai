import 'package:flutter/widgets.dart';

/// Centralized utility for managing keyboard focus and scroll behaviors 
/// across the Elvan app. Ensures consistency and reduces code duplication.
class ElvanKavanam {
  ElvanKavanam._(); // Prevent instantiation

  /// Unfocuses the current active input field.
  /// Used primarily before opening bottom sheets or modals to prevent the 
  /// keyboard from glitching or re-appearing unexpectedly when the modal closes.
  static void viduvi(BuildContext context) {
    FocusManager.instance.primaryFocus?.unfocus();
  }

  /// The standard keyboard dismiss behavior for all scroll views in the app.
  /// Set to `manual` to ensure scrolling doesn't unexpectedly hide the keyboard
  /// when working inside Autocomplete lists or Modals.
  static const ScrollViewKeyboardDismissBehavior surulNadathai = 
      ScrollViewKeyboardDismissBehavior.manual;
}
