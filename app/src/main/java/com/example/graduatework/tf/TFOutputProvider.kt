package com.example.graduatework.tf

import android.graphics.RectF
import com.example.graduatework.tools.Constants.NUM_DETECTIONS
import com.example.graduatework.tools.Constants.TF_INPUT_IMAGE_SIZE

class TFOutputProvider {

    fun create(): Output {
        return Output(
            globLocations[0],
            globClasses[0],
            globScores[0]
        )
    }

    inner class Output(
        private val locations: Array<FloatArray>,
        private val classes: FloatArray,
        private val scores: FloatArray
    ) {
        fun getOutput(): Map<Int, Any> {
            return outputTFContainer
        }

        fun mapToRecognizedSignsList(labels: List<String>): List<RecognizedSign> {
            val recognizedSigns = mutableListOf<RecognizedSign>()

            for (index in 0 until NUM_DETECTIONS) {
                val signRect = RectF(
                    locations[index][1] * TF_INPUT_IMAGE_SIZE.width,
                    locations[index][0] * TF_INPUT_IMAGE_SIZE.width,
                    locations[index][3] * TF_INPUT_IMAGE_SIZE.width,
                    locations[index][2] * TF_INPUT_IMAGE_SIZE.width
                )
                val labelIndex = (classes[index] + 1).toInt() // 1 - offset of background class
                recognizedSigns.add(
                    RecognizedSign(
                        rect = signRect,
                        label = labels.getOrElse(labelIndex) { " " },
                        score = scores[index]
                    )
                )
            }
            return recognizedSigns
        }
    }

    companion object {
        private val globLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        private val globClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        private val globScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        private val numDetections = FloatArray(1)

        private val outputTFContainer = hashMapOf(
            0 to globLocations,
            1 to globClasses,
            2 to globScores,
            3 to numDetections
        )
    }
}