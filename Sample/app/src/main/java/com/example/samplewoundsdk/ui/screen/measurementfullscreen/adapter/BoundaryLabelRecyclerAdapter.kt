package com.example.samplewoundsdk.ui.screen.measurementfullscreen.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.samplewoundsdk.databinding.SampleAppItemBoundaryLabelBinding
import com.example.samplewoundsdk.databinding.SampleAppItemHideOutlineLabelBinding

class BoundaryLabelRecyclerAdapter(
    private val labelList: List<Int>,
    private val verticesSelectedIndexes: List<Int>,
    private val onSelectedLabel: (selectedIndexes: List<Int>) -> Unit,
    private val onHideOutlineClick: (Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedIndexes = ArrayList(verticesSelectedIndexes)
    private var isOutlineHidden = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_REGULAR -> {
                ViewHolderRegular(
                    SampleAppItemBoundaryLabelBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                )
            }

            VIEW_TYPE_HIDE_OUTLINE -> {
                ViewHolderHideOutline(
                    SampleAppItemHideOutlineLabelBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                )
            }

            else -> {
                ViewHolderRegular(
                    SampleAppItemBoundaryLabelBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                VIEW_TYPE_HIDE_OUTLINE
            }

            else -> {
                VIEW_TYPE_REGULAR
            }
        }
    }


    override fun getItemCount() = labelList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolderRegular -> {
                holder.bind(labelList[position])
            }

            is ViewHolderHideOutline -> {
                holder.bind()
            }
        }
    }

    inner class ViewHolderRegular(private val itemBinding: SampleAppItemBoundaryLabelBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemBinding.apply {
                itemCL.setOnClickListener {
                    val position = adapterPosition - 1
                    if (selectedIndexes.contains(position)) {
                        selectedIndexes.remove(position)
                        indicatorV.isSelected = false
                    } else {
                        selectedIndexes.add(position)
                        indicatorV.isSelected = true
                    }
                    onSelectedLabel(selectedIndexes)

                    if (selectedIndexes.isEmpty()) {
                        isOutlineHidden = true
                        notifyDataSetChanged()
                    } else {
                        if (selectedIndexes.size == verticesSelectedIndexes.size) {
                            isOutlineHidden = false
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        fun bind(label: Int) {
            itemBinding.apply {
                buttonLabelACTV.text =
                    itemBinding.root.context.getString(
                        com.example.samplewoundsdk.R.string.close_button_label,
                        label
                    )
                indicatorV.isSelected = selectedIndexes.contains(adapterPosition - 1)
            }
        }

    }

    inner class ViewHolderHideOutline(private val itemBinding: SampleAppItemHideOutlineLabelBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind() {
            itemBinding.apply {
                hideOutlineButtonIconACIV.isEnabled = !isOutlineHidden
                hideOutlineButtonACTV.text = if (hideOutlineButtonIconACIV.isEnabled) {
                    itemBinding.root.context.getString(com.example.samplewoundsdk.R.string.HIDE_ALL)
                } else {
                    itemBinding.root.context.getString(com.example.samplewoundsdk.R.string.SHOW_ALL)
                }
                if (isOutlineHidden) {
                    selectedIndexes.clear()
                } else {
                    selectedIndexes = ArrayList(verticesSelectedIndexes)
                }
                itemCL.setOnClickListener {
                    onHideOutlineClick(isOutlineHidden)
                    isOutlineHidden = !isOutlineHidden
                    notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HIDE_OUTLINE = 0
        private const val VIEW_TYPE_REGULAR = 1
    }

}