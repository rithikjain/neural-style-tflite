package com.rithikjain.neuralstyletransfer

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.net.URI

class StyleTransferModel(private val context: Context) {
    private val interpreterPredict: Interpreter
    private val interpreterTransform: Interpreter
    private var filterBottleNeck: Array<Array<Array<FloatArray>>>? = null

    init {
        val options = Interpreter.Options()
        options.setNumThreads(4)
        interpreterPredict =
            Interpreter(FileUtil.loadMappedFile(context, PREDICT_MODEL_FILE), options)
        interpreterTransform =
            Interpreter(FileUtil.loadMappedFile(context, TRANSFORM_MODEL_FILE), options)
    }

    companion object {
        private const val PREDICT_MODEL_FILE = "predict-model.tflite"
        private const val TRANSFORM_MODEL_FILE = "transform-model.tflite"
        private const val STYLE_IMAGE_SIZE = 256
        private const val CONTENT_IMAGE_SIZE = 384
        private const val BOTTLENECK_SIZE = 100
    }

    fun setUpFilter(styleImageName: String) {
        try {
            val startTime = System.currentTimeMillis()

            // Loading style image
            val styleImage = ImageUtils.loadBitmapFromResources(context, "styles/$styleImageName")
            val input =
                ImageUtils.bitmapToByteBuffer(styleImage, STYLE_IMAGE_SIZE, STYLE_IMAGE_SIZE)

            val inputsForPredict = arrayOf<Any>(input)
            val outputsForPredict = HashMap<Int, Any>()
            val styleBottleneck = Array(1) { Array(1) { Array(1) { FloatArray(BOTTLENECK_SIZE) } } }
            outputsForPredict[0] = styleBottleneck

            // Running interpreter for the predict model
            interpreterPredict.runForMultipleInputsOutputs(inputsForPredict, outputsForPredict)

            filterBottleNeck = styleBottleneck

            val endTime = System.currentTimeMillis()
            Log.d("esh", "Time take for setting filter ${endTime - startTime}ms")

        } catch (e: Exception) {
            Log.d("esh", "Oof, something went wrong ${e.message}")
        }
    }

    fun styleImage(contentImagePath: String): Bitmap? {
        try {
            val startTime = System.currentTimeMillis()

            // Loading content image
            val contentImage = ImageUtils.decodeBitmap(File(contentImagePath))
            val contentArray =
                ImageUtils.bitmapToByteBuffer(contentImage, CONTENT_IMAGE_SIZE, CONTENT_IMAGE_SIZE)

            val inputsForStyleTransfer = arrayOf(contentArray, filterBottleNeck)
            val outputsForStyleTransfer = HashMap<Int, Any>()
            val outputImage =
                Array(1) { Array(CONTENT_IMAGE_SIZE) { Array(CONTENT_IMAGE_SIZE) { FloatArray(3) } } }
            outputsForStyleTransfer[0] = outputImage

            // Running interpreter for the style transform model
            interpreterTransform.runForMultipleInputsOutputs(
                inputsForStyleTransfer,
                outputsForStyleTransfer
            )

            val endTime = System.currentTimeMillis()
            Log.d("esh", "Time taken for transform ${endTime - startTime}ms")

            return ImageUtils.convertArrayToBitmap(
                outputImage,
                CONTENT_IMAGE_SIZE,
                CONTENT_IMAGE_SIZE
            )

        } catch (e: Exception) {
            Log.d("esh", "Oof, something went wrong ${e.message}")
            return null
        }
    }

    fun close() {
        interpreterPredict.close()
        interpreterTransform.close()
        Log.d("esh", "Closed successfully")
    }
}