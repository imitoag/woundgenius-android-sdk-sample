package com.example.samplewoundsdk.ui.screen.measurementresult.holder

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.pojo.measurement.MeasurementMetadata
import com.example.samplewoundsdk.data.pojo.media.MediaModel
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_AREA_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LENGTH_PREFIX
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LINE_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_OUTLINE_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_WIDTH_PREFIX
import com.example.samplewoundsdk.databinding.SampleAppActivityMeasurementResultHolderBinding
import com.example.samplewoundsdk.ui.screen.base.AbsActivity
import com.example.woundsdk.di.WoundGeniusSDK
import java.io.Serializable
import java.text.DecimalFormat

class MeasurementResultHolderActivity : AbsActivity<MeasurementResultHolderViewModel>() {

    private val args by lazy { intent.getSerializableExtra(EXTRA_ARGS) as? Args }

    override fun provideViewModelClass() = MeasurementResultHolderViewModel::class
    override fun provideLayoutId() = R.layout.sample_app_activity_measurement_result_holder

    lateinit var binding: SampleAppActivityMeasurementResultHolderBinding

    override fun initListeners() {
        binding.editSelectionButtonACTV.setOnClickListener {
            finish()
        }
    }

    private lateinit var assessmentImagesPagerAdapter: AssessmentImagesPagerAdapter

    private val decimalFormat = DecimalFormat("0.0#")

