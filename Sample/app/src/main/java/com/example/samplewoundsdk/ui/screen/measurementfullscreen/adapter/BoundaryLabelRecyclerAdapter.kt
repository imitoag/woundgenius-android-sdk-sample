package com.example.samplewoundsdk.ui.screen.measurementfullscreen.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleAppItemSampleBoundaryLabelBinding

class BoundaryLabelRecyclerAdapter(
    private val labelList: List<Int>,
    private val onSelectedLabel: (selectedIndexes: List<Int>) -> Unit
) : RecyclerView.Adapter<BoundaryLabelRecyclerAdapter.ViewHolder>() {

    private val selectedIndexes = ArrayList(labelList.mapIndexed { index, s -> index })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = SampleAppItemSampleBoundaryLabelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount() = labelList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(labelList[position])

    inner class ViewHolder(private val itemBinding: SampleAppItemSampleBoundaryLabelBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemBinding.apply {
                itemCL.setOnClickListener {
                    if (selectedIndexes.contains(adapterPosition)) {
                        selectedIndexes.remove(adapterPosition)
                        indicatorV.isSelected = false
                    } else {
                        selectedIndexes.add(adapterPosition)
                        indicatorV.isSelected = true
                    }
                    onSelectedLabel(selectedIndexes)
                }
            }
        }

        fun bind(label: Int) {
            itemBinding.apply {
                buttonLabelACTV.text = root.context.getString(R.string.close_button_label, label)
                indicatorV.isSelected = selectedIndexes.contains(adapterPosition)
            }
        }

    }

}