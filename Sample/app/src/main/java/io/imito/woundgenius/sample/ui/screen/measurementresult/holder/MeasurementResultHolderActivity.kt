package io.imito.woundgenius.sample.ui.screen.measurementresult.holder

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.davemorrissey.labs.subscaleview.ImageSource
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sdk.internal.data.pojo.media.MediaModel
import io.imito.woundgenius.sample.databinding.SampleAppActivityMeasurementResultHolderBinding
import io.imito.woundgenius.sample.ui.screen.base.AbsActivity
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.MeasurementMetadata
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.OutlineModel
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_AREA_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_LENGTH_PREFIX
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_LINE_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_MEASUREMENT_LINE_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_OUTLINE_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_WIDTH_PREFIX
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.internal.data.pojo.image.ImageResolution
import io.imito.woundgenius.sdk.internal.ui.screen.measurementresult.common.MeasurementsItemsAdapter
import io.imito.woundgenius.sdk.internal.utils.system.DarkModeUtils.isDarkModeEnabled
import io.imito.woundgenius.sdk.internal.utils.system.LandscapeUtils.isSDKSupportPortraitOnly
import io.imito.woundgenius.sdk.internal.utils.system.LandscapeUtils.onConfigurationChange
import io.imito.woundgenius.sdk.internal.utils.measurements.MeasurementMetadataUtils.groupAndIndexByType
import kotlinx.parcelize.Parcelize
import java.text.DecimalFormat

class MeasurementResultHolderActivity : AbsActivity<MeasurementResultHolderViewModel>() {

    private val args by lazy { intent.getParcelableExtra(EXTRA_ARGS) as? Args }

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

        if (!WoundGeniusSDK.getConfiguration().isLandscapeSupported) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            if (isSDKSupportPortraitOnly(
                    WoundGeniusSDK.getConfiguration().isLandscapeSupported,
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
        val hasMedia = draftMediaList.isNotEmpty() &&
                !draftMediaList.firstOrNull()?.image.isNullOrEmpty()
        val hasMeasurement = draftMediaList.any {
            it.metadata?.measurementData?.annotationList?.isNotEmpty() == true
        }
        val isMagicAssessment = args?.assessmentEntity?.magicAssessment == true

        viewModel?.apply {
            Log.d("Unit","hasMedia = ${hasMedia} isMagicAssessment = ${isMagicAssessment} hasMeasurement = ${hasMeasurement}")
            setUpAssessmentImagePager(
                draftMediaList,
                args?.assessmentEntity?.stomaDocumentation ?: false
            )

            when {
                !hasMedia -> showEmptyMediaPlaceholder()
                isMagicAssessment && !hasMeasurement -> showImageOnly(draftMediaList.firstOrNull()?.image ?:"")
                else -> {
                    val metadata = draftMediaList[0].metadata
                    prepareMediaMetaDataResultUi(metadata)
                }
            }
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

            backgroundColor = WoundGeniusSDK.getConfiguration().lightBackgroundColor?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_background
            )

            dividerColor = WoundGeniusSDK.getConfiguration().valueDividersColor?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_light_grey
            )
            formsColor = WoundGeniusSDK.getConfiguration().measurementFormsColor?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_forms_color
            )

