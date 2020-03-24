package com.example.graduatework.ui.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.graduatework.tf.RecognizedSign
import com.example.graduatework.tools.ImageTransformer
import kotlin.properties.Delegates

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val lightColors = listOf(Color.RED, Color.YELLOW, Color.YELLOW)
    private val darkColors = listOf(Color.CYAN, Color.BLUE, Color.BLACK)
    private val allColors = lightColors + darkColors

    private val rectPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private var rotation = 0

    private var signs by Delegates.observable<List<RecognizedSign>>(listOf()) { _, _, _ ->
        postInvalidate()
    }

    fun updateSigns(signs: List<RecognizedSign>) {
        this.signs = signs
    }

    fun setRotation(rotation: Int) {
        this.rotation = rotation
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (index in signs.indices) {
            rectPaint.color = allColors.getOrElse(index) { Color.WHITE }
            drawRect(signs[index].rect, canvas)
        }
    }

    private fun drawRect(rect: RectF, canvas: Canvas) {
        ImageTransformer.transformToCameraInputSize(rect)
        ImageTransformer.transformForDrawing(
            rect,
            canvas.width,
            canvas.height,
            rotation
        )
        canvas.drawRect(rect, rectPaint)
    }
}