package com.awst.customviewstrain

import android.content.Context
import android.graphics.Canvas
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

    private var backgroundPaint: Paint
    private var borderPaint: Paint
    private val textPaint: Paint

    private var textSize: Size

    init {
        desiredW = style.width
        desiredH = style.height

        textSize = calculateTextSize(symbol)

        backgroundPaint = Paint().apply {
            this.style = Paint.Style.FILL
            color = style.backgroundColor

        }

        borderPaint = Paint().apply {
//            this.isAntiAlias = true
            this.style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeWidth = style.borderWidth // Ширина границы из настроек стиля
            color = style.borderColor // Цвет границы из настроек стиля
        }

        textPaint = Paint().apply {
            this.isAntiAlias = true
            this.textSize =
                context.resources.getDimension(R.dimen.symbol_view_text_size) // размер из
            this.textAlign = Paint.Align.CENTER
            this.color =
                context.resources.getColor(R.color.symbol_view_text_color)  // цвет из ресурсо
        }
    }

    private fun calculateTextSize(symbol: Char?): Size {
        return symbol?.let {
            val textBounds = Rect()
            textPaint.getTextBounds(it.toString(), 0, 1, textBounds)
            Size(textBounds.width(), textBounds.height())
        } ?: Size(0, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSizeAndState(desiredW, widthMeasureSpec, 0)
        val h = resolveSizeAndState(desiredH, heightMeasureSpec, 0)
        setMeasuredDimension(w, h)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val borderWidthHalf = borderPaint.strokeWidth / 2
        backgroundRect.left = borderWidthHalf
        backgroundRect.top = borderWidthHalf
        backgroundRect.right = measuredWidth.toFloat() - borderWidthHalf
        backgroundRect.bottom = measuredHeight.toFloat() - borderWidthHalf
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(
            backgroundRect,
            cornerRadius,
            cornerRadius,
            backgroundPaint
        )

        canvas.drawRoundRect(
            backgroundRect,
            cornerRadius,
            cornerRadius,
            borderPaint
        )

        canvas.drawText(
            symbol?.toString() ?: "",
            backgroundRect.width() / 2 + borderPaint.strokeWidth / 2,
            backgroundRect.height() / 2 + textSize.height / 2 + borderPaint.strokeWidth / 2,
            textPaint
        )
    }

    data class Style(
        @Px val width: Int,
        @Px val height: Int,
        val backgroundColor: Int, // Цвет фона SymbolView
        val textColor: Int,       // Цвет текста (символа)
        val borderWidth: Float,
        val borderColor: Int
    )
}