package com.example.graduatework.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Size
import com.example.graduatework.tools.Constants.CAMERA_SIZE
import com.example.graduatework.tools.Constants.TF_INPUT_IMAGE_SIZE
import kotlin.math.abs
import kotlin.math.min

object ImageTransformer {

    private lateinit var lastTransformMatrix: Matrix

    fun transformToTFInput(image: Bitmap, orientation: Int): Bitmap {
        lastTransformMatrix = getTransformationMatrix(
            CAMERA_SIZE,
            TF_INPUT_IMAGE_SIZE,
            orientation
        )
        val transformedImage = createBitmap(TF_INPUT_IMAGE_SIZE)

        with(Canvas(transformedImage)) {
            drawBitmap(image, lastTransformMatrix, null)
        }

        return transformedImage
    }

    fun transformToCameraInputSize(rect: RectF) {
        val invertedMatrix = Matrix()
        lastTransformMatrix.invert(invertedMatrix)
        invertedMatrix.mapRect(rect)
    }

    fun transformForDrawing(rectF: RectF, canvasWidth: Int, canvasHeight: Int, rotation: Int) {
        val rotated = rotation % 180 == 90

        val multiplier: Float = min(
            canvasHeight / (if (rotated) CAMERA_SIZE.width else CAMERA_SIZE.height).toFloat(),
            canvasWidth / (if (rotated) CAMERA_SIZE.height else CAMERA_SIZE.width).toFloat()
        )

        val newSize = Size(
            (multiplier * if (rotated) CAMERA_SIZE.height else CAMERA_SIZE.width).toInt(),
            (multiplier * if (rotated) CAMERA_SIZE.width else CAMERA_SIZE.height).toInt()
        )

        getTransformationMatrix(
            CAMERA_SIZE,
            newSize,
            rotation
        ).mapRect(rectF)
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
}