package io.imito.woundgenius.sample.ui.screen.homescreen

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.gson.Gson
import io.imito.wizard.api.model.WizardInputConfig
import io.imito.woundgenius.sample.BuildConfig
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.data.pojo.license.SdkFeatureStatus
import io.imito.woundgenius.sample.databinding.SampleAppFragmentHomeScreenBinding
import io.imito.woundgenius.sample.ui.screen.base.AbsFragment
import io.imito.woundgenius.sample.ui.screen.main.MainBridge
import io.imito.woundgenius.sample.ui.screen.measurementresult.holder.MeasurementResultHolderActivity
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.internal.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.internal.data.pojo.bodypart.BodyPreviewDisplayMode
import io.imito.woundgenius.sdk.internal.data.pojo.bodypart.WGBodyPartPickerFrontBackConfig
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.internal.data.pojo.license.SdkFeature
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.MeasurementResult
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_AREA_TYPE
import io.imito.woundgenius.sdk.internal.data.pojo.outline.point.PointD.Companion.ANNOTATION_OUTLINE_TYPE
import io.imito.woundgenius.sdk.internal.managers.wizard.AssessmentWizardLauncher
import io.imito.woundgenius.sdk.internal.managers.wizard.AssessmentWizardResult
import io.imito.woundgenius.sdk.internal.ui.dialog.center.ImitoCenterScreenDialog
import io.imito.woundgenius.sdk.internal.ui.dialog.splashscreen.SplashScreenDialog
import io.imito.woundgenius.sdk.internal.ui.screen.bodypicker.BodyPartContract
import io.imito.woundgenius.sdk.internal.ui.screen.bodypicker.BodyPickerActivity
import io.imito.woundgenius.sdk.internal.ui.screen.measurecamera.MeasureCameraActivity
import io.imito.woundgenius.sdk.internal.ui.screen.measurecamera.MeasureCameraContract
import io.imito.woundgenius.sdk.internal.ui.view.bodypart.WGBodyPartPickerFrontBackView
import io.imito.woundgenius.sdk.internal.utils.bodypicker.BodyPartConverterUtils
import io.imito.woundgenius.sdk.internal.utils.chart.LineChartData
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.FORMS_FOLDER
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.MIME_TYPE_JSON
import io.imito.woundgenius.sdk.internal.utils.keys.Constants.UTC_DATE_FORMAT_PATTERN
import io.imito.woundgenius.sdk.internal.utils.system.LandscapeUtils.isSupportPortraitOnly
import io.imito.woundgenius.sdk.internal.utils.system.LandscapeUtils.onConfigurationChange
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

class HomeScreenFragment : AbsFragment<HomeScreenViewModel>() {

    private val mainBridge by lazy { activity as MainBridge }

    override fun provideViewModelClass() = HomeScreenViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_fragment_home_screen

    private val bodyPartHandler = Handler()

    private val objectAnimatorDownAnimator by lazy { ObjectAnimator.ofFloat(0f, 90f) }

    private val objectAnimatorUpAnimator by lazy { ObjectAnimator.ofFloat(90f, 0f) }

    lateinit var binding: SampleAppFragmentHomeScreenBinding

    private var wasLicenseIncorrect = false

    private var woundGeniusSDK = WoundGeniusSDK

    @Inject
    lateinit var gson: Gson

