package com.example.samplewoundsdk.ui.screen.homescreen

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import com.example.samplewoundsdk.BuildConfig
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.pojo.license.SdkFeatureStatus
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_AREA_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_OUTLINE_TYPE
import com.example.samplewoundsdk.databinding.SampleAppFragmentHomeScreenBinding
import com.example.samplewoundsdk.ui.screen.base.AbsFragment
import com.example.samplewoundsdk.ui.screen.main.MainBridge
import com.example.samplewoundsdk.ui.screen.measurementresult.holder.MeasurementResultHolderActivity
import com.example.samplewoundsdk.utils.data.LineChartData
import com.example.woundsdk.data.pojo.WoundGeniusOperatingMode
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.data.pojo.camera.cameramod.CameraMods
import com.example.woundsdk.di.WoundGeniusSDK
import com.example.woundsdk.dialog.ImitoCenterScreenDialog
import com.example.woundsdk.ui.screen.bodypicker.BodyPartContract
import com.example.woundsdk.ui.screen.bodypicker.BodyPickerActivity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraContract
import com.example.woundsdk.utils.ConverterUtil
import com.example.woundsdk.utils.LandscapeUtils.isSDKSupportPortraitOnly
import com.example.woundsdk.utils.LandscapeUtils.isSupportPortraitOnly
import com.example.woundsdk.utils.LandscapeUtils.onConfigurationChange
import com.example.woundsdk.utils.SdkFeature
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.io.File
import java.util.Locale

class HomeScreenFragment : AbsFragment<HomeScreenViewModel>() {

    private val mainBridge by lazy { activity as MainBridge }

    override fun provideViewModelClass() = HomeScreenViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_fragment_home_screen

    private val objectAnimatorDownAnimator by lazy { ObjectAnimator.ofFloat(0f, 90f) }

    private val objectAnimatorUpAnimator by lazy { ObjectAnimator.ofFloat(90f, 0f) }

    private lateinit var listEntry: ArrayList<Entry>

    lateinit var binding: SampleAppFragmentHomeScreenBinding

    private var wasLicenseIncorrect = false

    private var woundGeniusSDK = WoundGeniusSDK

