package com.example.graduatework.tools

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.example.graduatework.tools.Constants.CAMERA_SIZE
import org.koin.core.time.measureDuration
import timber.log.Timber

private const val MAX_CHANNEL_VALUE = 262143

fun ImageProxy.toBitmap(): Bitmap {
    val yuvArray = arrayOfNulls<ByteArray>(3)
    fillBytes(planes, yuvArray)

    val rgbArray = IntArray(width * height)

    convertYUV420ToARGB8888(
        yuvArray[0]!!,
        yuvArray[1]!!,
        yuvArray[2]!!,
        width,
        height,
        planes[0].rowStride,
        planes[1].rowStride,
        planes[1].pixelStride,
        rgbArray
    )

    return Bitmap.createBitmap(
        rgbArray, width, height,
        Bitmap.Config.ARGB_8888
    )
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

fun convertYUV420ToARGB8888(
    yData: ByteArray,
    uData: ByteArray,
    vData: ByteArray,
    width: Int,
    height: Int,
    yRowStride: Int,
    uvRowStride: Int,
    uvPixelStride: Int,
    out: IntArray
) {
    var outputIndex = 0
    for (j in 0 until height) {
        val positionY = yRowStride * j
        val positionUV = uvRowStride * (j shr 1)

        for (i in 0 until width) {
            val uvOffset = positionUV + (i shr 1) * uvPixelStride

            // "0xff and" is used to cut off bits from following value that are higher than
            // the low 8 bits
            out[outputIndex] = convertYUVToRGB(
                0xff and yData[positionY + i].toInt(),
                0xff and uData[uvOffset].toInt(),
                0xff and vData[uvOffset].toInt()
            )
            outputIndex += 1
        }
    }
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