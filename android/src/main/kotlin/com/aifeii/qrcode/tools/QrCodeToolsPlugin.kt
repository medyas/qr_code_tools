package com.aifeii.qrcode.tools

//import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
//import android.util.Log
import androidx.annotation.NonNull
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
//import com.googlecode.tesseract.android.TessBaseAPI
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
//import kotlinx.coroutines.*
//import org.jmrtd.lds.icao.MRZInfo
import java.io.*
import java.util.*


class QrCodeToolsPlugin : FlutterPlugin, MethodCallHandler {

    private lateinit var channel: MethodChannel
//    private lateinit var context: Context
//
//    private val scope = CoroutineScope(Job() + Dispatchers.Main)


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, channelName)
        channel.setMethodCallHandler(this)
//        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "decoder_barcode" -> {
                val filePath = call.argument<String>("file")
                val file = File(filePath ?: "")

                if (!file.exists()) {
                    result.error("File not found. filePath: $filePath", null, null)
                    return
                }

                val (bitmap, w, h) = getScaledBitmap(file)

                val binaryBitmap = getCroppedBinaryBitmap(w, h, bitmap)

                val hints = getDecoderOptions()

                try {
                    val decodeResult = MultiFormatReader().decode(binaryBitmap, hints)
                    result.success(decodeResult.text)
                } catch (e: NotFoundException) {
                    result.error("Not found data: ${e.message}", null, null)
                }
            }

            "decoder_mrz" -> {
//                val filePath = call.argument<String>("file")
//                val file = File(filePath ?: "")
//
//                if (!file.exists()) {
//                    result.error("File not found. filePath: $filePath", null, null)
//                    return
//                }
//
//                val (bitmap, _, _) = getScaledBitmap(file)
//
//                scope.launch {
//                    extractDataFromImage(bitmap, result)
//                }
            }

            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
//        scope.cancel()
    }

    private fun getCroppedBinaryBitmap(
        w: Int,
        h: Int,
        bitmap: Bitmap
    ): BinaryBitmap {
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels).crop(
            0,
            (bitmap.height * .7).toInt(), bitmap.width, (bitmap.height * .3).toInt(),
        )
        return BinaryBitmap(HybridBinarizer(source))
    }

    private fun getDecoderOptions(): Hashtable<DecodeHintType, Any> {
        val hints = Hashtable<DecodeHintType, Any>()
        val decodeFormats = ArrayList<BarcodeFormat>()
        decodeFormats.add(BarcodeFormat.ITF)
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        hints[DecodeHintType.CHARACTER_SET] = "utf-8"
        hints[DecodeHintType.TRY_HARDER] = true
        return hints
    }

    private fun getScaledBitmap(file: File): Triple<Bitmap, Int, Int> {
        val fis = FileInputStream(file)
        var bitmap = BitmapFactory.decodeStream(fis)
        var w = bitmap.width
        var h = bitmap.height

        if ((w * h) > (maxWidth * maxHeight)) {
            val scale: Float
            if (w >= h) {
                scale = maxWidth.toFloat() / w.toFloat()
                w = maxWidth
                h = (h * scale).toInt()
            } else {
                scale = maxHeight.toFloat() / h.toFloat()
                h = maxHeight
                w = (w * scale).toInt()
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false)
        }
        return Triple(bitmap, w, h)
    }

