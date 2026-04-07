package com.darerm1.designsystem.components

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.darerm1.designsystem.R
import com.darerm1.designsystem.databinding.ComponentSearchInputBinding

class SearchInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentSearchInputBinding.inflate(LayoutInflater.from(context), this, true)

    var query: String
        get() = binding.etQuery.text.toString()
        set(value) {
            binding.etQuery.setText(value)
            updateClearButtonVisibility(value.isNotEmpty())
        }

    var hint: String
        get() = binding.etQuery.hint.toString()
        set(value) = binding.etQuery.setHint(value)

    var onQueryChangeListener: ((String) -> Unit)? = null
    var onClearListener: (() -> Unit)? = null

    init {
        binding.etQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString().orEmpty()
                updateClearButtonVisibility(text.isNotEmpty())
                onQueryChangeListener?.invoke(text)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.ivClear.setOnClickListener {
            binding.etQuery.text.clear()
            onClearListener?.invoke()
        }

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.SearchInput)
            query = a.getString(R.styleable.SearchInput_queryText) ?: ""
            hint = a.getString(R.styleable.SearchInput_hint) ?: ""
            val clearVisible = a.getBoolean(R.styleable.SearchInput_clearButtonVisible, true)
            binding.ivClear.visibility = if (clearVisible) VISIBLE else GONE
            a.recycle()
        }
    }

    private fun updateClearButtonVisibility(hasText: Boolean) {
        binding.ivClear.visibility = if (hasText && binding.ivClear.visibility == VISIBLE) VISIBLE else GONE
    }

    fun setOnQueryTextListener(listener: (String) -> Unit) {
        onQueryChangeListener = listener
    }
}