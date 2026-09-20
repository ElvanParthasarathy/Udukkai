import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

/// Handles the staggered entrance animation from React (opacity 0->1, translateY 20px->0px).
class AuthAnimatedElement extends StatefulWidget {
  final Widget child;
  final int delayIndex; // 1, 2, 3... matches delay-1, delay-2 in CSS
  final Duration duration;

  const AuthAnimatedElement({
    super.key,
    required this.child,
    this.delayIndex = 1,
    this.duration =
        const Duration(milliseconds: 800), // Match React's 0.8s duration
  });

  @override
  State<AuthAnimatedElement> createState() => _AuthAnimatedElementState();
}

class _AuthAnimatedElementState extends State<AuthAnimatedElement>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _opacity;
  late Animation<Offset> _offset;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: widget.duration);

    _opacity = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeOut),
    );

    _offset =
        Tween<Offset>(begin: const Offset(0, 20.0), end: Offset.zero).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeOutBack),
    );

    // Staggered delay based on index (delay-1 = 100ms, delay-2 = 200ms)
    Future.delayed(Duration(milliseconds: widget.delayIndex * 100), () {
      if (mounted) {
        _controller.forward();
      }
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Opacity(
          opacity: _opacity.value,
          child: Transform.translate(
            offset: _offset.value,
            child: child,
          ),
        );
      },
      child: widget.child,
    );
  }
}

/// The main layout matching AuthLayout in React. Renders animated background shapes.
class AuthLayout extends ConsumerStatefulWidget {
  final Widget child;
  final bool hideLogo;
  final bool showBranding;
  final Widget? floatingActionButton;

  const AuthLayout({
    super.key,
    required this.child,
    this.hideLogo = false,
    this.showBranding = false,
    this.floatingActionButton,
  });

  @override
  ConsumerState<AuthLayout> createState() => _AuthLayoutState();
}

class _AuthLayoutState extends ConsumerState<AuthLayout> with TickerProviderStateMixin {
  late AnimationController _rotate1Controller;
  late AnimationController _rotate2Controller;
  late AnimationController _float1Controller;
  late AnimationController _float2Controller;

  @override
  void initState() {
    super.initState();
    // Rotate 1: 60s linear infinite
    _rotate1Controller =
        AnimationController(vsync: this, duration: const Duration(seconds: 60))
          ..repeat();
    // Rotate 2: 40s linear infinite reverse
    _rotate2Controller =
        AnimationController(vsync: this, duration: const Duration(seconds: 40))
          ..repeat(reverse: true); // Approximation of reverse rotation
    // Float 1: 4s ease-in-out infinite alternate
    _float1Controller =
        AnimationController(vsync: this, duration: const Duration(seconds: 4))
          ..repeat(reverse: true);
    // Float 2: 5s ease-in-out infinite alternate
    _float2Controller =
        AnimationController(vsync: this, duration: const Duration(seconds: 5))
          ..repeat(reverse: true);
  }

