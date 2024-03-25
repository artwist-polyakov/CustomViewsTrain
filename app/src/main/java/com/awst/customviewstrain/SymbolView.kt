package com.awst.customviewstrain

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import android.view.View
import androidx.annotation.Px

// This view is created programatically by the parent SmsConfirmationView.
// It's not a part of public API so we can use custom constructor here and
// don't care about inflation from XML.
class SymbolView(context: Context, style: Style) : View(context) {

    var symbol: Char? = null
        set(value) {
            field = value
            textSize = calculateTextSize(symbol)
            invalidate()
        }

    private var cornerRadius = 8f
    private var textSizePx = 16
    private val desiredW: Int
    private val desiredH: Int

    private val backgroundRect = RectF()

    private val textPaint: Paint

    private var textSize: Size


    init {
        desiredW = style.width
        desiredH = style.height
        textSize = calculateTextSize(symbol)



        textPaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = style.textSize
            this.textAlign = Paint.Align.CENTER
            this.color = style.textColor
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Здесь обновляем backgroundRect, чтобы он соответствовал новым размерам view
        backgroundRect.set(0f, 0f, w.toFloat(), h.toFloat())
    }


    private fun calculateTextSize(symbol: Char?): Size {
        return symbol?.let {
            val textBounds = Rect(desiredW, desiredH, desiredW, desiredH)
            textPaint.getTextBounds(it.toString(), 0, 1, textBounds)
            Size(textBounds.width(), textBounds.height())
        } ?: Size(16, 16)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSizeAndState(desiredW, widthMeasureSpec, 0)
        val h = resolveSizeAndState(desiredH, heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }


    override fun onDraw(canvas: Canvas) {


        canvas.drawText(
            symbol?.toString() ?: "_",
            backgroundRect.width() / 2,
            backgroundRect.height() / 2 + textSize.height / 2,
            textPaint
        )
    }

    data class Style(
        @Px val width: Int,
        @Px val height: Int,
        val backgroundColor: Int = Color.TRANSPARENT, // Цвет фона SymbolView
        val textColor: Int,       // Цвет текста (символа)
        val borderColor: Int,
        val textSize: Float = 16f,
    )
}