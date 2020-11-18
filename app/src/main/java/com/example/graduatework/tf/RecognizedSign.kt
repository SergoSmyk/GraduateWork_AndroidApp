package com.example.graduatework.tf

import android.graphics.Color
import android.graphics.RectF

data class RecognizedSign(
    val rect: RectF,
    val label: String,
    val score: Float
) {
    private var color = Color.RED

    fun attachColor(color: Int) {
        this.color = color
    }

    fun getColor(): Int  = color
}