package com.example.graduatework.tools

import android.util.Size

object Constants {

    //region Camera settings
    private const val CAMERA_WIDTH = 320
    private const val CAMERA_HEIGHT = 240
    val CAMERA_SIZE = Size(CAMERA_WIDTH, CAMERA_HEIGHT)
    //endregion

    //region TF Settings
    const val TF_OD_MODEL_FILENAME = "detect.tflite"
    const val TF_OD_LABELS_FILENAME = "labelmap.txt"

    private const val TF_INPUT_IMAGE_WIDTH = 300
    val TF_INPUT_IMAGE_SIZE = Size(TF_INPUT_IMAGE_WIDTH, TF_INPUT_IMAGE_WIDTH)

    const val TF_INPUT_BUFFER_SIZE = TF_INPUT_IMAGE_WIDTH * TF_INPUT_IMAGE_WIDTH * 3
    const val IMAGE_MEAN = 128.0f
    const val IMAGE_STD = 128.0f
    const val NUM_DETECTIONS = 10
    const val MIN_RESULT_SCORE = 0.55
    //endregion

}