    private val measurementsItemsAdapter by lazy {
        args?.run {
            MeasurementsItemsAdapter(
                needContinue = false,
                onDepthChange = { depthList ->
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SampleAppActivityMeasurementResultHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.measurementsItemsRV.adapter = measurementsItemsAdapter
        viewModel?.apply {
            setUpAssessmentImagePager(
                args?.assessmentEntity?.media ?: emptyList()
            )
        }
        prepareMediaMetaDataResultUi()
    }

    private fun prepareMediaMetaDataResultUi() {
        args?.apply {
            val metadataList = ArrayList<MeasurementMetadata>()
            val metadata =
                assessmentEntity.media?.find { it.metadata?.measurementData?.annotationList != null && it.metadata?.measurementData?.calibration != null }?.metadata
            if (metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_AREA_TYPE } != null) {
                val areaAnnotationItem =
                    metadata.measurementData.annotationList.find { it?.type == ANNOTATION_AREA_TYPE }
                val pointsList = areaAnnotationItem?.points
                val lines =
                    metadata.measurementData.annotationList.filter { it?.type == ANNOTATION_LINE_TYPE }
                val widthLine = lines.find { it?.prefix == ANNOTATION_WIDTH_PREFIX }
                val lengthLine = lines.find { it?.prefix == ANNOTATION_LENGTH_PREFIX }

                val widthA =
                    pointsList?.indexOfFirst { it.pointX == widthLine?.pointA?.pointX && it.pointY == widthLine?.pointA?.pointY }
                val widthB =
                    pointsList?.indexOfFirst { it.pointX == widthLine?.pointB?.pointX?.toInt() && it.pointY == widthLine?.pointB?.pointY?.toInt() }
                val lengthA =
                    pointsList?.indexOfFirst { it.pointX == lengthLine?.pointA?.pointX && it.pointY == lengthLine?.pointA?.pointY }
                val lengthB =
                    pointsList?.indexOfFirst { it.pointX == lengthLine?.pointB?.pointX?.toInt() && it.pointY == lengthLine?.pointB?.pointY?.toInt() }

                metadataList.add(
                    MeasurementMetadata(
                        area = areaAnnotationItem?.area ?: 0.0,
                        circumference = areaAnnotationItem?.circumference ?: 0.0,
                        length = lengthLine?.length ?: 0.0,
                        width = widthLine?.width ?: 0.0,
                        depth = areaAnnotationItem?.depth ?: 0.0,
                        vertices = pointsList?.map {
                            MeasurementMetadata.Point(it.pointX ?: 0, it.pointY ?: 0)
                        } ?: emptyList(),
                        lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                        widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                        countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                            ?: 1.0)).toInt()
                    )
                )
            }
            metadata?.measurementData?.annotationList?.filter { it?.type == ANNOTATION_OUTLINE_TYPE }
                ?.forEach { annotationItem ->
                    val pointsList = annotationItem?.points
                    val widthLine = Pair(annotationItem?.widthPointA, annotationItem?.widthPointB)
                    val lengthLine =
                        Pair(annotationItem?.lengthPointA, annotationItem?.lengthPointB)

                    val widthA =
                        pointsList?.indexOfFirst { it.pointX == widthLine.first?.pointX?.toInt() && it.pointY == widthLine.first?.pointY?.toInt() }
                    val widthB =
                        pointsList?.indexOfFirst { it.pointX == widthLine.second?.pointX?.toInt() && it.pointY == widthLine.second?.pointY?.toInt() }
                    val lengthA =
                        pointsList?.indexOfFirst { it.pointX == lengthLine.first?.pointX?.toInt() && it.pointY == lengthLine.first?.pointY?.toInt() }
                    val lengthB =
                        pointsList?.indexOfFirst { it.pointX == lengthLine.second?.pointX?.toInt() && it.pointY == lengthLine.second?.pointY?.toInt() }

                    metadataList.add(
                        MeasurementMetadata(
                            area = annotationItem?.area ?: 0.0,
                            circumference = annotationItem?.circumference ?: 0.0,
                            length = annotationItem?.length ?: 0.0,
                            width = annotationItem?.width ?: 0.0,
                            depth = annotationItem?.depth ?: 0.0,
                            vertices = pointsList?.map {
                                MeasurementMetadata.Point(it.pointX ?: 0, it.pointY ?: 0)
                            } ?: emptyList(),
                            lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                            widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                            countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                ?: 1.0)).toInt()
                        )
                    )
                }
            setUpMetadataUi(metadataList)
        }
    }

    private fun setUpMetadataUi(metadataList: List<MeasurementMetadata>) {
        args?.apply {
            measurementsItemsAdapter?.setData(metadataList)

            val allVertexesList = ArrayList<List<Point>>()
            val widthIndexes = ArrayList<Pair<Int?, Int?>>()
            val lengthIndexes = ArrayList<Pair<Int?, Int?>>()
            val areaList = ArrayList<Double>()
            var totalArea = 0.0
            var totalCircumference = 0.0
            metadataList.forEachIndexed { index, boundaryMetadata ->
                boundaryMetadata.apply {
                    boundaryMetadata.vertices?.let {
                        allVertexesList.add(it.map {
                            Point(
                                (it.x),
                                (it.y)
                            )
                        })
                    }
                    widthIndexes.add(
                        Pair(
                            boundaryMetadata.widthLine?.pointAIndex,
                            boundaryMetadata.widthLine?.pointBIndex
                        )
                    )
                    lengthIndexes.add(
                        Pair(
                            boundaryMetadata.lengthLine?.pointAIndex,
                            boundaryMetadata.lengthLine?.pointBIndex
                        )
                    )
                    boundaryMetadata.area?.let { areaList.add(it) }
                    if (area != null) {
                        totalArea += area!!
                    }
                    if (circumference != null) {
                        totalCircumference += circumference!!
                    }
                }
            }

            args?.apply {
                binding.apply {

                    if (assessmentEntity.isStoma == false){
                        stomaContainerCL.isVisible = false
                        measurementsItemsRV.isVisible = true

                        if (areaList.size == 1) {
                            woundContainerCL.isVisible = false
                            totalCircumferenceValueACTV.isVisible = false
                            circumferenceACTV.isVisible = false
                        } else if (areaList.size > 1) {
                            woundContainerCL.isVisible = true
                            totalCircumferenceValueACTV.isVisible = true
                            circumferenceACTV.isVisible = true
                        } else {
                            woundContainerCL.isVisible = false
                        }
                        totalAreaValueACTV.text =
                            getString(com.example.woundsdk.R.string.cm_square, decimalFormat.format(totalArea))
                        totalCircumferenceValueACTV.text =
                            getString(com.example.woundsdk.R.string.cm_square, decimalFormat.format(totalCircumference))
                    } else {
                        woundContainerCL.isVisible = false
                        stomaContainerCL.isVisible = true
                        measurementsItemsRV.isVisible = false

                        metadataList.firstOrNull().let {
                            stomaDiameterValueACTV.text = if (it?.length != null) {
                                getString(
                                    com.example.woundsdk.R.string.mm,
                                    decimalFormat.format((it.length ?: 0.0) * 10.0)
                                )
                            } else getString(com.example.woundsdk.R.string.not_a_number)
                            if (it?.depth != null) {
                                stomaHeightValueACET.setText(
                                    decimalFormat.format(
                                        it.depth ?: 0.0
                                    )
                                )
                            } else {
                                stomaHeightMmACTV.isVisible = false
                                stomaHeightValueACET.setHint(com.example.woundsdk.R.string.TAP_TO_ADD_HEIGHT)
                            }
                        }
                    }

                }
            }
        }
    }

    private fun setUpAssessmentImagePager(draftMediaList: List<MediaModel>) {
        binding.apply {
            (imagesPagerVP2.getChildAt(0) as RecyclerView).layoutManager?.isItemPrefetchEnabled =
                false
            (imagesPagerVP2.getChildAt(0) as RecyclerView).setItemViewCacheSize(1)
            imagesPagerVP2.adapter =
                AssessmentImagesPagerAdapter(
                    this@MeasurementResultHolderActivity,
                    ArrayList(draftMediaList)
                ).apply {
                    assessmentImagesPagerAdapter = this
                }
            indicatorSPI.attachToPager(imagesPagerVP2)
            indicatorSPI.invalidate()
            indicatorSPI.reattach()
            if (draftMediaList.size == 1){
                indicatorSPI.isVisible = false
            }
            if (draftMediaList.isNotEmpty()) {
                imagesPagerVP2.setCurrentItem(0, true)
            }
            imagesPagerVP2.beginFakeDrag()
            imagesPagerVP2.fakeDragBy(150f)
            imagesPagerVP2.endFakeDrag()
        }
    }


    override fun onKeyboardOpen() {}

    override fun onKeyboardClose() {}

    companion object {

        private const val EXTRA_ARGS = "KEY_ARGS"

        private data class Args(
            val assessmentEntity: SampleAssessmentEntity
        ) : Serializable

        fun open(
            context: Context,
            assessmentEntity: SampleAssessmentEntity
        ) = context.startActivity(
            Intent(context, MeasurementResultHolderActivity::class.java).apply {
                putExtra(EXTRA_ARGS, Args(assessmentEntity))
            }
        )

    }

}
