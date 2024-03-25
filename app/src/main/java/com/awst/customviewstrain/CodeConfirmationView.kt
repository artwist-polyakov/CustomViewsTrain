package com.awst.customviewstrain

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
) : LinearLayout(context, attrs, defStyleAttr), View.OnKeyListener {
    private var codeLength = 4
    private var style: Style
    private var callback: ((String) -> Unit)? = null
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
                val textSizePx = getDimension(R.styleable.CodeConfirmationView_symbolTextSize,
                        context.resources.getDimension(R.dimen.symbol_view_text_size))
                val textColor = getColor(R.styleable.CodeConfirmationView_symbolTextColor,
                        context.resources.getColor(R.color.symbol_view_text_color))
                style = Style(
                    codeLength = codeLength,
                    symbolsSpacing =
                    getDimensionPixelSize(
                        R.styleable.CodeConfirmationView_symbolsSpacing,
                        DEFAULT_SYMBOLS_SPACING
                    ),
                    symbolViewStyle = SymbolView.Style(
                        width = symbolWidth,
                        height = symbolHeight,
                        backgroundColor = Color.WHITE, // Например, белый фон
                        textColor = textColor, // Чёрный текст
                        borderWidth = 2f,
                        borderColor = Color.BLACK
                    )
                )
            } finally {
                recycle()
            }
        }
        isFocusable = true
        isFocusableInTouchMode = true
        setOnKeyListener(this)
        updateState()

        if (isInEditMode) {
            // Fill the view with demo data when viewing this view in Android Studio's editor
            repeat(codeLength) {
                enteredCode += 0.toString()
            }
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val char = event.unicodeChar.toChar()
            if (char.isDigit()) {
                if (enteredCode.length < codeLength) {
                    enteredCode += char
                    callback?.let {
                        it(enteredCode)
                    }
                    return true // указываем, что мы обработали событие
                }
            } else if (keyCode == KeyEvent.KEYCODE_DEL &&
                enteredCode.isNotEmpty()) {
                enteredCode = enteredCode.dropLast(1)
                return true // обработали событие удаления
            }
        }
        return false // не обрабатываем событие
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    fun getCurrentCode(): String {
        return enteredCode
    }

    fun setCode(code: String) {
        enteredCode = code
    }

    fun startEnterCode() {
        Log.d("MainActivity", "showKeyboard")
        requestFocus()
        postDelayed({
            showKeyboard()
        }, 1000)
    }

    fun stopEnterCode() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun setCallback(callback: ((String) -> Unit)?) {
        this.callback = callback
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
        val symbolStyle = SymbolView.Style(
            width = style.symbolViewStyle.width,
            height = style.symbolViewStyle.height,
            backgroundColor = Color.WHITE, // Например, белый фон
            textColor = Color.BLACK, // Чёрный текст
            borderWidth = 2f,
            borderColor = Color.BLACK // Чёрной границы
        )
        for (i in 0 until codeLength) {
            val symbolView = SymbolView(context, symbolStyle)
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

    internal fun View.showKeyboard() {
        Log.d("MainActivity", "showKeyboard")
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        imm.showSoftInput(this, 0)
    }

    companion object {
        internal const val DEFAULT_CODE_LENGTH = 4
        internal const val DEFAULT_SYMBOLS_SPACING = 8
        internal const val DEFAULT_SYMBOL_WIDTH = 48
        internal const val DEFAULT_SYMBOL_HEIGHT = 48
    }
}