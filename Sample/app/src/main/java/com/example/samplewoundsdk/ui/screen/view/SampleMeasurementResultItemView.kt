package com.example.samplewoundsdk.ui.screen.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleMeasurementResultItemBinding
import java.util.*

class SampleMeasurementResultItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var name = ""
    var area = ""
    var circumference = ""
    var length = ""
    var width = ""
    var depth: Double? = null
    var needContinue: Boolean = true
    private var binding: SampleMeasurementResultItemBinding
    private var callback: SampleMeasurementResultItemView? = null


    init {
        binding = SampleMeasurementResultItemBinding.inflate(LayoutInflater.from(context), this, true)
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        listenDepth()
    }

    fun setMeasurementBlockValues(
        needContinue: Boolean,
        name: String,
        areaValue: String,
        circumferenceValue: String,
        lengthValue: String,
        widthValue: String,
        depth: Double? = null
    ) {
        this.needContinue = needContinue
        this.name = name
        this.area = areaValue
        this.circumference = circumferenceValue
        this.length = lengthValue
        this.width = widthValue
        this.depth = depth

        binding.apply {
            measurementItemLabelACTV.text = name
            areaItemValueACTV.text = areaValue
            circumferenceItemValueACTV.text = circumferenceValue
            lengthItemValueACTV.text = lengthValue
            widthItemValueACTV.text = widthValue
            depth?.let {
                if (needContinue) {
                    depth.toString()
                } else {
                    val depthInMM = depth * 10
                    context.getString(
                        R.string.mm,
                        String.format(
                            Locale.UK,
                            context.getString(R.string.float_format_two_points),
                            depthInMM
                        ).toDouble().toString()
                    )
                }.let { depthValue ->
                    depthItemValueACET.setText(depthValue)
                    depthItemValueACET.setTextColor(ContextCompat.getColor(context, R.color.sample_app_measurement_value_text_color))
                }

                if (!needContinue) {
                    depthItemValueACET.isEnabled = false
                }
            }
            if (!needContinue && depth == null) {
                depthItemValueACET.visibility = View.GONE
            }
        }
    }

    private fun listenDepth() {
        binding.depthItemValueACET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if (needContinue) {
                    val depthInDouble = p0?.toString()?.toDoubleOrNull()
                    this@SampleMeasurementResultItemView.depth =
                        if (depthInDouble != null) depthInDouble / 10 else null
                    callback?.onTextChanged(depth.toString())
                }
            }
        })
    }

    fun onTextChanged(listener: SampleMeasurementResultItemView) {
        callback = listener
    }

    interface SampleMeasurementResultItemView {
        fun onTextChanged(value: String)
    }

}