  @override
  void dispose() {
    _rotate1Controller.dispose();
    _rotate2Controller.dispose();
    _float1Controller.dispose();
    _float2Controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final shapeColor =
        isDark ? Colors.white.withValues(alpha: 0.03) : const Color(0xFFEAEAEA);
    final bgColor = isDark ? const Color(0xFF0A0A0A) : const Color(0xFFFAFAFA);

    return Scaffold(
      backgroundColor: bgColor,
      floatingActionButton: widget.floatingActionButton,
      body: Stack(
        children: [
          // Shape 1 (Top Right, Large Rounded Square)
          Positioned(
            top: MediaQuery.of(context).size.height * 0.02,
            right: -MediaQuery.of(context).size.width * 0.1,
            width: MediaQuery.of(context).size.width * 0.5,
            height: MediaQuery.of(context).size.width * 0.5,
            child: RotationTransition(
              turns: _rotate1Controller,
              alignment: const Alignment(0.85, 0.15), // Transform origin approx
              child: Container(
                decoration: BoxDecoration(
                  color: shapeColor,
                  borderRadius: BorderRadius.circular(80),
                ),
              ),
            ),
          ),
          // Shape 2 (Bottom Left, Circle)
          Positioned(
            bottom: MediaQuery.of(context).size.height * 0.05,
            left: -MediaQuery.of(context).size.width * 0.1,
            width: MediaQuery.of(context).size.width * 0.7,
            height: MediaQuery.of(context).size.width * 0.7,
            child: AnimatedBuilder(
              animation: _float1Controller,
              builder: (context, child) {
                // Float animation: translateY(0px to 30px)
                final dy =
                    Curves.easeInOut.transform(_float1Controller.value) * 30.0;
                return Transform.translate(
                  offset: Offset(0, dy),
                  child: child,
                );
              },
              child: Container(
                decoration: BoxDecoration(
                  color: shapeColor,
                  shape: BoxShape.circle,
                ),
              ),
            ),
          ),
          // Shape 3 (Top Left, Small Rounded Square)
          Positioned(
            top: MediaQuery.of(context).size.height * 0.15,
            left: MediaQuery.of(context).size.width * 0.05,
            width: MediaQuery.of(context).size.width * 0.2,
            height: MediaQuery.of(context).size.width * 0.2,
            child: RotationTransition(
              turns: Tween(begin: 1.0, end: 0.0)
                  .animate(_rotate2Controller), // Reverse rotation
              child: Container(
                decoration: BoxDecoration(
                  color: shapeColor,
                  borderRadius: BorderRadius.circular(30),
                ),
              ),
            ),
          ),
          // Shape 4 (Center Right, Small Circle)
          Positioned(
            top: MediaQuery.of(context).size.height * 0.60,
            right: MediaQuery.of(context).size.width * 0.1,
            width: MediaQuery.of(context).size.width * 0.15,
            height: MediaQuery.of(context).size.width * 0.15,
            child: AnimatedBuilder(
              animation: _float2Controller,
              builder: (context, child) {
                // Float animation reverse: translateY(30px to 0px) approx
                final dy = (1.0 -
                        Curves.easeInOut.transform(_float2Controller.value)) *
                    30.0;
                return Transform.translate(
                  offset: Offset(0, dy),
                  child: child,
                );
              },
              child: Container(
                decoration: BoxDecoration(
                  color: shapeColor,
                  shape: BoxShape.circle,
                ),
              ),
            ),
          ),

          // Main Content
          SafeArea(
            child: Center(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                child: ConstrainedBox(
                  constraints: const BoxConstraints(maxWidth: 480),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 24.0, vertical: 32.0),
                    child: widget.child,
                  ),
                ),
              ),
            ),
          ),

          // Global Branding Signature
          if (widget.showBranding)
            Positioned(
              bottom: 64,
              left: 0,
              right: 0,
              child: IgnorePointer(
                child: Center(
                  child: Text(
                    K.elvanParthasarathy.tr(context, ref),
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w400,
                      letterSpacing: 1.2,
                      color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.5),
                    ),
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class AuthHeader extends StatelessWidget {
  final String title;
  final String subtitle;

  const AuthHeader({super.key, required this.title, required this.subtitle});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return AuthAnimatedElement(
      delayIndex: 1,
      child: Column(
        children: [
          Text(
            title,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 28, // clamp approximation
              fontWeight: FontWeight.w800,
              color: isDark ? Colors.white : const Color(0xFF1A1A1A),
              letterSpacing: -0.5,
            ),
          ),
          if (subtitle.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              subtitle,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w400,
                color: isDark
                    ? Colors.white.withValues(alpha: 0.6)
                    : const Color(0xFF666666),
              ),
            ),
          ]
        ],
      ),
    );
  }
}

class AuthInput extends StatefulWidget {
  final String label;
  final String placeholder;
  final String value;
  final ValueChanged<String> onChange;
  final bool isPassword;
  final String? helperText;
  final String? errorText;

  const AuthInput({
    super.key,
    required this.label,
    required this.placeholder,
    required this.value,
    required this.onChange,
    this.isPassword = false,
    this.helperText,
    this.errorText,
  });

  @override
  State<AuthInput> createState() => _AuthInputState();
}

class _AuthInputState extends State<AuthInput> {
  bool _obscureText = true;
  final FocusNode _focusNode = FocusNode();
  bool _isFocused = false;

  @override
  void initState() {
    super.initState();
    _obscureText = widget.isPassword;
    _focusNode.addListener(() {
      setState(() {
        _isFocused = _focusNode.hasFocus;
      });
    });
  }

