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
import kotlin.math.sign
import kotlin.properties.Delegates

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val lightColors = listOf(Color.RED, Color.YELLOW, Color.YELLOW)
    private val darkColors = listOf(Color.CYAN, Color.BLUE, Color.MAGENTA)
    private val allColors = lightColors + darkColors

    private val rectPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private var rotation = 0

    private var signs by Delegates.observable<List<RecognizedSign>>(listOf()) { _, _, _ ->
        postInvalidate()
    }

    fun updateSigns(signs: List<RecognizedSign>): List<RecognizedSign> {
        this.signs = signs
        for (index in signs.indices) {
            val color = allColors.getOrElse(index) { Color.WHITE }
            signs[index].attachColor(color)
        }
        return signs
    }

    fun setRotation(rotation: Int) {
        this.rotation = rotation
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        transformSignsRect(canvas)
        for (index in signs.indices) {
            rectPaint.color = signs[index].getColor()
            drawRect(signs[index].rect, canvas)
        }
    }

    private fun transformSignsRect(canvas: Canvas) {
        val listOfRect = signs.map { it.rect }
        ImageTransformer.transformToCameraInputSize(listOfRect, rotation)
        ImageTransformer.transformForDrawing(
            listOfRect,
            canvas.width,
            canvas.height,
            rotation
        )
    }

    private fun drawRect(rect: RectF, canvas: Canvas) {
        canvas.drawRect(rect, rectPaint)
    }
}