    private val measureCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        MeasureCameraContract()
    ) { assessment: AssessmentEntity? ->
        if (assessment != null) {
            binding.recyclerLockerV.visibility = View.VISIBLE
            viewModel?.saveAssessmentToDB(assessment)
        }
    }

    private val bodyPartLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        BodyPartContract()
    ) {
        if (it != null) {
            viewModel?.changeSelectedBodyParts(
                it
            )
        }
    }

    private val assessmentsAdapter by lazy {
        AssessmentsAdapter(
            onAssessmentClick = { assessment ->
                viewModel?.apply {
                    context?.let { context ->
                        MeasurementResultHolderActivity.open(
                            context,
                            assessment
                        )
                    }
                }
            },
            onAssessmentDelete = { draftAssessment ->
                viewModel?.apply {
                    draftAssessment.id.let {
                        viewModel?.deleteAssessment(
                            it
                        )
                    }
                }
            }
        )
    }

    override fun initListeners() {
        binding.apply {
            settingsButtonACIV.setOnClickListener {
                mainBridge.openSettingsScreen()
            }
            bodyPickerButtonCL.setOnClickListener {
                if (viewModel?.licenseErrorDialog?.value?.first == true) {
                    viewModel?.openLicenseIssueDialog(viewModel?.licenseErrorDialog?.value?.second)
                } else {
                    if (viewModel?.isNoLicenseError?.value == true) {
                        viewModel?.openNoLicenseKeyDialog()
                    } else {
                        if (viewModel?.availableFeatures?.value?.contains(SdkFeature.BODY_PART_PICKER.featureName) == true) {
                            val selectedBodyParts =
                                viewModel?.bodyPartSelectedLD?.value ?: emptyList()
                            context?.let { it1 ->
                                BodyPickerActivity.openWithModelList(
                                    bodyPartLauncher,
                                    it1,
                                    selectedBodyParts
                                )
                            }
                        } else {
                            ImitoCenterScreenDialog.getNoLicenseKeyDialog(
                                titleText = getString(R.string.WOUND_GENIUS_SDK_SDK_FEATURE_LOCKED_DIALOG_TITLE),
                                descriptionText = getString(R.string.WOUND_GENIUS_SDK_SDK_FEATURE_LOCKED_DIALOG_DESCRIPTION),
                                onOkClick = {
                                }
                            ).let {
                                it.show(this@HomeScreenFragment.parentFragmentManager, it.tag)
                            }
                        }

                    }
                }
            }
            captureModeButtonCL.setOnClickListener {
                val mediaFolder = File(context?.cacheDir?.absolutePath ?: "")
                if (!mediaFolder.exists()) {
                    mediaFolder.mkdir()
                }
                if (viewModel?.licenseErrorDialog?.value?.first == true) {
                    viewModel?.openLicenseIssueDialog(viewModel?.licenseErrorDialog?.value?.second)
                } else {
                    if (viewModel?.isNoLicenseError?.value == true) {
                        viewModel?.openNoLicenseKeyDialog()
                    } else {
                        WoundGeniusSDK.configure(
                            captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE),
                            captureScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_SUBTITLE),

                            pinsScreenTitle = getString(R.string.WOUND_GENIUS_SDK_PINS_SCREEN_TITLE),
                            pinsScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_PINS_SCREEN_SUBTITLE),

                            outlineScreenTitle = getString(R.string.WOUND_GENIUS_SDK_OUTLINE_SCREEN_TITLE),
                            outlineScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_OUTLINE_SCREEN_SUBTITLE),

                            resultScreenTitle = getString(R.string.WOUND_GENIUS_SDK_RESULTS_SCREEN_TITLE),
                            resultScreenSubTitle = getString(R.string.WOUND_GENIUS_SDK_RESULTS_SCREEN_SUBTITLE)
                        )

                        MeasureCameraActivity.openWithResult(
                            launcher = measureCameraLauncher,
                            fragment = this@HomeScreenFragment,
                            mediaFolder = mediaFolder.absolutePath
                        )
                    }
                }
            }
            licenseKeyButtonCL.setOnClickListener {
                mainBridge.openSettingsScreen()
            }
            expandChartCL.setOnClickListener {
                viewModel?.onExpandChartClick(viewModel?.isMeasurementChartExpandLD?.value ?: false)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding = SampleAppFragmentHomeScreenBinding.bind(view)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { activity ->

            val container = binding.homeScreenContainer
            container.addView(object : View(activity) {
                override fun onConfigurationChanged(newConfig: Configuration?) {
                    super.onConfigurationChanged(newConfig)
                    onConfigurationChange(activity)
                }
            })
        }

        binding.apply {
            val sdkVersionTitle =
                "WoundGenius: ${woundGeniusSDK.sdkReleaseVersion} Build: ${BuildConfig.VERSION_NAME}"
            toolbarLabelACTV.text = sdkVersionTitle
            assessmentsRV.adapter = assessmentsAdapter

            setUpUiTheme()

            viewModel?.apply {
                getLicenseKey()


                onSavedLicenseKeyReceived.observe(viewLifecycleOwner) {
                    it ?: return@observe
                    licenseKeyButtonCL.isVisible = woundGeniusSDK.getLicenseKey().isNullOrEmpty()
                    val licenseVerifyResult = woundGeniusSDK.validateLicenseKey()
                    viewModel?.handleLicenseResult(licenseVerifyResult)
                }

                sdkFeaturesStatusLD.observe(viewLifecycleOwner) { sdkFeaturesStatus ->
                    sdkFeaturesStatus?.let {
                        availableFeatures.value?.let { availableFeatures ->
                            onLicenseUpdate(availableFeatures, it)
                        }
                    }
                }

                getAssessmentList()
                bodyPartSelectedLD.observe(viewLifecycleOwner) { bodyPart ->
                    if (bodyPart.isNullOrEmpty()) {
                        bodyPickerButtonLabelACTV.text = "Pick body Part"
                    } else {
                        var selectedSize = 0
                        bodyPart.forEach {
                            selectedSize += it.items?.size ?: 0
                        }
                        if (selectedSize > 1) {
                            bodyPickerButtonLabelACTV.text =
                                "Pick body Part\n" + "Selected $selectedSize Body Parts"
                        } else {
                            val bodyRegion = context?.let {
                                ConverterUtil.convertBodyPartServerToUiNew(
                                    it,
                                    bodyPart[0].items?.get(0)?.itemId ?: ""
                                )
                            }

                            bodyPickerButtonLabelACTV.text =
                                "Pick body Part\n" + "Selected Body Part: $bodyRegion"
                        }
                    }
                }

                assessmentProgress.observe(viewLifecycleOwner) { isShowProgress ->
                    isShowProgress ?: return@observe
                    recyclerLockerV.visibility = if (isShowProgress) View.VISIBLE else View.GONE
                    paginationProgressB.visibility = if (isShowProgress) View.VISIBLE else View.GONE
                }
                isMeasurementChartExpandLD.observe(viewLifecycleOwner) { isMeasurementChartExpand ->
                    isMeasurementChartExpand ?: return@observe
                    if (!isMeasurementChartExpand) {
                        objectAnimatorDownAnimator.addUpdateListener {
                            val value = it.animatedValue as Float
                            if (value != 90f) {
                                expandChartArrowIconACIV.rotation = value
                            }
                        }
                        objectAnimatorDownAnimator.interpolator = LinearInterpolator()
                        objectAnimatorDownAnimator.start().apply {
                            objectAnimatorDownAnimator.removeAllUpdateListeners()
                        }
                    } else {
                        objectAnimatorUpAnimator.addUpdateListener {
                            val value = it.animatedValue as Float
                            if (value != 0f) {
                                expandChartArrowIconACIV.rotation = value
                            }
                        }
                        objectAnimatorUpAnimator.interpolator = LinearInterpolator()
                        objectAnimatorUpAnimator.start().apply {
                            objectAnimatorUpAnimator.removeAllUpdateListeners()
                        }
                    }
                    llChart.isVisible = isMeasurementChartExpand
                }
                noLicenseKeyErrorDialog.observe(viewLifecycleOwner) {
                    it ?: return@observe
                    ImitoCenterScreenDialog.getNoLicenseKeyDialog(
                        titleText = getString(R.string.WOUND_GENIUS_SDK_NO_LICENSE_KEY_DIALOG_TITLE),
                        descriptionText = getString(R.string.WOUND_GENIUS_SDK_NO_LICENSE_KEY_DIALOG_DESCRIPTION),
                        onOkClick = {

                        }
                    ).let {
                        it.show(this@HomeScreenFragment.parentFragmentManager, it.tag)
                    }
                }
                licenseErrorDialog.observe(viewLifecycleOwner) {
                    it.third ?: return@observe
                    ImitoCenterScreenDialog.getLicenseIssueDialog(
                        titleText = getString(R.string.WOUND_GENIUS_SDK_LICENSE_ISSUE_DIALOG_TITLE),
                        descriptionText = it.second?.value ?: "",
                        onOkClick = {

                        }
                    ).let {
                        it.show(this@HomeScreenFragment.parentFragmentManager, it.tag)
                    }
                }
                assessmentsResponseLD.observe(viewLifecycleOwner) { assessments ->
                    if (assessments.isNotEmpty()) {
                        recyclerLockerV.visibility = View.GONE
                        assessmentsRV.visibility = View.VISIBLE
                        val measurementMetaDataAmount =
                            assessments.filter { it.media?.find { !it.metadata?.measurementData?.annotationList.isNullOrEmpty() } != null }.size
                        expandChartCL.isVisible =
                            assessments.find { it.media?.find { !it.metadata?.measurementData?.annotationList.isNullOrEmpty() } != null } != null && measurementMetaDataAmount >= 2
                        assessmentsAdapter.submitList(assessments)
                        setAssessmentChartData(assessments)
                        assessmentsRV.scrollToPosition(0)
                    } else {
                        expandChartCL.visibility = View.GONE
                        assessmentsRV.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun onCameraModsChange(cameraMod: CameraMods, isChecked: Boolean) {
        val availableCameraMods = ArrayList(woundGeniusSDK.getAvailableModes())
        if (isChecked) {
            if (!availableCameraMods.contains(cameraMod)) {
                availableCameraMods.add(cameraMod)
            }
        } else {
            availableCameraMods.removeIf {
                it == cameraMod
            }
        }

        woundGeniusSDK.configure(
            availableModes = availableCameraMods
        )
    }

    private fun onLicenseUpdate(
        availableFeatures: List<String>,
        sdkFeaturesStatus: SdkFeatureStatus
    ) {
        binding.apply {
            viewModel?.apply {

                var isEnabled =
                    sdkFeaturesStatus.availableModes?.contains(CameraMods.VIDEO_MODE) ?: false

                if (availableFeatures.contains(SdkFeature.VIDEO_CAPTURING.featureName)) {
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.VIDEO_MODE, isEnabled)
                } else {
                    onCameraModsChange(CameraMods.VIDEO_MODE, false)

                }

                if (availableFeatures.contains(SdkFeature.STOMA_DOCUMENTATION.featureName)) {
                    val isStomaFlowEnabled =
                        sdkFeaturesStatus.isStomaFlowEnable ?: false
                    woundGeniusSDK.configure(
                        isStomaFlow = isStomaFlowEnabled
                    )
                } else {
                    woundGeniusSDK.configure(
                        isStomaFlow = false
                    )
                }

                if (availableFeatures.contains(SdkFeature.PHOTO_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(CameraMods.PHOTO_MODE) ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.PHOTO_MODE, isEnabled)
                } else {
                    onCameraModsChange(CameraMods.PHOTO_MODE, false)
                }

                if (availableFeatures.contains(SdkFeature.MARKER_MEASUREMENT_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(CameraMods.MARKER_DETECT_MODE)
                            ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.MARKER_DETECT_MODE, isEnabled)
                } else {
                    onCameraModsChange(CameraMods.MARKER_DETECT_MODE, false)
                }

                if (availableFeatures.contains(SdkFeature.RULER_MEASUREMENT_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(CameraMods.MANUAL_MEASURE_MODE)
                            ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.MANUAL_MEASURE_MODE, isEnabled)
                } else {
                    onCameraModsChange(CameraMods.MANUAL_MEASURE_MODE, false)
                }
                !availableFeatures.contains(SdkFeature.RULER_MEASUREMENT_CAPTURING.featureName)

                if (availableFeatures.contains(SdkFeature.MULTIPLE_WOUNDS_PER_IMAGE.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isMultipleOutlinesSupported ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    woundGeniusSDK.configure(
                        isMultipleOutlinesEnabled = isEnabled
                    )

                } else {
                    woundGeniusSDK.configure(
                        isMultipleOutlinesEnabled = false
                    )
                }

                if (availableFeatures.contains(SdkFeature.WOUND_DETECTION.featureName)) {
                    var woundAutoDetectionMode =
                        sdkFeaturesStatus.autoDetectionMode

                    if (wasLicenseIncorrect) {
                        woundAutoDetectionMode = WoundAutoDetectionMode.WOUND
                    }


                    woundGeniusSDK.configure(
                        woundAutoDetectionMode = if (woundGeniusSDK.getIsStomaFlow()) WoundAutoDetectionMode.NONE else woundAutoDetectionMode
                    )
                } else {

                    woundGeniusSDK.configure(
                        woundAutoDetectionMode = WoundAutoDetectionMode.NONE
                    )
                }



                if (availableFeatures.contains(SdkFeature.LIVE_WOUND_DETECTION.featureName)) {
                    isEnabled = sdkFeaturesStatus.isLiveDetectionEnabled ?: false

                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }

                    woundGeniusSDK.configure(
                        isLiveDetectionEnabled = isEnabled
                    )
                } else {

                    woundGeniusSDK.configure(
                        isLiveDetectionEnabled = false
                    )
                }

                if (availableFeatures.contains(SdkFeature.LOCAL_STORAGE_IMAGES.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isMediaFromGalleryAllowed ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    woundGeniusSDK.configure(
                        isAddFromLocalStorageAvailable = isEnabled
                    )

                } else {
                    woundGeniusSDK.configure(
                        isAddFromLocalStorageAvailable = false
                    )

                }


                if (availableFeatures.contains(SdkFeature.BODY_PART_PICKER.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isBodyPickerAllowed ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    woundGeniusSDK.configure(
                        isAddBodyPickerOnCaptureScreenAvailable = isEnabled
                    )

                } else {
                    woundGeniusSDK.configure(
                        isAddBodyPickerOnCaptureScreenAvailable = false
                    )
                }


                if (availableFeatures.contains(SdkFeature.FRONTAL_CAMERA.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isFrontalCameraSupported ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    woundGeniusSDK.configure(
                        isFrontCameraUsageAllowed = isEnabled
                    )

                } else {
                    woundGeniusSDK.configure(
                        isFrontCameraUsageAllowed = false
                    )
                }

                activity?.let {
                    val isOnlyPortrait =
                        isSupportPortraitOnly(it)
                    if (isOnlyPortrait) {
                        woundGeniusSDK.configure(isLandScapeSupported = false)
                        onConfigurationChange(it)
                    } else {
                        if (woundGeniusSDK.getIsLandscapeSupported() && (sdkFeaturesStatus.isLandScapeSupported || it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_FULL_USER)) {
                            woundGeniusSDK.configure(isLandScapeSupported = true)
                            onConfigurationChange(it)
                        } else {
                            woundGeniusSDK.configure(isLandScapeSupported = false)
                            onConfigurationChange(it)
                        }
                    }
                }

                if (availableFeatures.isNotEmpty()) {
                    wasLicenseIncorrect = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.getFeatureStatus()
    }

    private fun setUpUiTheme() {
        binding.apply {

            var backgroundColor: Int? = null
            var primaryButtonColor: Int? = null
            var textColor: Int? = null

            backgroundColor = woundGeniusSDK.getLightBackgroundColor()?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_background
            )
            primaryButtonColor = woundGeniusSDK.getPrimaryButtonColor()?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_button_color
            )
            textColor = woundGeniusSDK.getTextColor()?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_text_color
            )

            backgroundColor?.let {
                toolbarCL.setBackgroundColor(it)
                homeScreenContainer.setBackgroundColor(it)
            }
            primaryButtonColor?.let { color ->
                captureModeButtonCL.backgroundTintList = ColorStateList.valueOf(color)
                bodyPickerButtonCL.backgroundTintList = ColorStateList.valueOf(color)
                settingsButtonACIV.imageTintList = ColorStateList.valueOf(color)
            }
            textColor?.let { textColor ->
                toolbarLabelACTV.setTextColor(textColor)
                sampleSdkVersionLabelACTV.setTextColor(textColor)
                expandChartLabelACTV.setTextColor(textColor)
            }
        }
    }

    private fun setAssessmentChartData(assessmentList: List<SampleAssessmentEntity>) {
//        binding.lineChartV.updateChartData(assessmentList)

        val chartList = ArrayList<LineChartData>()

        if (assessmentList.isNotEmpty()) {
            assessmentList.forEach { assessment ->
                assessment.media?.forEach { media ->
                    if (media.metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_AREA_TYPE } != null) {
                        val areaAnnotationItem =
                            media.metadata.measurementData.annotationList.find { it?.type == ANNOTATION_AREA_TYPE }
                        chartList.add(
                            LineChartData(
                                assessment.timestamp,
                                String.format(
                                    Locale.UK,
                                    getString(R.string.WOUND_GENIUS_SDK_float_format_two_points),
                                    areaAnnotationItem?.area
                                ).toFloat()
                            )
                        )
                    }
                    media.metadata?.measurementData?.annotationList?.filter { it?.type == ANNOTATION_OUTLINE_TYPE }
                        ?.forEach {
                            chartList.add(
                                LineChartData(
                                    assessment.timestamp,
                                    String.format(
                                        Locale.UK,
                                        getString(R.string.WOUND_GENIUS_SDK_float_format_two_points),
                                        it?.area
                                    ).toFloat()
                                )
                            )
                        }
                }
            }
        }
        val isOnlyOneDate = chartList.filter { it.timeStamp != null }.sortedBy { it.timeStamp }
            .all { it.timeStamp == assessmentList.firstOrNull()?.timestamp }
        createLineChart(chartList, isOnlyOneDate)
    }

    private fun createLineChart(
        chartLineData: List<LineChartData>,
        isOnlyOneDate: Boolean
    ) {
        binding.apply {
            if (lineChart.data != null) {
                lineChart.data.clearValues()
                lineChart.data.notifyDataChanged()
                lineChart.notifyDataSetChanged()
                lineChart.invalidate()
                lineChart.zoomToCenter(0f, 0f)
            }

            lineChart.axisRight.isEnabled = false
            lineChart.description.isEnabled = false
            lineChart.setDrawGridBackground(false)
            lineChart.legend.isEnabled = false

            lineChart.xAxis.apply {
                isEnabled = false
                position = XAxis.XAxisPosition.BOTTOM
                isGranularityEnabled = true
                setDrawLabels(true)
                setDrawAxisLine(true)
                setDrawGridLines(false) //hide vertical lines X
                enableGridDashedLine(10f, 10f, 0f)
                textSize = 12f
            }
            lineChart.axisLeft.apply {
                isEnabled = true
                isGranularityEnabled = true
                setDrawTopYLabelEntry(true)
                setDrawLabels(true)
                setDrawAxisLine(true)
                setDrawGridLines(true)
                setDrawGridLinesBehindData(false)
                textSize = 12f
            }

            lineChart.setTouchEnabled(false)
            lineChart.onChartGestureListener
            lineChart.isScaleXEnabled = true
            lineChart.isScaleYEnabled = false
            lineChart.setPinchZoom(false)
            lineChart.isDoubleTapToZoomEnabled = true
            lineChart.isDragDecelerationEnabled = false
            lineChart.setExtraOffsets(5f, 5f, 5f, 5f)

            if (chartLineData.isNotEmpty()) {
                listEntry = ArrayList()

                val sortedChartData = chartLineData.sortedWith(compareBy { it.timeStamp })
                val firstAssessmentDot = sortedChartData.firstOrNull()
                var multiplier = 0

                sortedChartData.forEach {
                    if (it.timeStamp != null) {
                        val entry = if (isOnlyOneDate
                            && it.timeStamp.toFloat() == firstAssessmentDot?.timeStamp?.toFloat()
                        ) {
                            multiplier += 100000
                            Entry(
                                (it.timeStamp - firstAssessmentDot.timeStamp).toFloat() + multiplier,
                                it.area,
                                it.timeStamp + multiplier
                            )
                        } else {
                            Entry(
                                (it.timeStamp - (firstAssessmentDot?.timeStamp ?: 0)).toFloat(),
                                it.area,
                                it.timeStamp
                            )
                        }
                        listEntry.add(entry)
                    }
                }

                val last = listEntry.last()
                val first = listEntry.first()
                lineChart.xAxis.axisMaximum = last.x
                lineChart.xAxis.axisMinimum = first.x

                val lineDataSet = LineDataSet(listEntry, "A")

                lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                // draw selection line as dashed
                lineDataSet.enableDashedHighlightLine(10f, 5f, 0f)
                lineDataSet.highLightColor = Color.BLACK
                lineDataSet.color = Color.RED
                lineDataSet.setDrawValues(true)
                lineDataSet.valueTextSize = 10f
                lineDataSet.setDrawCircles(true)
                lineDataSet.setDrawCircleHole(false)
                lineDataSet.setCircleColor(Color.RED)
                lineDataSet.circleRadius = 3f

                lineDataSet.lineWidth = 1.5f
                val dataSets = ArrayList<ILineDataSet>()
                dataSets.add(lineDataSet)

                val lineData = LineData(dataSets)
                lineChart.data = lineData
                lineChart.invalidate()
            } else {
                val lineData = LineData()
                lineChart.data = lineData
                lineChart.invalidate()
            }
        }
    }


    companion object {

        private const val PREVIEW = "preview"

        fun newInstance() = HomeScreenFragment()
    }

}
