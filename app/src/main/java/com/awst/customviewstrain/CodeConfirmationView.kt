package com.awst.customviewstrain

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.children

class CodeConfirmationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {
    private var codeLength = 4
    private var style: Style
    private var enteredCode: String = ""
        set(value) {
            require(value.length <= codeLength) { "enteredCode=$value is longer than $codeLength" }
            field = value
            onChangeListener?.onCodeChange(
                code = value,
                isComplete = value.length == codeLength
            )
            updateState()
        }

    private var onChangeListener: OnChangeListener? = null

    private val symbolSubviews: Sequence<SymbolView>
        get() = children.filterIsInstance<SymbolView>()

    init {
        orientation = HORIZONTAL
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CodeConfirmationView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                codeLength =
                    getInteger(R.styleable.CodeConfirmationView_codeLength, DEFAULT_CODE_LENGTH)
                val symbolWidth = getDimensionPixelSize(
                    R.styleable.CodeConfirmationView_symbolWidth,
                    DEFAULT_SYMBOL_WIDTH
                )
                val symbolHeight = getDimensionPixelSize(
                    R.styleable.CodeConfirmationView_symbolHeight,
                    DEFAULT_SYMBOL_HEIGHT
                )
                style = Style(
                    codeLength = codeLength,
                    symbolsSpacing =
                    getDimensionPixelSize(
                        R.styleable.CodeConfirmationView_symbolsSpacing,
                        DEFAULT_SYMBOLS_SPACING
                    ),
                    symbolViewStyle = SymbolView.Style(
                        width = symbolWidth,
                        height = symbolHeight
                    )
                )
            } finally {
                recycle()
            }
        }

        updateState()

        if (isInEditMode) {
            // Fill the view with demo data when viewing this view in Android Studio's editor
            repeat(codeLength) {
                enteredCode += 0.toString()
            }
        }
    }

    fun setCode(code: String) {
        enteredCode = code
    }

    private fun updateState() {
        val codeLengthChanged = codeLength != symbolSubviews.count()
        if (codeLengthChanged) {
            setupSymbolSubviews()
        }

        val viewCode = symbolSubviews.map { it.symbol }
            .filterNotNull()
            .joinToString(separator = "")
        val isViewCodeOutdated = enteredCode != viewCode
        if (isViewCodeOutdated) {
            symbolSubviews.forEachIndexed { index, view ->
                view.symbol = enteredCode.getOrNull(index)
            }
        }
    }

    private fun setupSymbolSubviews() {
        removeAllViews()

        for (i in 0 until codeLength) {
            val symbolView = SymbolView(context, style.symbolViewStyle)
            addView(symbolView)

            if (i < codeLength.dec()) {
                val space = Space(context).apply {
                    layoutParams = ViewGroup.LayoutParams(style.symbolsSpacing, 0)
                }
                addView(space)
            }
        }
    }

    fun interface OnChangeListener {
        fun onCodeChange(code: String, isComplete: Boolean)
    }

    data class Style(
        val codeLength: Int,
        val symbolViewStyle: SymbolView.Style,
        val symbolsSpacing: Int
        // You might want to add other style-related properties here
    )

    companion object {
        internal const val DEFAULT_CODE_LENGTH = 4
        internal const val DEFAULT_SYMBOLS_SPACING = 8
        internal const val DEFAULT_SYMBOL_WIDTH = 48
        internal const val DEFAULT_SYMBOL_HEIGHT = 48
    }
}