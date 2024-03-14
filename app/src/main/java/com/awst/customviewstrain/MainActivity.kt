package com.awst.customviewstrain

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val button = findViewById<Button>(R.id.button)
//        button.setOnClickListener {
//            val stepProgressView = findViewById<StepProgressView>(R.id.stepProgressView)
//            stepProgressView.increaseCurrentStep()
//        }

        val codeConfirmationView =
            findViewById<CodeConfirmationView>(R.id.codeView)
        codeConfirmationView.setBackgroundColor(Color.TRANSPARENT)
        val invisibleEditText = findViewById<EditText>(R.id.invisible_edit_text)
        codeConfirmationView.setOnClickListener {

            Log.d("MainActivity", "codeConfirmationView.setOnClickListener")
            // Запросите фокус на невидимом EditText
            invisibleEditText.requestFocus()
            // Откройте клавиатуру
            val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as
                    InputMethodManager
            imm.hideSoftInputFromWindow(invisibleEditText.windowToken, 0)
            invisibleEditText.postDelayed({
                imm.showSoftInput(invisibleEditText, 0)
            }, 100)
        }
        invisibleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, after: Int, p3: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before:
            Int, count: Int) {
                // Установите новое значение в codeConfirmationView
                s?.let {
                    codeConfirmationView.setCode( it.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) { }
        })


    }
}