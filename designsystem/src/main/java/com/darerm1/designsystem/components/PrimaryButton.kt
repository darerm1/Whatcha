package com.darerm1.designsystem.components

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import com.darerm1.designsystem.R

class PrimaryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    var buttonText: String = ""
        set(value) {
            field = value
            text = value
        }

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.PrimaryButton)
            val customText = a.getString(R.styleable.PrimaryButton_primaryButtonText)
            if (!customText.isNullOrEmpty()) {
                text = customText
            }
            a.recycle()
        }
    }
}