    private val measureCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        MeasureCameraContract()
    ) { measurements: List<MeasurementResult>? ->
        if (!measurements.isNullOrEmpty()) {
            binding.recyclerLockerV.visibility = View.VISIBLE
            viewModel?.saveAssessmentToDB(measurements)
        }
    }

    private val magicAssessmentLauncher: ActivityResultLauncher<WizardInputConfig> =
        registerForActivityResult(
            AssessmentWizardLauncher.createContract()
        ) { wizardAssessmentResult: AssessmentWizardResult ->
            when (wizardAssessmentResult) {
                is AssessmentWizardResult.Success -> {
                    binding.recyclerLockerV.visibility = View.VISIBLE
                    val durableResult = wizardAssessmentResult.copy(
                        measurementResultWrapper = wizardAssessmentResult.measurementResultWrapper?.let { result ->
                            result.copy(image = persistMagicAssessmentImage(result.image) ?: result.image)
                        }
                    )
                    viewModel?.saveMagicAssessmentToDB(requireContext(), durableResult)
                }

                is AssessmentWizardResult.Failure -> {
                    // No user-facing handling needed: failure is already logged/handled upstream; nothing to restore here.
                }

                is AssessmentWizardResult.Canceled -> {
                    // No-op: user cancelled the wizard, so there is nothing to save or restore
                }

                else -> {}
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
            },
            onAssessmentShare = { assessment ->
                shareAssessmentAsJson(assessment)
            }
        )
    }

    private fun shareAssessmentAsJson(assessment: SampleAssessmentEntity) {
        val ctx = context ?: return

        val jsonObject = gson.toJsonTree(assessment).asJsonObject.apply {
            addProperty("sdkVersion", WoundGeniusSDK.sdkVersion)
        }
        val json = gson.toJson(jsonObject)

        val timestamp = SimpleDateFormat(UTC_DATE_FORMAT_PATTERN, Locale.UK).format(Date())
        val fileName =
            "${if (assessment.magicAssessment == true) "FormsModel_" else "Measurement_"}$timestamp.json"

        val sharesDir = File(ctx.cacheDir, FORMS_FOLDER).apply { mkdirs() }
        val jsonFile = File(sharesDir, fileName)
        jsonFile.writeText(json)

        val uri = FileProvider.getUriForFile(
            ctx,
            ctx.getString(R.string.file_provider),
            jsonFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_JSON
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Magic Assessment Result"))
    }

    override fun initListeners() { // NOSONAR Cognitive Complexity — UI/view code, refactor requires on-device verification
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
                val mediaFolder = mediaDir()
                if (viewModel?.licenseErrorDialog?.value?.first == true) {
                    viewModel?.openLicenseIssueDialog(viewModel?.licenseErrorDialog?.value?.second)
                } else {
                    if (viewModel?.isNoLicenseError?.value == true) {
                        viewModel?.openNoLicenseKeyDialog()
                    } else {
                        if (woundGeniusSDK.getConfiguration()?.availableModes?.isNotEmpty() == true) {
                            context?.let {
                                if (childFragmentManager.findFragmentByTag(SplashScreenDialog.TAG) == null) {
                                    SplashScreenDialog.getInstance(onProceed = {
                                        MeasureCameraActivity.openWithResult(
                                            launcher = measureCameraLauncher,
                                            fragment = this@HomeScreenFragment,
                                            mediaFolder = mediaFolder.absolutePath
                                        )
                                    }).show(childFragmentManager, SplashScreenDialog.TAG)
                                }
                            }
                        } else {
                            ImitoCenterScreenDialog.getNoLicenseKeyDialog(
                                titleText = getString(R.string.WOUND_GENIUS_SDK_SDK_NO_ENABLED_CAMERA_MODS_DIALOG_TITLE),
                                descriptionText = getString(R.string.WOUND_GENIUS_SDK_SDK_NO_ENABLED_CAMERA_MODS_DESCRIPTION),
                                onOkClick = {

                                }
                            ).let {
                                it.show(this@HomeScreenFragment.parentFragmentManager, it.tag)
                            }
                        }
                    }
                }
            }
            startMagicAssessmentButtonCL.setOnClickListener {
                context?.let {

                    val inputConfig = WizardInputConfig(
                        cacheFolder = wizardCacheDir()
                    )

                    magicAssessmentLauncher.launch(inputConfig)
                }
            }
            licenseKeyButtonCL.setOnClickListener {
                mainBridge.openSettingsScreen()
            }
            chartLabelContainerCL.setOnClickListener {
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // NOSONAR Cognitive Complexity — UI/view code, refactor requires on-device verification
        super.onViewCreated(view, savedInstanceState)


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
                                BodyPartConverterUtils.convertBodyPartServerToUiNew(
                                    it,
                                    bodyPart[0].items?.get(0)?.itemId ?: ""
                                )
                            }

                            bodyPickerButtonLabelACTV.text =
                                "Pick Body Part\n" + "Selected Body Part: $bodyRegion"
                        }

                        val selectedBodyPartsColorInt =
                            woundGeniusSDK.getConfiguration().primaryButtonColor?.let {
                                context?.getColor(it.toInt())
                            } ?: context?.getColor(R.color.sample_app_button_color)

                        val selectedBodyPartsColor = selectedBodyPartsColorInt?.let {
                            String.format("#%06X", 0xFFFFFF and it)
                        }

                        val config = WGBodyPartPickerFrontBackConfig(
                            bodyParts = bodyPart,
                            showBodyPartListView = true,
                            showOrientationLabels = true,
                            displayMode = BodyPreviewDisplayMode.BOTH,
                            selectedBodyPartsColor = selectedBodyPartsColor
                        )

                        selectedBodyPartPreview.isVisible = true
                        selectedBodyPartPreview.init(config)

                        bodyPartHandler.postDelayed({
                            binding.selectedBodyPartPreview.isVisible = false
                        }, 5000)
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
                    chartWGCV.isVisible = isMeasurementChartExpand
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
                        lineChartContainerCL.isVisible =
                            assessments.find { it.media?.find { !it.metadata?.measurementData?.annotationList.isNullOrEmpty() } != null } != null && measurementMetaDataAmount >= 2
                        assessmentsAdapter.submitList(assessments)
                        setAssessmentChartData(assessments)
                        assessmentsRV.scrollToPosition(0)
                    } else {
                        lineChartContainerCL.visibility = View.GONE
                        assessmentsRV.visibility = View.GONE
                        binding.chartWGCV.isVisible = false
                    }
                }
            }
        }
    }

    private fun onLicenseUpdate( // NOSONAR Cognitive Complexity — UI/view code, refactor requires on-device verification
        availableFeatures: List<String>,
        sdkFeaturesStatus: SdkFeatureStatus
    ) {

        var config = woundGeniusSDK.getConfiguration()

        // Single Area is gated by its own license feature; without it the mode is always off.
        val isSingleAreaEnabled = if (availableFeatures.contains(SdkFeature.SINGLE_AREA_MODE.featureName)) {
            wasLicenseIncorrect || (sdkFeaturesStatus.isSingleAreaEnabled ?: false)
        } else false

        // Measurement Line is the baseline tool: enabled when the license does not manage it,
        // configurable when it does, and forced on whenever Single Area is active (Single Area builds on it).
        val isMeasurementLineEnabled = when {
            isSingleAreaEnabled -> true
            availableFeatures.contains(SdkFeature.LINE_MEASUREMENT.featureName) ->
                wasLicenseIncorrect || (sdkFeaturesStatus.isMeasurementLineEnabled ?: false)
            else -> true
        }

        config = config.copy(

            availableModes = checkAvailableModes(availableFeatures, sdkFeaturesStatus),

            isStomaFlow = availableFeatures.contains(SdkFeature.STOMA_DOCUMENTATION.featureName) &&
                    (sdkFeaturesStatus.isStomaFlowEnable ?: false),

            isMultipleOutlinesEnabled = if (availableFeatures.contains(SdkFeature.MULTIPLE_WOUNDS_PER_IMAGE.featureName)) {
                wasLicenseIncorrect || (sdkFeaturesStatus.isMultipleOutlinesSupported ?: false)
            } else false,

            // Auto Detection
            autoDetectionMode = if (availableFeatures.contains(SdkFeature.WOUND_DETECTION.featureName)) {
                val mode =
                    if (wasLicenseIncorrect) WoundAutoDetectionMode.WOUND else sdkFeaturesStatus.autoDetectionMode

                if (config.isStomaFlow) WoundAutoDetectionMode.NONE else mode
            } else WoundAutoDetectionMode.NONE,

            // Live Detection
            isLiveWoundDetectionEnabled = availableFeatures.contains(SdkFeature.LIVE_WOUND_DETECTION.featureName) &&
                    (wasLicenseIncorrect || (sdkFeaturesStatus.isLiveDetectionEnabled ?: false)),

            // Measurement Line
            isMeasurementLineEnabled = isMeasurementLineEnabled,

            // Single Area
            isSingleAreaEnabled = isSingleAreaEnabled,

            // Gallery & Body Picker & Front Camera
            isAddFromLocalStorageAvailable = availableFeatures.contains(SdkFeature.LOCAL_STORAGE_IMAGES.featureName) &&
                    (wasLicenseIncorrect || (sdkFeaturesStatus.isMediaFromGalleryAllowed ?: false)),

            isBodyPartPickerAvailable = availableFeatures.contains(SdkFeature.BODY_PART_PICKER.featureName) &&
                    (wasLicenseIncorrect || (sdkFeaturesStatus.isBodyPickerAllowed ?: false)),

            isFrontCameraUsageAllowed = availableFeatures.contains(SdkFeature.FRONTAL_CAMERA.featureName) &&
                    (wasLicenseIncorrect || (sdkFeaturesStatus.isFrontalCameraSupported ?: false)),

            // Limits
            minNumberOfMedia = sdkFeaturesStatus.minNumberOfMedia,
            maxNumberOfMedia = sdkFeaturesStatus.maxNumberOfMedia
        )


        activity?.let { act ->
            val isOnlyPortrait = isSupportPortraitOnly(act)
            val shouldSupportLandscape = !isOnlyPortrait &&
                    (config?.isLandscapeSupported == true && (sdkFeaturesStatus.isLandScapeSupported || act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_FULL_USER))

            config = config.copy(isLandscapeSupported = shouldSupportLandscape)
            onConfigurationChange(act)
        }


        if (availableFeatures.isNotEmpty()) {
            wasLicenseIncorrect = false
        }


        WoundGeniusSDK.updateConfig(config)
    }

    private fun checkAvailableModes(
        features: List<String>,
        status: SdkFeatureStatus
    ): List<ImitoCameraMode> {
        val modes = mutableListOf<ImitoCameraMode>()

        val checkMode = { feature: SdkFeature, mode: ImitoCameraMode ->
            val isAllowedByLicense = features.contains(feature.featureName)
            val isEnabledInStatus = status.availableModes?.contains(mode) == true
            if (isAllowedByLicense && (wasLicenseIncorrect || isEnabledInStatus)) {
                modes.add(mode)
            }
        }

        checkMode(SdkFeature.VIDEO_CAPTURING, ImitoCameraMode.VIDEO_MODE)
        checkMode(SdkFeature.PHOTO_CAPTURING, ImitoCameraMode.PHOTO_MODE)
        checkMode(SdkFeature.MARKER_MEASUREMENT_CAPTURING, ImitoCameraMode.MARKER_DETECT_MODE)
        checkMode(SdkFeature.RULER_MEASUREMENT_CAPTURING, ImitoCameraMode.MANUAL_MEASURE_MODE)

        return modes
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

            backgroundColor = woundGeniusSDK.getConfiguration().lightBackgroundColor?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_background
            )

            primaryButtonColor = woundGeniusSDK.getConfiguration().primaryButtonColor?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_button_color
            )

            textColor = woundGeniusSDK.getConfiguration().textColor?.let {
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
                startMagicAssessmentButtonCL.backgroundTintList = ColorStateList.valueOf(color)
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

        var chartList = ArrayList<LineChartData>()

        if (assessmentList.isNotEmpty()) {
            assessmentList.forEach { assessment ->
                assessment.media?.forEach { media ->
                    if (media.metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_AREA_TYPE } != null) {
                        val areaAnnotationItem =
                            media?.metadata?.measurementData?.annotationList?.find { it?.type == ANNOTATION_AREA_TYPE }
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

        binding.chartWGCV.setData(chartList)
    }


    /**
     * Durable, app-owned directory for captured media. Lives under [Context.getFilesDir] (NOT the
     * cache dir) so it survives OS cache eviction and — crucially — is never touched by the wizard's
     * scratch-folder cleanup, which wipes its whole cache folder when a Magic Assessment finishes.
     */
    private fun mediaDir(): File =
        File(requireContext().filesDir, MEDIA_DIR_NAME).apply { mkdirs() }

    /**
     * Dedicated, disposable scratch folder handed to the wizard. Kept separate from [mediaDir] and
     * from the rest of the app cache so the wizard can safely empty it on assessment finish without
     * destroying saved media from either the camera or earlier Magic Assessments.
     */
    private fun wizardCacheDir(): File =
        File(requireContext().cacheDir, WIZARD_CACHE_DIR_NAME).apply { mkdirs() }

    /**
     * Copies a Magic Assessment result image out of the wizard's transient cache into [mediaDir] and
     * returns the durable path. The SDK persists the result image in a scratch dir that is wiped on
     * the next assessment, so we must own a copy. Returns the original path if the source is missing.
     */
    private fun persistMagicAssessmentImage(sourcePath: String?): String? {
        if (sourcePath.isNullOrEmpty()) return sourcePath
        val source = File(sourcePath)
        if (!source.exists()) return sourcePath
        return try {
            val destDir = File(mediaDir(), System.currentTimeMillis().toString()).apply { mkdirs() }
            val dest = File(destDir, source.name)
            source.copyTo(dest, overwrite = true)
            dest.absolutePath
        } catch (e: Exception) {
            Timber.w(e, "Failed to persist magic assessment image; keeping source path")
            sourcePath
        }
    }

    companion object {

        private const val MEDIA_DIR_NAME = "media"
        private const val WIZARD_CACHE_DIR_NAME = "wizard"

        fun newInstance() = HomeScreenFragment()
    }

}
