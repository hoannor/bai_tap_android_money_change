package com.example.moneychange

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sourceCurrencySpinner: Spinner
    private lateinit var targetCurrencySpinner: Spinner
    private lateinit var sourceAmountEditText: EditText
    private lateinit var targetAmountEditText: EditText
    private lateinit var exchangeRateTextView: TextView

    private val exchangeRates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.923,
        "JPY" to 110.0,
        "VND" to 23000.0,
        "GBP" to 0.76,
        "AUD" to 1.35,
        "CAD" to 1.27,
        "CHF" to 0.91,
        "CNY" to 6.45,
        "INR" to 74.57,
        "KRW" to 1180.0,
        "SGD" to 1.36
    )

    // Biến để kiểm soát hướng chuyển đổi
    private var isSourceFocused = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sourceCurrencySpinner = findViewById(R.id.sourceCurrencySpinner)
        targetCurrencySpinner = findViewById(R.id.targetCurrencySpinner)
        sourceAmountEditText = findViewById(R.id.sourceAmountEditText)
        targetAmountEditText = findViewById(R.id.targetAmountEditText)
        exchangeRateTextView = findViewById(R.id.exchangeRateTextView)

        setupSpinners()
        setupListeners()
    }

    private fun setupSpinners() {
        val currencies = exchangeRates.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        sourceCurrencySpinner.adapter = adapter
        targetCurrencySpinner.adapter = adapter
    }

    private fun setupListeners() {
        // Lắng nghe thay đổi trong EditText của số tiền nguồn
        sourceAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isSourceFocused) {
                    convertCurrency(isSourceToTarget = true)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Lắng nghe thay đổi trong EditText của số tiền đích
        targetAmountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isSourceFocused) {
                    convertCurrency(isSourceToTarget = false)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Lắng nghe thay đổi khi người dùng chọn loại tiền
        sourceCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateExchangeRate()
                convertCurrency(isSourceToTarget = true)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        targetCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateExchangeRate()
                convertCurrency(isSourceToTarget = true)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Lắng nghe sự thay đổi focus của EditText
        sourceAmountEditText.setOnFocusChangeListener { _, hasFocus ->
            isSourceFocused = hasFocus
        }

        targetAmountEditText.setOnFocusChangeListener { _, hasFocus ->
            isSourceFocused = !hasFocus
        }
    }

    private fun convertCurrency(isSourceToTarget: Boolean) {
        if (isSourceToTarget) {
            val sourceAmountText = sourceAmountEditText.text.toString()
            if (sourceAmountText.isNotEmpty()) {
                val sourceAmount = sourceAmountText.toDoubleOrNull()
                if (sourceAmount != null) {
                    val convertedAmount = convertAmount(sourceAmount, isSourceToTarget)
                    targetAmountEditText.removeTextChangedListener(targetAmountWatcher)
                    targetAmountEditText.setText("%.2f".format(convertedAmount))
                    targetAmountEditText.addTextChangedListener(targetAmountWatcher)
                } else {
                    targetAmountEditText.setText("")
                }
            } else {
                targetAmountEditText.setText("")
            }
        } else {
            val targetAmountText = targetAmountEditText.text.toString()
            if (targetAmountText.isNotEmpty()) {
                val targetAmount = targetAmountText.toDoubleOrNull()
                if (targetAmount != null) {
                    val convertedAmount = convertAmount(targetAmount, isSourceToTarget)
                    sourceAmountEditText.removeTextChangedListener(sourceAmountWatcher)
                    sourceAmountEditText.setText("%.2f".format(convertedAmount))
                    sourceAmountEditText.addTextChangedListener(sourceAmountWatcher)
                } else {
                    sourceAmountEditText.setText("")
                }
            } else {
                sourceAmountEditText.setText("")
            }
        }
    }

    private fun convertAmount(amount: Double, isSourceToTarget: Boolean): Double {
        val sourceCurrency = sourceCurrencySpinner.selectedItem.toString()
        val targetCurrency = targetCurrencySpinner.selectedItem.toString()
        val sourceRate = exchangeRates[sourceCurrency] ?: 1.0
        val targetRate = exchangeRates[targetCurrency] ?: 1.0

        return if (isSourceToTarget) {
            (amount / sourceRate) * targetRate
        } else {
            (amount / targetRate) * sourceRate
        }
    }

    private fun updateExchangeRate() {
        val sourceCurrency = sourceCurrencySpinner.selectedItem.toString()
        val targetCurrency = targetCurrencySpinner.selectedItem.toString()

        val sourceRate = exchangeRates[sourceCurrency] ?: 1.0
        val targetRate = exchangeRates[targetCurrency] ?: 1.0
        val rate = targetRate / sourceRate

        exchangeRateTextView.text = "1 $sourceCurrency = %.3f $targetCurrency".format(rate)
    }

    // Định nghĩa các TextWatcher để sử dụng trong convertCurrency
    private val sourceAmountWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isSourceFocused) convertCurrency(isSourceToTarget = true)
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private val targetAmountWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (!isSourceFocused) convertCurrency(isSourceToTarget = false)
        }
        override fun afterTextChanged(s: Editable?) {}
    }
}