            measurementValueColor = WoundGeniusSDK.getConfiguration().measurementResultColor?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_measurement_value_text_color
            )

            primaryButtonColor = WoundGeniusSDK.getConfiguration().primaryButtonColor?.let {
                getColor(
                    it.toInt()
                )
            } ?: getColor(
                R.color.sample_app_button_color
            )
            textColor = WoundGeniusSDK.getConfiguration().textColor?.let {
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
                maxDepthACTV.setTextColor(textColor)
            }
            dividerColor?.let { dividerColor ->
                totalAreaValueDividerV.setBackgroundColor(dividerColor)
            }
            formsColor?.let { formsColor ->
                allAreaContainerCL.backgroundTintList = ColorStateList.valueOf(formsColor)
            }
            measurementValueColor?.let { measurementValueColor ->
                totalAreaValueACTV.setTextColor(measurementValueColor)
                maxDepthValueACTV.setTextColor(measurementValueColor)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val window = this.window
        WindowInsetsControllerCompat(window, binding.root).isAppearanceLightStatusBars =
            !isDarkModeEnabled(this@MeasurementResultHolderActivity)
    }

    private fun prepareMediaMetaDataResultUi(metadata: MediaModel.Metadata?) {
        args?.apply {
            val metadataList = ArrayList<MeasurementMetadata>()
            metadata?.measurementData?.annotationList?.sortedBy { it?.id }
                ?.forEach { annotationItem ->
                    when (annotationItem?.type) {
                        ANNOTATION_AREA_TYPE -> {
                            val pointsList = annotationItem?.points
                            val lines =
                                metadata.measurementData?.annotationList?.filter { it?.type == ANNOTATION_LINE_TYPE }
                            val widthLine = lines?.find { it?.prefix == ANNOTATION_WIDTH_PREFIX }
                            val lengthLine = lines?.find { it?.prefix == ANNOTATION_LENGTH_PREFIX }

                            val widthA =
                                pointsList?.indexOfFirst { it.x == widthLine?.pointA?.pointX?.toDouble() && it.y == widthLine?.pointA?.pointY?.toDouble() }
                            val widthB =
                                pointsList?.indexOfFirst { it.x == widthLine?.pointB?.pointX?.toDouble() && it.y == widthLine?.pointB?.pointY?.toDouble() }
                            val lengthA =
                                pointsList?.indexOfFirst { it.x == lengthLine?.pointA?.pointX?.toDouble() && it.y == lengthLine?.pointA?.pointY?.toDouble() }
                            val lengthB =
                                pointsList?.indexOfFirst { it.x == lengthLine?.pointB?.pointX?.toDouble() && it.y == lengthLine?.pointB?.pointY?.toDouble() }

                            metadataList.add(
                                MeasurementMetadata(
                                    area = annotationItem?.area ?: 0.0,
                                    circumference = annotationItem?.circumference ?: 0.0,
                                    length = lengthLine?.length ?: 0.0,
                                    width = widthLine?.width ?: 0.0,
                                    depth = if (args?.assessmentEntity?.stomaDocumentation == true) {
                                        (annotationItem?.depth ?: 0.0f)
                                    } else {
                                        (annotationItem?.depth ?: 0.0f) / 10
                                    },
                                    vertices = pointsList?.map {
                                        PointD(it.x ?: 0.0, it.y ?: 0.0)
                                    } ?: emptyList(),
                                    lengthLine = MeasurementMetadata.Line(
                                        lengthA ?: -1,
                                        lengthB ?: -1
                                    ),
                                    widthLine = MeasurementMetadata.Line(
                                        widthA ?: -1,
                                        widthB ?: -1
                                    ),
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
                                        PointD(it.x ?: 0.0, it.y ?: 0.0)
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
                            val widthLine =
                                Pair(annotationItem?.widthPointA, annotationItem?.widthPointB)
                            val lengthLine =
                                Pair(annotationItem?.lengthPointA, annotationItem?.lengthPointB)

                            val widthA =
                                pointsList?.indexOfFirst { it.x == widthLine.first?.pointX?.toDouble() && it.y == widthLine.first?.pointY?.toDouble() }
                            val widthB =
                                pointsList?.indexOfFirst { it.x == widthLine.second?.pointX?.toDouble() && it.y == widthLine.second?.pointY?.toDouble() }
                            val lengthA =
                                pointsList?.indexOfFirst { it.x == lengthLine.first?.pointX?.toDouble() && it.y == lengthLine.first?.pointY?.toDouble() }
                            val lengthB =
                                pointsList?.indexOfFirst { it.x == lengthLine.second?.pointX?.toDouble() && it.y == lengthLine.second?.pointY?.toDouble() }

                            metadataList.add(
                                MeasurementMetadata(
                                    area = annotationItem?.area ?: 0.0,
                                    circumference = annotationItem?.circumference ?: 0.0,
                                    length = annotationItem?.length ?: 0.0,
                                    width = annotationItem?.width ?: 0.0,
                                    depth = (annotationItem?.depth ?: 0.0f) / 10,
                                    vertices = pointsList?.map {
                                        PointD(it.x ?: 0.0, it.y ?: 0.0)
                                    } ?: emptyList(),
                                    lengthLine = MeasurementMetadata.Line(
                                        lengthA ?: -1,
                                        lengthB ?: -1
                                    ),
                                    widthLine = MeasurementMetadata.Line(
                                        widthA ?: -1,
                                        widthB ?: -1
                                    ),
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


            val allVertexesList = ArrayList<List<PointD>>()
            val widthIndexes = ArrayList<Pair<Int?, Int?>>()
            val lengthIndexes = ArrayList<Pair<Int?, Int?>>()
            val areaList = ArrayList<Double>()
            var totalArea = 0.0
            var maxDepth: Float? = null
            metadataList.forEachIndexed { index, boundaryMetadata ->
                boundaryMetadata.apply {
                    boundaryMetadata.vertices?.let {
                        allVertexesList.add(it.map {
                            PointD(
                                (it.x ?: 0.0),
                                (it.y ?: 0.0)
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
                    if ((depth ?: 0.0f) > (maxDepth ?: 0.0f)) {
                        maxDepth = depth
                    }
                }
            }

            args?.apply {
                binding.apply {
                    args?.apply {

                        measurementsItemsRV.isVisible = true

                        if (areaList.size <= 1) {
                            woundContainerCL.isVisible = false
                            maxDepthValueACTV.isVisible = false
                            maxDepthACTV.isVisible = false
                        } else {
                            woundContainerCL.isVisible = true
                            maxDepthValueACTV.isVisible = true
                            maxDepthACTV.isVisible = true
                        }

                        measurementsItemsAdapter?.setData(indexedMetadataList)
                        totalAreaValueACTV.text =
                            getString(
                                io.imito.woundgenius.sdk.R.string.WOUND_GENIUS_SDK_cm_square,
                                decimalFormat.format(totalArea)
                            )
                        maxDepthValueACTV.text =
                            if (maxDepth != null) {
                                getString(
                                    io.imito.woundgenius.sdk.R.string.WOUND_GENIUS_SDK_mm,
                                    decimalFormat.format((maxDepth ?: 0.0f) * 10)
                                )
                            } else {
                                getString(R.string.WOUND_GENIUS_SDK_not_a_number)
                            }


                        maxDepthACTV.isVisible =
                            WoundGeniusSDK.getConfiguration().showTotalCircumference
                        maxDepthValueACTV.isVisible =
                            WoundGeniusSDK.getConfiguration().showTotalCircumference
                        totalAreaValueDividerV.isInvisible =
                            !WoundGeniusSDK.getConfiguration().showTotalCircumference
                    }
                }
            }
            Log.e("Unit",metadataList.toString())
        }
    }

    private fun setUpAssessmentImagePager(draftMediaList: List<MediaModel>, isStoma: Boolean) {
        binding.apply {
            val mediaWithImage = draftMediaList.filter { !it.image.isNullOrEmpty() }

            if (mediaWithImage.isEmpty()) {
                imagesPagerVP2.isVisible = false
                indicatorSPI.isVisible = false
                return
            }

            emptyMediaPlaceholderACIV.isVisible = false
            imagesPagerVP2.isVisible = true

            (imagesPagerVP2.getChildAt(0) as RecyclerView).layoutManager?.isItemPrefetchEnabled =
                false
            (imagesPagerVP2.getChildAt(0) as RecyclerView).setItemViewCacheSize(1)
            imagesPagerVP2.adapter =
                AssessmentImagesPagerAdapter(
                    this@MeasurementResultHolderActivity,
                    ArrayList(mediaWithImage),
                    isStoma
                ).apply {
                    assessmentImagesPagerAdapter = this
                }
            indicatorSPI.attachToPager(imagesPagerVP2)
            indicatorSPI.invalidate()
            indicatorSPI.reattach()
            if (mediaWithImage.size == 1) {
                indicatorSPI.isVisible = false
            }
            imagesPagerVP2.setCurrentItem(0, true)
            imagesPagerVP2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val metadata = mediaWithImage[position].metadata
                    prepareMediaMetaDataResultUi(metadata)
                }
            })
        }
    }

    private fun showEmptyMediaPlaceholder() {
        binding.apply {
            emptyMediaPlaceholderACIV.isVisible = true
            imagesPagerVP2.isVisible = false
            indicatorSPI.isVisible = false
            measurementsCL.isVisible = false
            woundContainerCL.isVisible = false
            measurementsItemsRV.isVisible = false
            regularImageACIV.isVisible = false
        }
    }

    private fun showImageOnly(imagePath: String) {
        binding.apply {
            Log.d("Unit","showImageOnly")
            emptyMediaPlaceholderACIV.isVisible = false
            measurementsCL.isVisible = false
            woundContainerCL.isVisible = false
            measurementsItemsRV.isVisible = false
            imagesPagerVP2.isVisible = false
            regularImageACIV.isVisible = true
            Log.d("Unit","imagePath = ${imagePath}")
                Glide.with(this@MeasurementResultHolderActivity)
                    .load(imagePath)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(regularImageACIV)
        }
    }


    override fun onKeyboardOpen() {}

    override fun onKeyboardClose() {}

    companion object {

        private const val EXTRA_ARGS = "KEY_ARGS"
        private const val STOMA_ITEM_PATTERN = "%d"
        private const val ONE = "1"

        @Parcelize
        private data class Args(
            val assessmentEntity: SampleAssessmentEntity
        ) : Parcelable

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
