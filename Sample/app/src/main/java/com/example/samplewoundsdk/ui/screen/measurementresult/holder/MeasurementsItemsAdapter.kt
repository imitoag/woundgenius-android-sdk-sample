package com.example.samplewoundsdk.ui.screen.measurementresult.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleAppItemSampleMeasurementResultBinding
import com.example.samplewoundsdk.ui.screen.view.SampleMeasurementResultItemView
import com.example.woundsdk.data.pojo.measurement.MeasurementMetadata
import com.example.woundsdk.di.WoundGeniusSDK
import java.text.DecimalFormat
import java.util.Locale

class MeasurementsItemsAdapter(
    private val needContinue: Boolean,
    private val onDepthChange: (List<Double?>) -> Unit
) : RecyclerView.Adapter<MeasurementsItemsAdapter.ViewHolder>() {

    private val decimalFormat = DecimalFormat("0.0#")
    private val depthList = ArrayList<Double?>()

    val list = ArrayList<MeasurementMetadata>()

    fun setData(items: List<MeasurementMetadata>) {
        list.clear()
        list.addAll(items)
        depthList.clear()
        depthList.addAll(items.map { it.depth })
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = SampleAppItemSampleMeasurementResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(list[position])

    inner class ViewHolder(private val itemBinding: SampleAppItemSampleMeasurementResultBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemBinding.apply {
                measurementresultMRIV.onTextChanged(object :
                    SampleMeasurementResultItemView.SampleMeasurementResultItemView {
                    override fun onTextChanged(value: String) {
                        var depth = value.toDoubleOrNull()
                        depth = String.format(Locale.UK, "%.2f", depth).toDoubleOrNull()
                        depthList[bindingAdapterPosition] = depth
                        onDepthChange(depthList)
                    }
                })
            }
        }

        fun bind(item: MeasurementMetadata) {
            itemBinding.apply {
                val name = root.context.getString(R.string.WOUND_GENIUS_SDK_WOUND_ITEM_NUMBER)
                    .replace(WOUND_ITEM_PATTERN, (bindingAdapterPosition + 1).toString())

                var backgroundColor: Int? = null

                backgroundColor = WoundGeniusSDK.getLightBackgroundColor()?.let {
                    itemBinding.root.context.getColor(it.toInt())
                } ?: itemBinding.root.context?.getColor(
                    R.color.sample_app_background
                )
                backgroundColor?.let {
                    measurementResultItemCL.setBackgroundColor(it)
                }

                measurementresultMRIV.tag = name
                measurementresultMRIV.setMeasurementBlockValues(
                    needContinue = needContinue,
                    name = name,
                    areaValue = root.context.getString(
                        R.string.WOUND_GENIUS_SDK_cm_square,
                        decimalFormat.format(item.area)
                    ),
                    circumferenceValue = root.context.getString(
                        R.string.WOUND_GENIUS_SDK_cm,
                        decimalFormat.format(item.circumference)
                    ),
                    lengthValue = root.context.getString(
                        R.string.WOUND_GENIUS_SDK_cm,
                        decimalFormat.format(item.length)
                    ),
                    widthValue = root.context.getString(
                        R.string.WOUND_GENIUS_SDK_cm,
                        decimalFormat.format(item.width)
                    ),
                    depth = item.depth
                )
            }
        }

    }

    companion object {
        private const val WOUND_ITEM_PATTERN = "%d"
    }
}