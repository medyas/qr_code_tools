import 'dart:async';

import 'package:flutter/services.dart';

class QrCodeToolsPlugin {
  static const _channel = const MethodChannel('barcode_tools');

  /// extract barcode data from image
  /// the image should contain a barcode type BarcodeFormat.ITF
  /// [filePath] is local file path
  static Future<String?> decodeBarcode(String filePath) =>
      _channel.invokeMethod(
        'decoder_barcode',
        {
          'file': filePath,
        },
      );

  /// extract MRZ data from image
  /// [filePath] is local file path
  static Future<String?> decodeMrz(String filePath) => _channel.invokeMethod(
        'decoder_mrz',
        {
          'file': filePath,
        },
      );
}
