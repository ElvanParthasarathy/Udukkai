import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../koorugal/ullnuzhaivu_koorugal.dart';
// To access ShellDemoScreen
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> {
  String _email = '';
  String _password = '';
  bool _loading = false;
  String _error = '';

  void _handleLogin() async {
    if (_email.isEmpty || _password.isEmpty) {
      setState(() {
        _error = K.vidupattaPulangalaiNirappavum.tr(context, ref);
      });
      return;
    }

    setState(() {
      _error = '';
      _loading = true;
    });

    // Mock Login Delay
    await Future.delayed(const Duration(seconds: 1));

    if (!mounted) return;

    if (_email == 'test@niril.com' && _password == 'kadavuchol') {
      ref.read(isLoggedInProvider.notifier).setLoggedIn(true);
      Navigator.popUntil(context, (route) => route.isFirst);
    } else {
      ref.read(isLoggedInProvider.notifier).setLoggedIn(true);
      Navigator.popUntil(context, (route) => route.isFirst);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AuthLayout(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          AuthAnimatedElement(
            delayIndex: 0,
            child: AuthBackButton(
              onPressed: () => Navigator.pop(context),
            ),
          ),
          const SizedBox(height: 16),
          AuthHeader(
            title: K.niril.tr(context, ref), // Brand name
            subtitle: K.cheyaliyaiAnugaUlnuzhaiyavum.tr(context, ref),
          ),
          const SizedBox(height: 32),
          AuthInput(
            label: K.minnanjalMugavari.tr(context, ref),
            placeholder: K.minnanjalaiUllidavum.tr(context, ref),
            value: _email,
            onChange: (val) => setState(() => _email = val),
          ),
          AuthInput(
            label: K.kadavuchol.tr(context, ref),
            placeholder: K.kadavuchollaiUllidavum.tr(context, ref),
            value: _password,
            onChange: (val) => setState(() => _password = val),
            isPassword: true,
            errorText: _error.isNotEmpty ? _error : null,
          ),
          const SizedBox(height: 32),
          AuthButton(
            text: K.ulnuzhaiga.tr(context, ref),
            loading: _loading,
            onPressed: _handleLogin,
          ),
        ],
      ),
    );
  }
}
