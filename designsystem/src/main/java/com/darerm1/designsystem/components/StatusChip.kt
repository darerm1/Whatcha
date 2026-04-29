package com.darerm1.designsystem.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.darerm1.designsystem.R
import com.darerm1.designsystem.databinding.ComponentStatusChipBinding

class StatusChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentStatusChipBinding.inflate(LayoutInflater.from(context), this, true)

    var status: String = STATUS_NOT_SET
        set(value) {
            field = value
            applyStatusStyle(value)
        }

    var chipText: String = ""
        set(value) {
            field = value
            binding.chip.text = value
        }

    var onChipClickListener: (() -> Unit)? = null

    init {
        binding.chip.setOnClickListener {
            onChipClickListener?.invoke()
        }

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.StatusChip)
            val statusInt = a.getInt(R.styleable.StatusChip_status, 3)
            status = intToStatus(statusInt)
            chipText = a.getString(R.styleable.StatusChip_chipText) ?: ""
            a.recycle()
        }
    }

    private fun applyStatusStyle(statusValue: String) {
        val colorRes = when (statusValue) {
            STATUS_PLANNED -> R.color.ds_status_planned
            STATUS_COMPLETED -> R.color.ds_status_completed
            STATUS_ABANDONED -> R.color.ds_status_abandoned
            else -> R.color.ds_status_not_set
        }
        val color = ContextCompat.getColor(context, colorRes)
        binding.chip.chipBackgroundColor = ColorStateList.valueOf(color)
        binding.chip.setTextColor(ContextCompat.getColor(context, R.color.ds_white))
    }

    private fun intToStatus(value: Int): String = when (value) {
        0 -> STATUS_PLANNED
        1 -> STATUS_COMPLETED
        2 -> STATUS_ABANDONED
        else -> STATUS_NOT_SET
    }

    companion object {
        const val STATUS_PLANNED = "planned"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_ABANDONED = "abandoned"
        const val STATUS_NOT_SET = "not_set"
    }
}
