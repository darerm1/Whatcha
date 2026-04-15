package com.darerm1.designsystem.components

import android.content.Context
import android.util.AttributeSet
import com.darerm1.designsystem.R
import com.google.android.material.button.MaterialButton

class SecondaryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonOutlinedStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    var buttonText: String = ""
        set(value) {
            field = value
            text = value
        }

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.SecondaryButton)
            val customText = a.getString(R.styleable.SecondaryButton_secondaryButtonText)
            if (!customText.isNullOrEmpty()) {
                text = customText
            }
            a.recycle()
        }
    }
}
