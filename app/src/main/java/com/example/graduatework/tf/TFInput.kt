package com.example.graduatework.tf

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.example.graduatework.tools.Constants
import com.example.graduatework.tools.Constants.IMAGE_MEAN
import com.example.graduatework.tools.Constants.IMAGE_STD
import com.example.graduatework.tools.Constants.TF_INPUT_IMAGE_SIZE
import com.example.graduatework.tools.ImageTransformer
import com.example.graduatework.tools.toBitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFInput private constructor() {

    fun getInput(): Array<ByteBuffer> {
        return arrayOf(byteBuffer)
    }

    companion object {

        private val byteBuffer = ByteBuffer.allocateDirect(Constants.TF_INPUT_BUFFER_SIZE).apply {
            order(ByteOrder.LITTLE_ENDIAN)
        }

        fun create(
            image: ImageProxy,
            sensorOrientation: Int,
            isModelQuantized: Boolean
        ): TFInput {
            val input = TFInput()
            val croppedInput = cropInputImage(image.toBitmap(), sensorOrientation)
            val pixelsArray = mapBitmapToPixelsArray(croppedInput)

            with(byteBuffer) {
                rewind()
                pixelsArray.forEach { pixel ->
                    if (isModelQuantized)
                        mapPixelToThreeBytes(pixel).forEach { byte ->
                            put(byte)
                        }
                    else
                        mapPixelToThreeFloats(pixel).forEach { flt ->
                            putFloat(flt)
                        }
                }
            }
            return input
        }

        private fun cropInputImage(input: Bitmap, sensorOrientation: Int): Bitmap {
            return ImageTransformer.transformToTFInput(input, sensorOrientation)
        }

        private fun mapBitmapToPixelsArray(input: Bitmap): IntArray {
            val pixelsArray = IntArray(TF_INPUT_IMAGE_SIZE.width * TF_INPUT_IMAGE_SIZE.width)
            with(input) {
                getPixels(pixelsArray, 0, width, 0, 0, width, height)
            }
            return pixelsArray
        }

        private fun mapPixelToThreeFloats(pixel: Int): Array<Float> {
            return arrayOf(
                ((pixel shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD,
                ((pixel shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD,
                ((pixel and 0xFF) - IMAGE_MEAN) / IMAGE_STD
            )
        }

        private fun mapPixelToThreeBytes(pixel: Int): Array<Byte> {
            return arrayOf(
                (pixel shr 16 and 0xFF).toByte(),
                (pixel shr 8 and 0xFF).toByte(),
                (pixel and 0xFF).toByte()
            )
        }
    }
}

