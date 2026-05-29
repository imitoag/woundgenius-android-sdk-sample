package io.imito.woundgenius.sample.ui.screen.homescreen

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.databinding.SampleAppLayoutAssessmentListItemBinding
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.internal.utils.media.MediaIUtils.isVideoFile

class AssessmentsAdapter(
    private val onAssessmentClick: (
        draftAssessmentModel: SampleAssessmentEntity
    ) -> Unit,
    private val onAssessmentDelete: (draftAssessmentModel: SampleAssessmentEntity) -> Unit,
    private val onAssessmentShare: (draftAssessmentModel: SampleAssessmentEntity) -> Unit
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
                shareAssessmentACIV.setOnClickListener {
                    getItem(bindingAdapterPosition)?.let { it1 ->
                        onAssessmentShare(it1)
                    }
                }
            }
        }

        fun bind(item: SampleAssessmentEntity) {
            itemBinding.apply {

                var primaryButtonColor: Int? = null
                var textColor: Int? = null
                var dividerColor: Int? = null
                var measurementValueColor: Int? = null

                WoundGeniusSDK.getConfiguration().valueDividersColor?.let {
                    dividerColor = this.root.context?.getColor(
                        it.toInt()
                    )
                }

                primaryButtonColor = WoundGeniusSDK.getConfiguration().primaryButtonColor?.let {
                    this.root.context?.  getColor(
                        it.toInt()
                    )
                } ?: this.root.context?.getColor(
                    io.imito.woundgenius.sample.R.color.sample_app_button_color
                )

                WoundGeniusSDK.getConfiguration().measurementResultColor?.let {
                    measurementValueColor = this.root.context?.getColor(
                        it.toInt()
                    )
                }
                WoundGeniusSDK.getConfiguration().textColor?.let {
                    textColor = this.root.context?.getColor(
                        it.toInt()
                    )
                }

                textColor?.let {
                    measurementMethodNameACTV.setTextColor(it)
                    assessmentImageIV.imageTintList = ColorStateList.valueOf(textColor)
                }
                measurementValueColor?.let {
                    creationDateTv.setTextColor(it)
                }
                dividerColor?.let {
                    view.setBackgroundColor(it)
                }

                primaryButtonColor?.let {
                    shareAssessmentACIV.imageTintList = ColorStateList.valueOf(primaryButtonColor)
                }


                creationDateTv.text = item.uiDatetime

                val hasImage = !item.media.isNullOrEmpty() &&
                        !item.media?.firstOrNull()?.image.isNullOrEmpty()
                val hasMeasurement =
                    item.media?.find { it.metadata?.measurementData?.annotationList?.isNotEmpty() == true } != null
                val hasForms = !item.observationsJson.isNullOrEmpty()

                if (!hasMeasurement && !hasForms) {
                    shareAssessmentACIV.visibility = View.INVISIBLE
                }

                measurementMethodNameACTV.text = if (item.magicAssessment == true) {
                    when {
                        hasImage && hasMeasurement && hasForms -> MEASUREMENTS_AND_FORMS
                        !hasImage && !hasMeasurement -> FORMS
                        else -> IMAGE_AND_FORMS
                    }
                } else {
                    if (hasMeasurement) {
                        MEASUREMENT
                    } else {
                        if (item.media?.size == 1 && isVideoFile(item.media?.first()?.image ?: "")) {
                            VIDEO
                        } else MEDIA
                    }
                }

                if (item.magicAssessment == true && !hasImage) {
                    assessmentImageIV.setImageResource(
                        R.drawable.ic_forms_document
                    )
                } else {
                    Glide.with(this.root.context)
                        .load(item.media?.firstOrNull()?.image)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(RequestOptions())
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(assessmentImageIV)
                }


            }
        }
    }

    companion object {
        private const val MEASUREMENT = "Measurement"
        private const val MEDIA = "Media"
        private const val VIDEO = "Video"
        private const val IMAGE_AND_FORMS = "Image + Forms"
        private const val FORMS = "Forms"
        private const val MEASUREMENTS_AND_FORMS = "Measurements + Forms"
    }

}