package com.example.graduatework.tf

import android.graphics.RectF
import com.example.graduatework.tools.Constants.NUM_DETECTIONS
import com.example.graduatework.tools.Constants.TF_INPUT_IMAGE_SIZE

class TFOutput {
    private val locations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
    private val classes = Array(1) { FloatArray(NUM_DETECTIONS) }
    private val scores = Array(1) { FloatArray(NUM_DETECTIONS) }
    private val numDetections = FloatArray(1)

    private val output = hashMapOf(
        0 to locations,
        1 to classes,
        2 to scores,
        3 to numDetections
    )

    fun getOutput(): Map<Int, Any> = output

    fun mapToRecognizedSignsList(labels: List<String>): List<RecognizedSign> {
        val recognizedSigns = mutableListOf<RecognizedSign>()
        for (index in 0 until NUM_DETECTIONS) {
            val signRect = RectF(
                locations[0][index][1] * TF_INPUT_IMAGE_SIZE.width,
                locations[0][index][0] * TF_INPUT_IMAGE_SIZE.width,
                locations[0][index][3] * TF_INPUT_IMAGE_SIZE.width,
                locations[0][index][2] * TF_INPUT_IMAGE_SIZE.width
            )
            val labelIndex = (classes[0][index] + 1).toInt() // 1 - offset of background class
            recognizedSigns.add(
                RecognizedSign(
                    rect = signRect,
                    label = labels[index],
                    score = scores[0][index]
                )
            )
        }
        return recognizedSigns
    }
}