//    private suspend fun extractDataFromImage(bitmap: Bitmap, result: MethodChannel.Result) {
//        withContext(Dispatchers.IO) {
//            try {
//                val cropped = calculateCutoutRect(bitmap, true)
//                copyOcrFileToCache()
//                val mrz = scanMRZ(cropped)
//                val isValid = checkIsValidMrz(mrz)
//                val fixedMrz = extractMRZ(mrz)
//
//                withContext(Dispatchers.Main) {
//                    Log.d("QrCodeToolsPlugin", "MRZ: $mrz")
//                    Log.d("QrCodeToolsPlugin", "IsValid: $isValid --- $fixedMrz")
//                    result.success(fixedMrz)
//                }
//            } catch (e: Exception) {
//                Log.d("QrCodeToolsPlugin", "Extract Error: $e")
//                withContext(Dispatchers.Main) {
//                    result.error("Not found data: ${e.message}", null, null)
//                }
//            }
//        }
//    }
//
//    private fun copyOcrFileToCache() {
//        val cacheDir = context.cacheDir.toString()
//        val assetDir = context.assets
//
//        val f = File("$cacheDir/tessdata/ocrb.traineddata")
//        val folder = File("$cacheDir/tessdata")
//        if (!f.exists()) try {
//            if (!f.exists() && !folder.mkdirs()) {
//                Log.e("QrCodeToolsPlugin", "Couldn't make directory $folder")
//                return
//            }
//            val `is`: InputStream = assetDir.open("tessdata/ocrb.traineddata")
//            val fos = FileOutputStream(f)
//            copyFile(`is`, fos)
//            fos.close()
//            `is`.close()
//        } catch (e: java.lang.Exception) {
//            throw RuntimeException(e)
//        }
//
//
////        val f2 = File("$cacheDir/tessdata/eng.user-patterns")
////        if (!f2.exists()) try {
////            val `is`: InputStream = assetDir.open("tessdata/eng.user-patterns")
////            val fos = FileOutputStream(f2)
////            copyFile(`is`, fos)
////            fos.close()
////            `is`.close()
////        } catch (e: java.lang.Exception) {
////            throw RuntimeException(e)
////        }
//
////        val f3 = File("$cacheDir/tessdata/eng.traineddata")
////        if (!f3.exists()) try {
////            val `is`: InputStream = assetDir.open("tessdata/eng.traineddata")
////            val fos = FileOutputStream(f3)
////            copyFile(`is`, fos)
////            fos.close()
////            `is`.close()
////        } catch (e: java.lang.Exception) {
////            throw RuntimeException(e)
////        }
//    }
//
//    private fun scanMRZ(bitmap: Bitmap): String {
//        val baseApi = TessBaseAPI()
//        baseApi.init(context.cacheDir.absolutePath, "ocrb")
//        baseApi.setImage(bitmap)
//        val mrz = baseApi.utF8Text
//        baseApi.recycle()
//        return mrz
//    }
//
//    private fun checkIsValidMrz(mrz: String?): Boolean {
//        return try {
//            val mrzInfo1 = MRZInfo(mrz)
//            Log.d("QrCodeToolsPlugin", mrzInfo1.toString())
//            // compute check digits using this constructor from JMRTD
//            val mrzInfo2 = MRZInfo(
//                "P",  // workaround for JMRTD ID is not supported
//                mrzInfo1.issuingState,
//                mrzInfo1.primaryIdentifier,
//                mrzInfo1.secondaryIdentifier,
//                mrzInfo1.documentNumber,
//                mrzInfo1.nationality,
//                mrzInfo1.dateOfBirth,
//                mrzInfo1.gender,
//                mrzInfo1.dateOfExpiry,
//                mrzInfo1.personalNumber
//            )
//            mrzInfo2.documentCode = mrzInfo1.documentCode //undo workaround
//
//            Log.d("QrCodeToolsPlugin", mrzInfo1.toString())
//            // If string representation of OCR is equals to computed check digits means is correct
//            mrzInfo1.toString() == mrzInfo2.toString()
//        } catch (e: IllegalStateException) {
//            Log.w("QrCodeToolsPlugin", "checksum fail", e)
//            false
//        } catch (e: IllegalArgumentException) {
//            Log.w("QrCodeToolsPlugin", "checksum fail", e)
//            false
//        }
//    }
//
//    private fun extractMRZ(input: String): String {
//        val lines = input.split("\n")
//        val mrzLength = lines.last().length
//        val mrzLines = lines.takeLastWhile { it.length == mrzLength }
//        return mrzLines.joinToString("\n")
//    }
//
//    private fun calculateCutoutRect(bitmap: Bitmap, cropToMRZ: Boolean): Bitmap {
//        val documentFrameRatio = 1.42 // Passport's size (ISO/IEC 7810 ID-3) is 125mm × 88mm
//        val width: Double
//        val height: Double
//
//        if (bitmap.height > bitmap.width) {
//            width = bitmap.width * 0.9 // Fill 90% of the width
//            height = width / documentFrameRatio
//        } else {
//            height = bitmap.height * 0.75 // Fill 75% of the height
//            width = height * documentFrameRatio
//        }
//
//        val mrzZoneOffset = if (cropToMRZ) height * 0.6 else 0.toDouble()
//        val topOffset = (bitmap.height - height) / 2 + mrzZoneOffset
//        val leftOffset = (bitmap.width - width) / 2
//
//        return Bitmap.createBitmap(
//            bitmap,
//            leftOffset.toInt(),
//            topOffset.toInt(),
//            width.toInt(),
//            (height - mrzZoneOffset).toInt()
//        )
//    }
//
//    @Throws(IOException::class)
//    private fun copyFile(`in`: InputStream, out: OutputStream): Boolean {
//        val buffer = ByteArray(1024)
//        var read: Int
//        while (`in`.read(buffer).also { read = it } != -1) {
//            out.write(buffer, 0, read)
//        }
//        return true
//    }

    companion object {

        const val channelName = "barcode_tools"

        const val maxWidth = 5616
        const val maxHeight = 3744
    }
}
