package com.example.samplewoundsdk.ui.screen.homescreen

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import com.example.samplewoundsdk.BuildConfig
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.assessment.SampleAssessmentEntity
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_AREA_TYPE
import com.example.samplewoundsdk.data.pojo.media.MediaModel.Metadata.MeasurementData.Annotation.Companion.ANNOTATION_OUTLINE_TYPE
import com.example.samplewoundsdk.databinding.SampleAppFragmentHomeScreenBinding
import com.example.samplewoundsdk.ui.screen.base.AbsFragment
import com.example.samplewoundsdk.ui.screen.main.MainBridge
import com.example.samplewoundsdk.ui.screen.measurementresult.holder.MeasurementResultHolderActivity
import com.example.samplewoundsdk.utils.data.LineChartData
import com.example.woundsdk.data.pojo.assessment.entity.AssessmentEntity
import com.example.woundsdk.di.WoundGeniusSDK
import com.example.woundsdk.dialog.ImitoCenterScreenDialog
import com.example.woundsdk.ui.screen.bodypicker.BodyPartContract
import com.example.woundsdk.ui.screen.bodypicker.BodyPickerActivity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraActivity
import com.example.woundsdk.ui.screen.measurecamera.MeasureCameraContract
import com.example.woundsdk.utils.ConverterUtil
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
        viewModel?.changeBodyPartSelected(
            when {
                it?.first != null -> it.first!!.hashtagEn
                it?.second != null -> it.second!!.hashtagEn
                else -> ""
            }
        )
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
                            val htGroupId =
                                viewModel?.bodyPartSelectedLD?.value?.let { bodyPartSelected ->
                                    context?.let { it1 ->
                                        ConverterUtil.getHtGroupByHashTag(
                                            it1,
                                            bodyPartSelected
                                        )
                                    }
                                } ?: 0
                            BodyPickerActivity.openWithResult(
                                bodyPartLauncher,
                                this@HomeScreenFragment,
                                htGroupId
                            )
                        } else {
                            ImitoCenterScreenDialog.getNoLicenseKeyDialog(
                                titleText = getString(R.string.SDK_FEATURE_LOCKED_DIALOG_TITLE),
                                descriptionText = getString(R.string.SDK_FEATURE_LOCKED_DIALOG_DESCRIPTION),
                                onOkClick = {
                                }
                            ).let {
                                it.show(this@HomeScreenFragment.parentFragmentManager, it.tag)
                            }
                        }

                    }
                }
            }
            captureModeButtonLabelACTV.setOnClickListener {
                val previewDir = File(context?.cacheDir, PREVIEW)
                if (!previewDir.exists()) {
                    previewDir.mkdir()
                }
                if (viewModel?.licenseErrorDialog?.value?.first == true) {
                    viewModel?.openLicenseIssueDialog(viewModel?.licenseErrorDialog?.value?.second)
                } else {
                    if (viewModel?.isNoLicenseError?.value == true) {
                        viewModel?.openNoLicenseKeyDialog()
                    } else {
                        MeasureCameraActivity.openWithResult(
                            launcher = measureCameraLauncher,
                            fragment = this@HomeScreenFragment,
                            mediaFolder = previewDir.absolutePath
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
        binding.apply {
            val sdkVersionTitle =
                "WoundGenius: ${WoundGeniusSDK.sdkReleaseVersion} Build: ${BuildConfig.VERSION_NAME}"
            toolbarLabelACTV.text = sdkVersionTitle
            assessmentsRV.adapter = assessmentsAdapter
            val primaryButtonColor = context?.getColor(
                WoundGeniusSDK.getPrimaryButtonColor()?.toInt() ?: R.color.sample_app_red
            )
            if (primaryButtonColor != null) {
                bodyPickerButtonCL.setBackgroundColor(primaryButtonColor)
                settingsButtonACIV.setColorFilter(primaryButtonColor)
                captureModeButtonCL.setBackgroundColor(primaryButtonColor)
                licenseKeyButtonCL.setBackgroundColor(primaryButtonColor)
            }

            viewModel?.apply {
                getAssessmentList()
                bodyPartSelectedLD.observe(viewLifecycleOwner) { bodyPart ->
                    if (bodyPart.isNullOrEmpty()) {
                        bodyPickerButtonLabelACTV.text = "Pick body Part"
                    } else {
                        val bodyRegion = context?.let {
                            ConverterUtil.convertBodyPartServerToUiNew(
                                it,
                                bodyPart
                            )
                        }
                        bodyPickerButtonLabelACTV.text = "Pick body Part\n" +
                                "Selected Body Part: $bodyRegion"
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
                        titleText = getString(R.string.NO_LICENSE_KEY_DIALOG_TITLE),
                        descriptionText = getString(R.string.NO_LICENSE_KEY_DIALOG_DESCRIPTION),
                        onOkClick = {

                        }
                    ).let {
                        it.show(this@HomeScreenFragment.parentFragmentManager, it.tag)
                    }
                }
                licenseErrorDialog.observe(viewLifecycleOwner) {
                    it.third ?: return@observe
                    ImitoCenterScreenDialog.getLicenseIssueDialog(
                        titleText = getString(R.string.LICENSE_ISSUE_DIALOG_TITLE),
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
    private fun setAssessmentChartData(assessmentList: List<SampleAssessmentEntity>) {
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
                                    getString(R.string.float_format_two_points),
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
                                        getString(R.string.float_format_two_points),
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
