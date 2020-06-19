package com.rithikjain.neuralstyletransfer

import android.content.Context
import android.graphics.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class ImageUtils {
    companion object {

        private fun scaleBitmapAndKeepRatio(
            targetBmp: Bitmap,
            reqHeightInPixels: Int,
            reqWidthInPixels: Int
        ): Bitmap {
            if (targetBmp.height == reqHeightInPixels && targetBmp.width == reqWidthInPixels) {
                return targetBmp
            }
            val matrix = Matrix()
            matrix.setRectToRect(
                RectF(
                    0f, 0f,
                    targetBmp.width.toFloat(),
                    targetBmp.width.toFloat()
                ),
                RectF(
                    0f, 0f,
                    reqWidthInPixels.toFloat(),
                    reqHeightInPixels.toFloat()
                ),
                Matrix.ScaleToFit.FILL
            )
            return Bitmap.createBitmap(
                targetBmp, 0, 0,
                targetBmp.width,
                targetBmp.width, matrix, true
            )
        }

        fun bitmapToByteBuffer(
            bitmapIn: Bitmap,
            width: Int,
            height: Int,
            mean: Float = 0.0f,
            std: Float = 255.0f
        ): ByteBuffer {
            val bitmap = scaleBitmapAndKeepRatio(bitmapIn, width, height)
            val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
            inputImage.order(ByteOrder.nativeOrder())
            inputImage.rewind()

            val intValues = IntArray(width * height)
            bitmap.getPixels(intValues, 0, width, 0, 0, width, height)
            var pixel = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val value = intValues[pixel++]

                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value and 0xFF) - mean) / std)
                }
            }

            inputImage.rewind()
            return inputImage
        }

        fun loadBitmapFromResources(context: Context, path: String): Bitmap {
            val inputStream = context.assets.open(path)
            return BitmapFactory.decodeStream(inputStream)
        }

        fun convertArrayToBitmap(
            imageArray: Array<Array<Array<FloatArray>>>,
            imageWidth: Int,
            imageHeight: Int
        ): Bitmap {
            val conf = Bitmap.Config.ARGB_8888 // see other conf types
            val styledImage = Bitmap.createBitmap(imageWidth, imageHeight, conf)

            for (x in imageArray[0].indices) {
                for (y in imageArray[0][0].indices) {
                    val color = Color.rgb(
                        ((imageArray[0][x][y][0] * 255).toInt()),
                        ((imageArray[0][x][y][1] * 255).toInt()),
                        (imageArray[0][x][y][2] * 255).toInt()
                    )

                    // this y, x is in the correct order!!!
                    styledImage.setPixel(y, x, color)
                }
            }
            return styledImage
        }

    }
}