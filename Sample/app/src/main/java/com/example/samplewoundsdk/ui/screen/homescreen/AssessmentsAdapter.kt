package com.example.samplewoundsdk.ui.screen.homescreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.databinding.SampleAppLayoutAssessmentListItemBinding
import com.example.woundsdk.data.pojo.cameramod.CameraMods

class AssessmentsAdapter(
    private val onAssessmentClick: (
        draftAssessmentModel: SampleAssessmentEntity
    ) -> Unit,
    private val onAssessmentDelete: (draftAssessmentModel: SampleAssessmentEntity) -> Unit
) : ListAdapter<SampleAssessmentEntity, AssessmentsAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = SampleAppLayoutAssessmentListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(view)
    }

    private class DiffCallback : DiffUtil.ItemCallback<SampleAssessmentEntity>() {
        override fun getChangePayload(
            oldItem: SampleAssessmentEntity,
            newItem: SampleAssessmentEntity
        ): Any =
            Any()

        override fun areItemsTheSame(
            oldItem: SampleAssessmentEntity,
            newItem: SampleAssessmentEntity
        ) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SampleAssessmentEntity,
            newItem: SampleAssessmentEntity
        ) = false
    }

    override fun onBindViewHolder(holder: AssessmentsAdapter.ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val itemBinding: SampleAppLayoutAssessmentListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.apply {
                deleteAssessmentACTV.setOnClickListener {
                    getItem(bindingAdapterPosition)?.let { it1 ->
                        onAssessmentDelete(
                            it1
                        )
                    }
                    assessmentSL.animateReset()
                }
                assessmentCL.setOnClickListener {
                    getItem(bindingAdapterPosition)?.let { it1 ->
                        onAssessmentClick(
                            it1
                        )
                    }
                }
            }
        }

        fun bind(item: SampleAssessmentEntity) {
            itemBinding.apply {
                creationDateTv.text = item.uiDatetime
                measurementMethodNameACTV.text =
                    when (item.media?.firstOrNull()?.measurementMethod) {
                        CameraMods.PHOTO_MODE -> {
                            this.root.context.getString(R.string.PHOTO)
                        }
                        CameraMods.MANUAL_MEASURE_MODE,CameraMods.MARKER_DETECT_MODE -> {
                            MEASUREMENT
                        }
                        CameraMods.VIDEO_MODE -> {
                            VIDEO
                        }

                        else -> {
                            ""
                        }
                    }

                Glide.with(this.root.context)
                    .load(item.media?.first()?.imagePath)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(assessmentImageIV)
            }
        }
    }

    companion object {
        private const val MEASUREMENT = "Measurement"
        private const val VIDEO = "Video"
    }

}