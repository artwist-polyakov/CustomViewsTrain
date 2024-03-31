package com.awst.customviewstrain

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
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
                val textSizePx = getDimension(
                    R.styleable.CodeConfirmationView_symbolTextSize,
                    context.resources.getDimension(R.dimen.symbol_view_text_size)
                )
                val textColor = getColor(
                    R.styleable.CodeConfirmationView_symbolTextColor,
                    context.resources.getColor(R.color.symbol_view_text_color)
                )
                val dividerColor = getColor(
                    R.styleable.CodeConfirmationView_borderColor,
                    Color.BLACK
                )
                val dividerWidth = getDimensionPixelSize(
                    R.styleable.CodeConfirmationView_borderWidth,
                    2
                )
                val cornerRadius = getDimension(
                    R.styleable.CodeConfirmationView_backgroundCornerRadius,
                    25f
                )
                val backgroundColor = getColor(
                    R.styleable.CodeConfirmationView_backgroundColour,
                    Color.GRAY
                )

                style = Style(
                    codeLength = codeLength,
                    symbolsSpacing =
                    getDimensionPixelSize(
                        R.styleable.CodeConfirmationView_symbolsSpacing,
                        DEFAULT_SYMBOLS_SPACING
                    ),
                    dividerColor = dividerColor,
                    dividerWidth = dividerWidth,
                    cornerRadius = cornerRadius,
                    background = backgroundColor,
                    symbolViewStyle = SymbolView.Style(
                        width = symbolWidth,
                        height = symbolHeight,
                        textSize = textSizePx,
                        textColor = textColor, // Чёрный текст
                        borderColor = Color.BLACK
                    )
                )
                updateState()
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
        setupBackground()
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
                enteredCode.isNotEmpty()
            ) {
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
        post{
            showKeyboard()
        }
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
        Log.d("MainActivity", "updateState: enteredCode=$enteredCode, viewCode=$viewCode")
    }

    private fun setupSymbolSubviews() {
        removeAllViews()
        val symbolStyle = SymbolView.Style(
            textSize = style.symbolViewStyle.textSize,
            width = style.symbolViewStyle.width,
            height = style.symbolViewStyle.height,
            textColor = Color.BLACK, // Чёрный текст
            borderColor = Color.BLACK // Чёрной границы
        )
        for (i in 0 until codeLength) {
            val symbolView = SymbolView(context, symbolStyle)
            addView(symbolView)

            if (i < codeLength) {
                val space = Space(context).apply {
                    layoutParams = ViewGroup.LayoutParams(style.symbolsSpacing, 0)
                }
                addView(space)
            }
            if (i < codeLength - 1) {
                val divider = View(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        style.dividerWidth,
                        style.symbolViewStyle.height
                    )
                    setBackgroundColor(style.dividerColor)
                }
                addView(divider)
            }
        }
    }

    fun interface OnChangeListener {
        fun onCodeChange(code: String, isComplete: Boolean)
    }

    private fun setupBackground() {
        val backgroundDrawable = GradientDrawable().apply {
            setColor(style.background) // Указываем цвет фона
            cornerRadius = style.cornerRadius // Устанавливаем радиус скругления
        }
        background = backgroundDrawable
    }


    data class Style(
        val codeLength: Int,
        val symbolViewStyle: SymbolView.Style,
        val symbolsSpacing: Int,
        val dividerColor: Int = Color.BLACK,       // цвет разделителя
        val dividerWidth: Int = 2,
        val cornerRadius: Float = 25f,
        val borderColor: Int = Color.BLACK,
        val background: Int = Color.GRAY,
        val symbolFont: String? = null
        // You might want to add other style-related properties here
    )

    internal fun View.showKeyboard() {
        Log.d("MainActivity", "showKeyboard")
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // При касании CodeConfirmationView показываем клавиатуру
            requestFocus()
            showKeyboard()
        }
        return super.onTouchEvent(event)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        outAttrs.actionLabel = null
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE
        return BaseInputConnection(this, false)
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true // Указываем, что наша view может вести себя как text editor
    }


    companion object {
        internal const val DEFAULT_CODE_LENGTH = 4
        internal const val DEFAULT_SYMBOLS_SPACING = 8
        internal const val DEFAULT_SYMBOL_WIDTH = 48
        internal const val DEFAULT_SYMBOL_HEIGHT = 48
    }
}