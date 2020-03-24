package com.example.graduatework.tf

import android.graphics.RectF

data class RecognizedSign(
    val rect: RectF,
    val label: String,
    val score: Float
)