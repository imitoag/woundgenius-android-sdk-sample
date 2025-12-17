package io.imito.woundgenius.sample.ui.screen.measurementresult.holder

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_AREA_TYPE
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_MEASUREMENT_LINE_TYPE
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LENGTH_PREFIX
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_LINE_TYPE
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_OUTLINE_TYPE
import io.imito.woundgenius.sdk.data.pojo.entity.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_WIDTH_PREFIX
import io.imito.woundgenius.sample.databinding.SampleAppActivityMeasurementResultHolderBinding
import io.imito.woundgenius.sample.ui.screen.base.AbsActivity
import io.imito.woundgenius.sdk.data.pojo.measurement.MeasurementMetadata
import io.imito.woundgenius.sdk.data.pojo.measurement.OutlineModel
import io.imito.woundgenius.sdk.di.WoundGeniusSDK
import io.imito.woundgenius.sdk.ui.screen.measurementresult.common.MeasurementsItemsAdapter
import io.imito.woundgenius.sdk.utils.DarkModeUtils.isDarkModeEnabled
import io.imito.woundgenius.sdk.utils.LandscapeUtils.isSDKSupportPortraitOnly
import io.imito.woundgenius.sdk.utils.LandscapeUtils.onConfigurationChange
import io.imito.woundgenius.sdk.utils.MeasurementMetadataUtils.groupAndIndexByType
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

        val orientation = resources.configuration.orientation

        if (!WoundGeniusSDK.getIsLandscapeSupported()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            if (isSDKSupportPortraitOnly(
                    WoundGeniusSDK.getIsLandscapeSupported(),
                    this@MeasurementResultHolderActivity
                )
            ) {
                if (orientation != Configuration.ORIENTATION_PORTRAIT) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            } else {
                val container = binding.measurementResultLayoutCL
                container.addView(object : View(this) {
                    override fun onConfigurationChanged(newConfig: Configuration?) {
                        super.onConfigurationChanged(newConfig)
                        onConfigurationChange(this@MeasurementResultHolderActivity)
                    }
                })

                if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
                }
            }
        }




        setContentView(binding.root)

        binding.measurementsItemsRV.adapter = measurementsItemsAdapter
        val draftMediaList = args?.assessmentEntity?.media ?: emptyList()
        viewModel?.apply {
            setUpAssessmentImagePager(
                args?.assessmentEntity?.media ?: emptyList(),
                args?.assessmentEntity?.stomaDocumentation ?: false
            )

            val metadata = draftMediaList[0].metadata
            prepareMediaMetaDataResultUi(metadata)
        }
        setUpUiTheme()
    }

    private fun setUpUiTheme() {
        binding.apply {

            var backgroundColor: Int? = null
            var primaryButtonColor: Int? = null
            var textColor: Int? = null
            var dividerColor: Int? = null
            var formsColor: Int? = null
            var measurementValueColor: Int? = null

            backgroundColor = WoundGeniusSDK.getLightBackgroundColor()?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_background
            )

            dividerColor = WoundGeniusSDK.getValueDividersColor()?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_light_grey
            )
            formsColor = WoundGeniusSDK.getFormsColor()?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_forms_color
            )

            measurementValueColor = WoundGeniusSDK.getMeasurementResultColor()?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_measurement_value_text_color
            )

            primaryButtonColor = WoundGeniusSDK.getPrimaryButtonColor()?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_button_color
            )
            textColor = WoundGeniusSDK.getTextColor()?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_text_color
            )

            backgroundColor?.let { backgroundColor ->
                toolbarCL.setBackgroundColor(backgroundColor)
                NSV.setBackgroundColor(backgroundColor)
                measurementResultContainerCL.setBackgroundColor(backgroundColor)
                measurementResultLayoutCL.setBackgroundColor(backgroundColor)
                measurementsCL.setBackgroundColor(backgroundColor)
                woundContainerCL.setBackgroundColor(backgroundColor)
            }
            primaryButtonColor?.let { primaryButtonColor ->
                editSelectionButtonACTV.setTextColor(primaryButtonColor)
                editSelectionButtonACTV.compoundDrawableTintList =
                    ColorStateList.valueOf(primaryButtonColor)
                indicatorSPI.selectedDotColor = primaryButtonColor
            }
            textColor?.let { textColor ->
                allAreasACTV.setTextColor(textColor)
                areaACTV.setTextColor(textColor)
                circumferenceACTV.setTextColor(textColor)
            }
            dividerColor?.let { dividerColor ->
                totalAreaValueDividerV.setBackgroundColor(dividerColor)
            }
            formsColor?.let { formsColor ->
                allAreaContainerCL.backgroundTintList = ColorStateList.valueOf(formsColor)
            }
            measurementValueColor?.let { measurementValueColor ->
                totalAreaValueACTV.setTextColor(measurementValueColor)
                totalCircumferenceValueACTV.setTextColor(measurementValueColor)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val window = this.window
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars = !isDarkModeEnabled(this@MeasurementResultHolderActivity)
    }

    private fun prepareMediaMetaDataResultUi(metadata: MediaModel.Metadata?) {
        args?.apply {
            val metadataList = ArrayList<MeasurementMetadata>()
            metadata?.measurementData?.annotationList?.sortedBy { it?.id }?.forEach { annotationItem ->
                when (annotationItem?.type) {
                    ANNOTATION_AREA_TYPE -> {
                        val pointsList = annotationItem?.points
                        val lines =
                            metadata.measurementData?.annotationList?.filter { it?.type == ANNOTATION_LINE_TYPE }
                        val widthLine = lines?.find { it?.prefix == ANNOTATION_WIDTH_PREFIX }
                        val lengthLine = lines?.find { it?.prefix == ANNOTATION_LENGTH_PREFIX }

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
                                area = annotationItem?.area ?: 0.0,
                                circumference = annotationItem?.circumference ?: 0.0,
                                length = lengthLine?.length ?: 0.0,
                                width = widthLine?.width ?: 0.0,
                                depth =  if (args?.assessmentEntity?.stomaDocumentation == true) {
                                    (annotationItem?.depth ?: 0.0) * 10
                                } else {
                                    annotationItem?.depth ?: 0.0
                                },
                                vertices = pointsList?.map {
                                    MeasurementMetadata.Point(it.pointX ?: 0, it.pointY ?: 0)
                                } ?: emptyList(),
                                lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                                widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                                countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                    ?: 1.0)).toInt(),
                                order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                                id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                type = if (args?.assessmentEntity?.stomaDocumentation == true) {
                                    OutlineModel.OutlineType.STOMA
                                } else {
                                    OutlineModel.OutlineType.WOUND
                                }
                            )
                        )
                    }
                    ANNOTATION_MEASUREMENT_LINE_TYPE -> {
                        val pointsList = annotationItem?.points

                        metadataList.add(
                            MeasurementMetadata(
                                length = annotationItem?.length ?: 0.0,
                                vertices = pointsList?.map {
                                    MeasurementMetadata.Point(it.pointX ?: 0, it.pointY ?: 0)
                                } ?: emptyList(),
                                countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                    ?: 1.0)).toInt(),
                                order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                                id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                type = OutlineModel.OutlineType.MEASUREMENT_LINE
                            )
                        )
                    }
                    ANNOTATION_OUTLINE_TYPE -> {
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
                                depth =  if (args?.assessmentEntity?.stomaDocumentation == true) {
                                    (annotationItem?.depth ?: 0.0) * 10
                                } else {
                                    annotationItem?.depth ?: 0.0
                                },
                                vertices = pointsList?.map {
                                    MeasurementMetadata.Point(it.pointX ?: 0, it.pointY ?: 0)
                                } ?: emptyList(),
                                lengthLine = MeasurementMetadata.Line(lengthA ?: -1, lengthB ?: -1),
                                widthLine = MeasurementMetadata.Line(widthA ?: -1, widthB ?: -1),
                                countPxInCm = (1.0 / (metadata.measurementData?.calibration?.unitPerPixel
                                    ?: 1.0)).toInt(),
                                order = annotationItem?.order ?: (metadataList.lastIndex + 1),
                                id = annotationItem?.id ?: (metadataList.lastIndex + 1),
                                type = if (args?.assessmentEntity?.stomaDocumentation == true) {
                                    OutlineModel.OutlineType.STOMA
                                } else {
                                    OutlineModel.OutlineType.WOUND
                                }
                            )
                        )
                    }
                }
            }
            setUpMetadataUi(metadataList)
        }
    }

    private fun setUpMetadataUi(metadataList: List<MeasurementMetadata>) {
        args?.apply {
            val indexedMetadataList = groupAndIndexByType(metadataList)


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
                    args?.apply {

                        measurementsItemsRV.isVisible = true

                        if (areaList.size <= 1) {
                            woundContainerCL.isVisible = false
                            totalCircumferenceValueACTV.isVisible = false
                            circumferenceACTV.isVisible = false
                        } else {
                            woundContainerCL.isVisible = true
                            totalCircumferenceValueACTV.isVisible = true
                            circumferenceACTV.isVisible = true
                        }

                        measurementsItemsAdapter?.setData(indexedMetadataList)
                        totalAreaValueACTV.text =
                            getString(
                                io.imito.woundgenius.sdk.R.string.WOUND_GENIUS_SDK_cm_square,
                                decimalFormat.format(totalArea)
                            )
                        totalCircumferenceValueACTV.text =
                            getString(
                                io.imito.woundgenius.sdk.R.string.WOUND_GENIUS_SDK_cm_square,
                                decimalFormat.format(totalCircumference)
                            )

                        circumferenceACTV.isVisible = WoundGeniusSDK.getIsShowTotalCircumference()
                        totalCircumferenceValueACTV.isVisible =
                            WoundGeniusSDK.getIsShowTotalCircumference()
                        totalAreaValueDividerV.isInvisible =
                            !WoundGeniusSDK.getIsShowTotalCircumference()
                    }
                }
            }
        }
    }

    private fun setUpAssessmentImagePager(draftMediaList: List<MediaModel>, isStoma: Boolean) {
        binding.apply {
            (imagesPagerVP2.getChildAt(0) as RecyclerView).layoutManager?.isItemPrefetchEnabled =
                false
            (imagesPagerVP2.getChildAt(0) as RecyclerView).setItemViewCacheSize(1)
            imagesPagerVP2.adapter =
                AssessmentImagesPagerAdapter(
                    this@MeasurementResultHolderActivity,
                    ArrayList(draftMediaList),
                    isStoma
                ).apply {
                    assessmentImagesPagerAdapter = this
                }
            indicatorSPI.attachToPager(imagesPagerVP2)
            indicatorSPI.invalidate()
            indicatorSPI.reattach()
            if (draftMediaList.size == 1) {
                indicatorSPI.isVisible = false
            }
            if (draftMediaList.isNotEmpty()) {
                imagesPagerVP2.setCurrentItem(0, true)
            }
            imagesPagerVP2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val metadata = draftMediaList[position].metadata
                    prepareMediaMetaDataResultUi(metadata)
                }
            })
        }
    }


    override fun onKeyboardOpen() {}

    override fun onKeyboardClose() {}

    companion object {

        private const val EXTRA_ARGS = "KEY_ARGS"
        private const val STOMA_ITEM_PATTERN = "%d"
        private const val ONE = "1"

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
