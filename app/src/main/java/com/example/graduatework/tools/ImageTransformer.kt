package com.example.graduatework.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Size
import androidx.camera.core.ImageProxy
import com.example.graduatework.tools.Constants.TF_INPUT_IMAGE_SIZE
import kotlin.math.abs
import kotlin.math.min

object ImageTransformer {

    private const val MAX_CHANNEL_VALUE = 262143

    private var CAMERA_SIZE = Size(480, 640)

    fun setAnalyzerImageSize(width: Int, height: Int) {
        CAMERA_SIZE = Size(width, height)
    }

    fun transformToTFInput(image: Bitmap, orientation: Int): Bitmap {
        val matrix = getTransformationMatrix(
            CAMERA_SIZE,
            TF_INPUT_IMAGE_SIZE,
            orientation
        )
        val transformedImage = createBitmap(TF_INPUT_IMAGE_SIZE)

        with(Canvas(transformedImage)) {
            drawBitmap(image, matrix, null)
        }

        return transformedImage
    }

    fun transformToCameraInputSize(rectList: List<RectF>, orientation: Int) {
        val invertedMatrix = getTransformationMatrix(
            TF_INPUT_IMAGE_SIZE,
            CAMERA_SIZE,
            -orientation
        )
        rectList.forEach { rect ->
            invertedMatrix.mapRect(rect)
        }
    }

    fun transformForDrawing(rectList: List<RectF>, canvasWidth: Int, canvasHeight: Int, rotation: Int) {
        val rotated = rotation % 180 == 90

        val multiplier: Float = min(
            canvasHeight / (if (rotated) CAMERA_SIZE.width else CAMERA_SIZE.height).toFloat(),
            canvasWidth / (if (rotated) CAMERA_SIZE.height else CAMERA_SIZE.width).toFloat()
        )

        val newSize = Size(
            (multiplier * if (rotated) CAMERA_SIZE.height else CAMERA_SIZE.width).toInt(),
            (multiplier * if (rotated) CAMERA_SIZE.width else CAMERA_SIZE.height).toInt()
        )

        val matrix = getTransformationMatrix(
            CAMERA_SIZE,
            newSize,
            rotation
        )

        rectList.forEach { rect ->
            matrix.mapRect(rect)
        }
    }

    private fun createBitmap(size: Size): Bitmap {
        return Bitmap.createBitmap(
            size.width,
            size.height,
            Bitmap.Config.ARGB_8888
        )
    }

    /**
     * Returns a transformation matrix from one reference frame into another. Handles cropping (if
     * maintaining aspect ratio is desired) and rotation.
     *
     * @param srcSize Size of source frame.
     * @param dstSize Size of destination frame.
     * @param applyRotation Amount of rotation to apply from one frame to another. Must be a multiple
     * of 90.
     * @return The transformation fulfilling the desired requirements.
     */
    private fun getTransformationMatrix(
        srcSize: Size,
        dstSize: Size,
        applyRotation: Int
    ): Matrix {
        val matrix = Matrix()
        if (applyRotation != 0) {
            // Translate so center of image is at origin.
            matrix.postTranslate(-srcSize.width / 2.0f, -srcSize.height / 2.0f)

            // Rotate around origin.
            matrix.postRotate(applyRotation.toFloat())
        }

        // Account for the already applied rotation, if any, and then determine how
        // much scaling is needed for each axis.
        val transpose = (abs(applyRotation) + 90) % 180 == 0
        val inWidth = if (transpose) srcSize.height else srcSize.width
        val inHeight = if (transpose) srcSize.width else srcSize.height

        // Apply scaling if necessary.
        if (inWidth != dstSize.width || inHeight != dstSize.height) {
            val scaleFactorX = dstSize.width / inWidth.toFloat()
            val scaleFactorY = dstSize.height / inHeight.toFloat()
            // Scale exactly to fill dst from src.
            matrix.postScale(scaleFactorX, scaleFactorY)
        }
        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstSize.width / 2.0f, dstSize.height / 2.0f)
        }

        return matrix
    }


    fun toBitmap(image: ImageProxy): Bitmap {
        with(image) {
            val yuvArray = arrayOfNulls<ByteArray>(3)
            fillBytes(planes, yuvArray)

            val rgbArray = convertYUV420ToARGB8888(
                yuvArray.mapNotNull { it!! },
                Size(width, height),
                planes[0].rowStride,
                planes[1].rowStride,
                planes[1].pixelStride
            )

            return Bitmap.createBitmap(
                rgbArray, width, height,
                Bitmap.Config.ARGB_8888
            )
        }
    }

    private fun fillBytes(planes: Array<ImageProxy.PlaneProxy>, yuvBytes: Array<ByteArray?>) {
        // Row stride is the total number of bytes occupied in memory by a row of an image.
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer.get(yuvBytes[i]!!)
        }
    }

    private fun convertYUV420ToARGB8888(
        yuvData: List<ByteArray>,
        size: Size,
        yRowStride: Int,
        uvRowStride: Int,
        uvPixelStride: Int
    ): IntArray {
        val out = IntArray(size.width * size.height)
        var outputIndex = 0
        for (j in 0 until size.height) {
            val positionY = yRowStride * j
            val positionUV = uvRowStride * (j shr 1)

            for (i in 0 until size.width) {
                val uvOffset = positionUV + (i shr 1) * uvPixelStride

                // "0xff and" is used to cut off bits from following value that are higher than
                // the low 8 bits
                out[outputIndex] = convertYUVToRGB(
                    0xff and yuvData[0][positionY + i].toInt(),
                    0xff and yuvData[1][uvOffset].toInt(),
                    0xff and yuvData[2][uvOffset].toInt()
                )
                outputIndex += 1
            }
        }
        return out
    }

    private fun convertYUVToRGB(y: Int, u: Int, v: Int): Int {
        // Adjust and check YUV values
        val yNew = if (y - 16 < 0) 0 else y - 16
        val uNew = u - 128
        val vNew = v - 128
        val expandY = 1192 * yNew
        var r = expandY + 1634 * vNew
        var g = expandY - 833 * vNew - 400 * uNew
        var b = expandY + 2066 * uNew

        // Clipping RGB values to be inside boundaries [ 0 , MAX_CHANNEL_VALUE ]
        val checkBoundaries = { x: Int ->
            when {
                x > MAX_CHANNEL_VALUE -> MAX_CHANNEL_VALUE
                x < 0 -> 0
                else -> x
            }
        }
        r = checkBoundaries(r)
        g = checkBoundaries(g)
        b = checkBoundaries(b)
        return -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
    }
}