  @override
  void dispose() {
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final inputBg = isDark ? const Color(0xFF1E1E1E) : const Color(0xFFFFFFFF);
    final textColor = isDark ? Colors.white : const Color(0xFF1A1A1A);
    final labelColor =
        isDark ? Colors.white.withValues(alpha: 0.6) : const Color(0xFF666666);

    // Simulate the CSS hover/focus shadows
    final boxShadow = isDark
        ? null
        : [
            if (_isFocused)
              BoxShadow(
                  color: Colors.black.withValues(alpha: 0.16),
                  blurRadius: 24,
                  offset: const Offset(0, 8))
            else
              BoxShadow(
                  color: Colors.black.withValues(alpha: 0.08),
                  blurRadius: 24,
                  offset: const Offset(0, 8))
          ];

    return AuthAnimatedElement(
      delayIndex: 2,
      child: Padding(
        padding: const EdgeInsets.only(bottom: 12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (widget.label.isNotEmpty) ...[
              Padding(
                padding: const EdgeInsets.only(left: 20.0, bottom: 8.0),
                child: Text(
                  widget.label,
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: labelColor,
                    letterSpacing: 0.5,
                  ),
                ),
              ),
            ],
            Container(
              decoration: BoxDecoration(
                color: inputBg,
                borderRadius: BorderRadius.circular(50),
                boxShadow: boxShadow,
              ),
              child: TextField(
                focusNode: _focusNode,
                obscureText: _obscureText,
                onChanged: widget.onChange,
                textAlignVertical: TextAlignVertical.center,
                style: TextStyle(
                  fontSize: 14.0,
                  fontWeight: FontWeight.w500,
                  color: textColor,
                ),
                decoration: InputDecoration(
                  isDense: true,
                  hintText: widget.placeholder,
                  hintStyle: TextStyle(
                    color: labelColor,
                    fontWeight: FontWeight.w400,
                  ),
                  contentPadding:
                      const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                  border: InputBorder.none,
                  enabledBorder: InputBorder.none,
                  focusedBorder: InputBorder.none,
                  suffixIcon: widget.isPassword
                      ? IconButton(
                          icon: Icon(
                            _obscureText
                                ? Icons.visibility_off
                                : Icons.visibility,
                            color:
                                isDark ? Colors.white : const Color(0xFF111111),
                            size: 20.0,
                          ),
                          onPressed: () {
                            setState(() {
                              _obscureText = !_obscureText;
                            });
                          },
                        )
                      : null,
                ),
              ),
            ),
            if (widget.errorText != null || widget.helperText != null) ...[
              Padding(
                padding: const EdgeInsets.only(left: 12.0, top: 6.0),
                child: Text(
                  widget.errorText ?? widget.helperText!,
                  style: TextStyle(
                    fontSize: 10,
                    color: widget.errorText != null
                        ? theme.colorScheme.error
                        : labelColor,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class AuthButton extends StatelessWidget {
  final String text;
  final VoidCallback onPressed;
  final bool loading;
  final bool disabled;

  const AuthButton({
    super.key,
    required this.text,
    required this.onPressed,
    this.loading = false,
    this.disabled = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    final btnBg = isDark ? Colors.white : Colors.black;
    final btnText = isDark ? Colors.black : Colors.white;
    final isDisabled = disabled || loading;

    return AuthAnimatedElement(
      delayIndex: 3,
      child: Padding(
        padding: const EdgeInsets.only(top: 16.0, bottom: 8.0),
        child: SizedBox(
          width: double.infinity,
          height: 54,
          child: ElevatedButton(
            onPressed: isDisabled ? null : onPressed,
            style: ElevatedButton.styleFrom(
              backgroundColor: btnBg,
              foregroundColor: btnText,
              disabledBackgroundColor: btnBg.withValues(alpha: 0.6),
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(50),
              ),
              shadowColor: Colors.transparent,
            ),
            child: loading
                ? SizedBox(
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator(
                      strokeWidth: 3,
                      valueColor: AlwaysStoppedAnimation<Color>(
                          btnText.withValues(alpha: 0.5)),
                    ),
                  )
                : Text(
                    text,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
          ),
        ),
      ),
    );
  }
}

class AuthBackButton extends StatelessWidget {
  final VoidCallback onPressed;

  const AuthBackButton({super.key, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Align(
      alignment: Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 24.0),
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          color: theme.colorScheme.onSurface.withValues(alpha: 0.08),
          shape: BoxShape.circle,
        ),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            borderRadius: BorderRadius.circular(24),
            onTap: onPressed,
            child: const Center(
              child: Padding(
                padding: EdgeInsets.only(
                    right: 2.0), // Optical centering for chevron
                child: Icon(
                  CupertinoIcons.chevron_back,
                  size: 24,
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
