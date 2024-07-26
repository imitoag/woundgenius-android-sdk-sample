package com.example.samplewoundsdk.ui.screen.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleAppFragmentSettingsScreenBinding
import com.example.samplewoundsdk.ui.screen.base.AbsFragment
import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.data.pojo.cameramod.CameraMods
import com.example.woundsdk.di.WoundGeniusSDK
import com.example.woundsdk.dialog.ImitoCenterScreenDialog
import com.example.woundsdk.utils.SdkFeature


class SettingsScreenFragment : AbsFragment<SettingsScreenViewModel>() {

    override fun provideViewModelClass() = SettingsScreenViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_fragment_settings_screen

    private lateinit var binding: SampleAppFragmentSettingsScreenBinding

    override fun initListeners() {
        binding.apply {
            backButtonACTV.setOnClickListener {
                activity?.onBackPressed()
            }
            addBodyPickerOnCameraLayoutCL.setOnClickListener {
                addBodyPickerOnCameraS.isChecked = !addBodyPickerOnCameraS.isChecked
            }
            addBodyPickerOnCameraS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    isAddBodyPickerOnCaptureScreenAvailable = isChecked,
                    captureScreenTitle = getString(R.string.CAPTURE_SCREEN_TITLE)
                )
            }
            addFromGalleryLayoutCL.setOnClickListener {
                addFromGalleryS.isChecked = !addFromGalleryS.isChecked
            }
            addFromGalleryLayoutCL.setOnClickListener {
                WoundGeniusSDK.configure(
                    isAddFromLocalStorageAvailable = !addFromGalleryS.isChecked,
                    captureScreenTitle = getString(R.string.CAPTURE_SCREEN_TITLE)
                )
            }
            isMultipleOutlinesEnabledLayoutCL.setOnClickListener {

                isMultipleOutlinesEnabledS.isChecked = !isMultipleOutlinesEnabledS.isChecked
            }
            isMultipleOutlinesEnabledS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    isMultipleOutlinesEnabled = isMultipleOutlinesEnabledS.isChecked,
                )
            }

            addVideoModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.VIDEO_MODE, isChecked)
            }
            addVideoModeLayoutCL.setOnClickListener {
                addVideoModeS.isChecked = !addVideoModeS.isChecked
            }

            addMarkerModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.MARKER_DETECT_MODE, isChecked)
            }
            addMarkerModeLayoutCL.setOnClickListener {
                addMarkerModeS.isChecked = !addMarkerModeS.isChecked
            }

            addPhotoModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.PHOTO_MODE, isChecked)
            }
            addPhotoModeLayoutCL.setOnClickListener {
                addPhotoModeS.isChecked = !addPhotoModeS.isChecked
            }

            liveDetectionLayoutCL.setOnClickListener {
                liveDetectionS.isChecked = !liveDetectionS.isChecked
            }

            addRulerModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.MANUAL_MEASURE_MODE, isChecked)
            }
            liveDetectionS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(isLiveDetectionEnabled = isChecked)
            }
            addRulerModeLayoutCL.setOnClickListener {
                addRulerModeS.isChecked = !addRulerModeS.isChecked
            }

            stomaFlowLayoutBlockerCL.setOnClickListener {

            }
            liveDetectionLayoutBlockerCL.setOnClickListener {

            }
            machineLearningLayoutBlockerCL.setOnClickListener {

            }
            addVideoModeLayoutBlockerCL.setOnClickListener {

            }
            addMarkerModeLayoutBlockerCL.setOnClickListener {

            }
            addPhotoModeLayoutBlockerCL.setOnClickListener {

            }
            addRulerModeLayoutBlockerCL.setOnClickListener {

            }
            addFromGalleryLayoutBlockerCL.setOnClickListener {

            }
            addBodyPickerOnCameraLayoutBlockerCL.setOnClickListener {

            }
            isMultipleOutlinesEnabledLayoutBlockerCL.setOnClickListener {

            }
            addCameraSwitchLayoutBlockerCL.setOnClickListener {

            }

            stomaFlowS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    woundAutoDetectionMode = if (isChecked) WoundAutoDetectionMode.NONE else WoundGeniusSDK.getAutoDetectionMod(),
                    isLiveDetectionEnabled = if (isChecked) false else WoundGeniusSDK.getIsLiveDetectionEnabled(),
                    isStomaFlow = isChecked,
                    captureScreenTitle = getString(R.string.CAPTURE_SCREEN_TITLE)
                )
                if (isChecked) {
                    liveDetectionS.isChecked = false
                    setupAutoDetectionModsUi()
                }
            }
            stomaFlowLayoutCL.setOnClickListener {
                stomaFlowS.isChecked = !stomaFlowS.isChecked
            }

            addCameraSwitchS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    isFrontCameraUsageAllowed = isChecked,
                    captureScreenTitle = getString(R.string.CAPTURE_SCREEN_TITLE)
                )
            }
            addCameraSwitchLayoutCL.setOnClickListener {
                addCameraSwitchS.isChecked = !addCameraSwitchS.isChecked
            }

            maxMediaNumberValueACET.addTextChangedListener(
                onTextChanged = { _, _, _, _ ->
                    if (maxMediaNumberValueACET.text.toString().isNotEmpty()) {
                        val sizeValue = maxMediaNumberValueACET.text.toString().toInt()
                        if (sizeValue > 100) {
                            maxMediaNumberValueACET.setText(MAX_MEDIA_CAPTURE_SIZE)
                        }
                        if (sizeValue < 1) {
                            maxMediaNumberValueACET.setText(MIN_MEDIA_CAPTURE_SIZE)
                        }
                        if (maxMediaNumberValueACET.text?.isNotEmpty() == true) {
                            maxMediaNumberValueACET.setSelection(maxMediaNumberValueACET.text.toString().length)
                        }
                        WoundGeniusSDK.configure(
                            maxNumberOfMedia = maxMediaNumberValueACET.text.toString().toInt(),
                            captureScreenTitle = getString(R.string.CAPTURE_SCREEN_TITLE)
                        )
                    }
                }
            )
            licenseKeyValueACET.onDone {
                WoundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
                val licenseVerifyResult = WoundGeniusSDK.validateLicenseKey()
                viewModel?.handleLicenseResult(licenseVerifyResult)
                viewModel?.saveLicenseKey(licenseKeyValueACET.text.toString())
            }
            licenseKeyValueACET.doOnTextChanged { text, start, before, count ->
                WoundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
                val licenseVerifyResult = WoundGeniusSDK.validateLicenseKey()
                viewModel?.handleLicenseResult(licenseVerifyResult)
                viewModel?.saveLicenseKey(licenseKeyValueACET.text.toString())
            }
            userIdValueACET.doOnTextChanged { text, start, before, count ->
                WoundGeniusSDK.setCustomerUserId(userIdValueACET.text.toString())
                viewModel?.saveUserId(userIdValueACET.text.toString())
            }
        }
    }

    private fun onCameraModsChange(cameraMod: CameraMods, isChecked: Boolean) {
        val availableCameraMods = ArrayList(WoundGeniusSDK.getAvailableModes())
        if (isChecked) {
            if (!availableCameraMods.contains(cameraMod)) {
                availableCameraMods.add(cameraMod)
            }
        } else {
            availableCameraMods.removeIf {
                it == cameraMod
            }
        }

        WoundGeniusSDK.configure(
            availableModes = availableCameraMods,
            captureScreenTitle = getString(R.string.CAPTURE_SCREEN_TITLE)
        )
    }

    private fun AppCompatEditText.onDone(callback: () -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                callback.invoke()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding = SampleAppFragmentSettingsScreenBinding.bind(view)
        return binding.root
    }

    private fun setupPrimaryColorUi() {
        viewModel?.apply {
            binding.apply {
                primaryButtonColorList.observe(viewLifecycleOwner) { primaryColors ->
                    val primaryButtonColor =
                        WoundGeniusSDK.getPrimaryButtonColor()?.toInt() ?: R.color.sample_app_red
                    val colorNameList = ArrayList<String>()
                    primaryColors.forEach {
                        colorNameList.add(it.first)
                    }
                    val adapter = context?.let { it1 ->
                        ArrayAdapter(
                            it1,
                            android.R.layout.simple_spinner_item, colorNameList
                        )
                    }
                    primaryColorSpinner.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View, position: Int, id: Long
                        ) {
                            WoundGeniusSDK.setPrimaryButtonColor(primaryColors[position].second.toString())
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // write code to perform some action
                        }
                    }
                    primaryColorSpinner.adapter = adapter
                    context?.getColor(primaryButtonColor)?.let {
                        editSelectionButtonACTV.setTextColor(it)
                        editSelectionArrowIconACTV.backgroundTintList = ColorStateList.valueOf(it)
                    }
                    val selectedPrimaryColor = primaryColors
                        .withIndex()
                        .find { it.value.second == primaryButtonColor }?.index ?: 0
                    primaryColorSpinner.setSelection(selectedPrimaryColor, false)
                }
            }
        }
    }

    private fun setupLightBGUi() {
        viewModel?.apply {
            binding.apply {
                lightBGColorListLD.observe(viewLifecycleOwner) { lightBGColors ->
                    val lightBGColor = WoundGeniusSDK.getLightBackgroundColor()?.toInt()
                        ?: R.color.sample_app_grey_light
                    val colorNameList = ArrayList<String>()
                    lightBGColors.forEach {
                        colorNameList.add(it.first)
                    }
                    val adapter = context?.let { it1 ->
                        ArrayAdapter(
                            it1,
                            android.R.layout.simple_spinner_item, colorNameList
                        )
                    }
                    lightBGSpinner.onItemSelectedListener = object :
                        AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View, position: Int, id: Long
                        ) {
                            WoundGeniusSDK.setLightBackgroundColor(lightBGColors[position].second.toString())
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // write code to perform some action
                        }
                    }
                    lightBGSpinner.adapter = adapter
                    val selectedPrimaryColor = lightBGColors
                        .withIndex()
                        .find { it.value.second == lightBGColor }?.index ?: 0

                    lightBGSpinner.setSelection(selectedPrimaryColor, false)
                }
            }
        }
    }

    private fun setupAutoDetectionModsUi() {
        binding.apply {
            val currentAutoDetectMod = WoundGeniusSDK.getAutoDetectionMod()
            val autoDetectionModList = ArrayList<String>()
            WoundAutoDetectionMode.values().forEach {
                autoDetectionModList.add(it.modName)
            }
            val adapter = context?.let { it1 ->
                ArrayAdapter(
                    it1,
                    android.R.layout.simple_spinner_item, autoDetectionModList
                )
            }
            autoDetectionSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    WoundGeniusSDK.setAutoDetectionMod(WoundAutoDetectionMode.values()[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
            autoDetectionSpinner.adapter = adapter
            val selectedWoundAutoDetectionMode = WoundAutoDetectionMode.values().withIndex()
                .find { it.value == currentAutoDetectMod }?.index ?: 0

            autoDetectionSpinner.setSelection(selectedWoundAutoDetectionMode, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel?.apply {
            binding.apply {
                getLicenseKey()
                onSavedLicenseKeyReceived.observe(viewLifecycleOwner) {
                    it ?: return@observe
                    if (WoundGeniusSDK.getLicenseKey()?.isNotEmpty() == true) {
                        licenseKeyValueACET.setText(WoundGeniusSDK.getLicenseKey())
                        val licenseVerifyResult = WoundGeniusSDK.validateLicenseKey()
                        viewModel?.handleLicenseResult(licenseVerifyResult)
                    } else {
                        licenseKeyValueACET.setText("")
                        blockUi()
                        featureStatusUi()
                    }
                }

                userIdLD.observe(viewLifecycleOwner) { userId ->
                    userIdValueACET.setText(userId)
                }
                noLicenseKeyErrorDialog.observe(viewLifecycleOwner) {
                    it ?: return@observe
                    ImitoCenterScreenDialog.getNoLicenseKeyDialog(
                        titleText = getString(R.string.NO_LICENSE_KEY_DIALOG_TITLE),
                        descriptionText = getString(R.string.NO_LICENSE_KEY_DIALOG_DESCRIPTION),
                        onOkClick = {

                        }
                    ).let {
                        it.show(this@SettingsScreenFragment.parentFragmentManager, it.tag)
                    }
                }
                licenseError.observe(viewLifecycleOwner) {
                    it ?: return@observe
                    blockUi()
                    featureStatusUi()
                }
                licenseErrorDialog.observe(viewLifecycleOwner) {
                    it.third ?: return@observe
                    ImitoCenterScreenDialog.getLicenseIssueDialog(
                        titleText = getString(R.string.LICENSE_ISSUE_DIALOG_TITLE),
                        descriptionText = it.second?.value ?: "",
                        onOkClick = {

                        }
                    ).let {
                        it.show(this@SettingsScreenFragment.parentFragmentManager, it.tag)
                    }
                }
                availableFeatures.observe(viewLifecycleOwner) { availableFeatures ->
                    if (!availableFeatures.contains(SdkFeature.PHOTO_CAPTURING.featureName)) {
                        onCameraModsChange(CameraMods.PHOTO_MODE, false)
                        addPhotoModeLayoutBlockerCL.isVisible = true
                    } else {
                        addPhotoModeLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.VIDEO_CAPTURING.featureName)) {
                        onCameraModsChange(CameraMods.VIDEO_MODE, false)
                        addVideoModeLayoutBlockerCL.isVisible = true
                    } else {
                        addVideoModeLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.RULER_MEASUREMENT_CAPTURING.featureName)) {
                        onCameraModsChange(CameraMods.MANUAL_MEASURE_MODE, false)
                        addRulerModeLayoutBlockerCL.isVisible = true
                    } else {
                        addRulerModeLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.MARKER_MEASUREMENT_CAPTURING.featureName)) {
                        onCameraModsChange(CameraMods.MARKER_DETECT_MODE, false)
                        addMarkerModeLayoutBlockerCL.isVisible = true
                    } else {
                        addMarkerModeLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.FRONTAL_CAMERA.featureName)) {
                        WoundGeniusSDK.setIsFrontCameraUsageAllowed(false)
                        addCameraSwitchLayoutBlockerCL.isVisible = true
                    } else {
                        addCameraSwitchLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.WOUND_DETECTION.featureName)) {
                        WoundGeniusSDK.setWoundAutoDetectionMode(WoundAutoDetectionMode.NONE)
                        machineLearningLayoutBlockerCL.isVisible = true
                    } else {
                        machineLearningLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.BODY_PART_PICKER.featureName)) {
                        WoundGeniusSDK.setIsAddBodyPickerAvailable(false)
                        addBodyPickerOnCameraLayoutBlockerCL.isVisible = true
                    } else {
                        addBodyPickerOnCameraLayoutBlockerCL.isVisible = false
                    }
                    if (!availableFeatures.contains(SdkFeature.LOCAL_STORAGE_IMAGES.featureName)) {
                        WoundGeniusSDK.setIsAddFromLocalStorageAvailable(false)
                        addFromGalleryLayoutBlockerCL.isVisible = true
                    } else {
                        addFromGalleryLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.LIVE_WOUND_DETECTION.featureName)) {
                        WoundGeniusSDK.setIsLiveDetectionEnabled(false)
                        liveDetectionLayoutBlockerCL.isVisible = true
                    } else {
                        liveDetectionLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.MULTIPLE_WOUNDS_PER_IMAGE.featureName)) {
                        WoundGeniusSDK.setIsMultipleOutlinesEnabled(false)
                        isMultipleOutlinesEnabledLayoutBlockerCL.isVisible = true
                    } else {
                        isMultipleOutlinesEnabledLayoutBlockerCL.isVisible = false
                    }
                    if (!availableFeatures.contains(SdkFeature.STOMA_DOCUMENTATION.featureName)) {
                        WoundGeniusSDK.setIsStomaFlow(false)
                        stomaFlowLayoutBlockerCL.isVisible = true
                    } else {
                        stomaFlowLayoutBlockerCL.isVisible = false
                    }

                    if (!availableFeatures.contains(SdkFeature.DEBUG_MODE.featureName)) {
                        WoundGeniusSDK.setIsDebugMode(false)
                    } else {
                        WoundGeniusSDK.setIsDebugMode(true)
                    }
                    featureStatusUi()
                }
                getUserId()
                setupPrimaryColorUi()
                setupLightBGUi()
                setupAutoDetectionModsUi()
            }
        }
    }

    private fun blockUi() {
        binding.apply {
            stomaFlowLayoutBlockerCL.isVisible = true
            liveDetectionLayoutBlockerCL.isVisible = true
            machineLearningLayoutBlockerCL.isVisible = true
            addVideoModeLayoutBlockerCL.isVisible = true
            addMarkerModeLayoutBlockerCL.isVisible = true
            addPhotoModeLayoutBlockerCL.isVisible = true
            addRulerModeLayoutBlockerCL.isVisible = true
            addFromGalleryLayoutBlockerCL.isVisible = true
            addBodyPickerOnCameraLayoutBlockerCL.isVisible = true
            isMultipleOutlinesEnabledLayoutBlockerCL.isVisible = true
            addCameraSwitchLayoutBlockerCL.isVisible = true

            WoundGeniusSDK.configure(
                isStomaFlow = false,
                woundAutoDetectionMode = WoundAutoDetectionMode.NONE,
                isLiveDetectionEnabled = false,
                availableModes = emptyList(),
                isMultipleOutlinesEnabled = false,
                isFrontCameraUsageAllowed = false,
                isAddFromLocalStorageAvailable = false,
                isAddBodyPickerOnCaptureScreenAvailable = false
            )
        }
    }

    private fun featureStatusUi() {
        binding.apply {
            if (WoundGeniusSDK.getCustomerUserId().isNotEmpty()) {
                userIdValueACET.setText(WoundGeniusSDK.getCustomerUserId())
            }
            val isAddMediaFromGallery = WoundGeniusSDK.getIsAddFromLocalStorageAvailable()
            if (addFromGalleryS.isChecked != isAddMediaFromGallery) {
                addFromGalleryS.isChecked = isAddMediaFromGallery
            }
            val isMultiOutlineEnable = WoundGeniusSDK.getIsMultipleOutlinesEnabled()
            if (isMultipleOutlinesEnabledS.isChecked != isMultiOutlineEnable) {
                isMultipleOutlinesEnabledS.isChecked = isMultiOutlineEnable
            }
            val isAddBodyPickerOnCamera = WoundGeniusSDK.getIsAddBodyPickerAvailable()
            if (addBodyPickerOnCameraS.isChecked != isAddBodyPickerOnCamera) {
                addBodyPickerOnCameraS.isChecked = isAddBodyPickerOnCamera
            }
            val iStomaFlow = WoundGeniusSDK.getIsStomaFlow()
            if (stomaFlowS.isChecked != iStomaFlow) {
                stomaFlowS.isChecked = iStomaFlow
            }
            val isLiveDetectionEnabled = WoundGeniusSDK.getIsLiveDetectionEnabled()
            if (liveDetectionS.isChecked != isLiveDetectionEnabled) {
                liveDetectionS.isChecked = isLiveDetectionEnabled
            }
            val addCameraSwitch = WoundGeniusSDK.getIsFrontCameraUsageAllowed()
            if (addCameraSwitchS.isChecked != addCameraSwitch) {
                addCameraSwitchS.isChecked = addCameraSwitch
            }
            val addVideoMode = WoundGeniusSDK.getAvailableModes().contains(CameraMods.VIDEO_MODE)
            if (addVideoModeS.isChecked != addVideoMode) {
                addVideoModeS.isChecked = addVideoMode
            }
            val addRulerMode =
                WoundGeniusSDK.getAvailableModes().contains(CameraMods.MANUAL_MEASURE_MODE)
            if (addRulerModeS.isChecked != addRulerMode) {
                addRulerModeS.isChecked = addRulerMode
            }
            val addPhotoMode =
                WoundGeniusSDK.getAvailableModes().contains(CameraMods.PHOTO_MODE)
            if (addPhotoModeS.isChecked != addPhotoMode) {
                addPhotoModeS.isChecked = addPhotoMode
            }
            val addMarkerMod =
                WoundGeniusSDK.getAvailableModes().contains(CameraMods.MARKER_DETECT_MODE)
            if (addMarkerModeS.isChecked != addMarkerMod) {
                addMarkerModeS.isChecked = addMarkerMod
            }
            val maxMediaNumber = WoundGeniusSDK.getMaxNumberOfMedia()
            maxMediaNumberValueACET.setText(maxMediaNumber.toString())
        }
    }

    companion object {
        const val MAX_MEDIA_CAPTURE_SIZE = "100"
        const val MIN_MEDIA_CAPTURE_SIZE = "1"
        fun newInstance() = SettingsScreenFragment()
    }

}
