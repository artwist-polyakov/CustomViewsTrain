package com.awst.customviewstrain

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

class StepProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var currentStep = 0
    private var totalSteps = 0
    private val stepViews = mutableListOf<TextView>()
    private var stepActiveStyle = 0
    private var stepInactiveStyle = 0
    private var stepCurrentStyle = 0

    init {

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StepProgressView,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                stepActiveStyle = getResourceId(R.styleable.StepProgressView_stepActiveBackground, 0)
                stepInactiveStyle = getResourceId(R.styleable.StepProgressView_stepInactiveBackground, 0)
                stepCurrentStyle = getResourceId(R.styleable.StepProgressView_stepCurrentBackground, 0)
                currentStep = checkResourceIsCorrect(this, R.styleable.StepProgressView_currentStep) - 1
                totalSteps = checkResourceIsCorrect(this, R.styleable.StepProgressView_totalSteps)
                Log.d("StepProgressView", "currentStep: $currentStep, totalSteps: $totalSteps")
                Log.d("StepProgressView", "stepActiveStyle: $stepActiveStyle, stepInactiveStyle: $stepInactiveStyle, stepCurrentStyle: $stepCurrentStyle")
                initializeSteps(totalSteps)
            } finally {
                recycle()
            }
        }

    }

    private fun initializeSteps(totalSteps: Int) {
        orientation = HORIZONTAL
        val marginInPx = context.dpToPx(8)

        for (i in 0 until totalSteps) {
            val stepView = TextView(context).apply {
                text = (i + 1).toString()
                minimumHeight = 56
                minimumWidth = context.dpToPx(50)
                textAlignment = TEXT_ALIGNMENT_CENTER
                textSize = 18f
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            if (i > 0) {
                layoutParams.marginStart = marginInPx
            }

            stepView.layoutParams = layoutParams
            stepViews.add(stepView)
            addView(stepView)
            processView(stepView, i)
        }
        invalidate()
        requestLayout()
    }

    private fun updateSteps() {
        for (i in 0 until totalSteps) {
            val stepView = stepViews[i]
            processView(stepView, i)
        }
    }

    private fun setCurrentStep(step: Int) {
        if (step < 0 || step > totalSteps) {
            throw IllegalArgumentException("Step must be between 0 and $totalSteps")
        }
        currentStep = step
        updateSteps()
    }

    private fun processView(view: TextView, i: Int) {
        when (i) {
            in 0 until currentStep -> view.setBackgroundResource(stepActiveStyle)
            currentStep -> view.setBackgroundResource(stepCurrentStyle)
            else -> view.setBackgroundResource(stepInactiveStyle)
        }
    }

    private fun checkResourceIsCorrect(attrs: TypedArray, resourceId: Int): Int {
        val result = attrs.getInt(resourceId, 0)
        if (result >= 0) {
            return result
        }
        return 0

    }

    fun increaseCurrentStep() {
        if (currentStep < totalSteps-1) {
            currentStep++
            updateSteps()
        }
    }
    fun decreaseCurrentStep() {
        if (currentStep > 0) {
            currentStep--
            updateSteps()
        }
    }
}