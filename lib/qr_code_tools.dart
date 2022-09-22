import 'dart:async';

import 'package:flutter/services.dart';

class QrCodeToolsPlugin {
  static const _channel = const MethodChannel('qr_code_tools');

  /// [filePath] is local file path
  static Future<String?> decodeFrom(String filePath) => _channel.invokeMethod(
        'decoder',
        {
          'file': filePath,
        },
      );
}
