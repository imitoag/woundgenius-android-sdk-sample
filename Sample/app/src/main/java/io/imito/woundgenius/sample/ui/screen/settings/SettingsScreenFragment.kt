package io.imito.woundgenius.sample.ui.screen.settings

import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import io.imito.woundgenius.sample.R
import io.imito.woundgenius.sample.data.pojo.license.SdkFeatureStatus
import io.imito.woundgenius.sample.databinding.SampleAppFragmentSettingsScreenBinding
import io.imito.woundgenius.sample.ui.screen.base.AbsFragment
import io.imito.woundgenius.sample.utils.FileLogTree
import io.imito.woundgenius.sdk.internal.data.pojo.autodetectionmod.WoundAutoDetectionMode
import io.imito.woundgenius.sdk.internal.data.pojo.camera.mode.ImitoCameraMode
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.internal.utils.system.LandscapeUtils.isSupportPortraitOnly
import io.imito.woundgenius.sdk.internal.utils.system.LandscapeUtils.onConfigurationChange
import io.imito.woundgenius.sdk.internal.data.pojo.license.SdkFeature


class SettingsScreenFragment : AbsFragment<SettingsScreenViewModel>() {

    override fun provideViewModelClass() = SettingsScreenViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_fragment_settings_screen

    private lateinit var binding: SampleAppFragmentSettingsScreenBinding

    private var wasLicenseIncorrect = false
    
    private var woundGeniusSDK = WoundGeniusSDK


    override fun initListeners() { // NOSONAR Cognitive Complexity — settings UI/view code, refactor requires on-device verification
        binding.apply {
            backButtonACTV.setOnClickListener {
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    // No-op: a non-empty license key requires no extra action before navigating back
                }
                activity?.onBackPressed()
            }
            shareLogsButtonCL.setOnClickListener {
                context?.let { FileLogTree.shareLogs(it) }
            }

            addBodyPickerOnCameraLayoutCL.setOnClickListener {
                addBodyPickerOnCameraS.isChecked = !addBodyPickerOnCameraS.isChecked
            }
            addBodyPickerOnCameraS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isBodyPartPickerAvailable = isChecked
                )
                WoundGeniusSDK.updateConfig(config)

                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }

            addCameraSwitchLayoutCL.setOnClickListener {
                addCameraSwitchS.isChecked = !addCameraSwitchS.isChecked
            }

            addCameraSwitchS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isFrontCameraUsageAllowed = isChecked
                )
                WoundGeniusSDK.updateConfig(config)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }

            addFromGalleryLayoutCL.setOnClickListener {
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isAddFromLocalStorageAvailable = !addFromGalleryS.isChecked
                )
                WoundGeniusSDK.updateConfig(config)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
                addFromGalleryS.isChecked = !addFromGalleryS.isChecked
            }
            isMultipleOutlinesEnabledLayoutCL.setOnClickListener {
                isMultipleOutlinesEnabledS.isChecked = !isMultipleOutlinesEnabledS.isChecked
            }
            addFromGalleryS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isAddFromLocalStorageAvailable = isChecked,
                )
                WoundGeniusSDK.updateConfig(config)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }

            isMultipleOutlinesEnabledS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isMultipleOutlinesEnabled = isMultipleOutlinesEnabledS.isChecked
                )
                WoundGeniusSDK.updateConfig(config)

                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }

            addVideoModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(ImitoCameraMode.VIDEO_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }
            addVideoModeLayoutCL.setOnClickListener {
                addVideoModeS.isChecked = !addVideoModeS.isChecked
            }

            addMarkerModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(ImitoCameraMode.MARKER_DETECT_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }
            addMarkerModeLayoutCL.setOnClickListener {
                addMarkerModeS.isChecked = !addMarkerModeS.isChecked
            }

            addPhotoModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(ImitoCameraMode.PHOTO_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }
            addPhotoModeLayoutCL.setOnClickListener {
                addPhotoModeS.isChecked = !addPhotoModeS.isChecked
            }

            liveDetectionLayoutCL.setOnClickListener {
                liveDetectionS.isChecked = !liveDetectionS.isChecked
            }

            addRulerModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(ImitoCameraMode.MANUAL_MEASURE_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }
            liveDetectionS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isLiveWoundDetectionEnabled = isChecked
                )
                WoundGeniusSDK.updateConfig(config)

                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }
            addRulerModeLayoutCL.setOnClickListener {
                addRulerModeS.isChecked = !addRulerModeS.isChecked
            }

            stomaFlowS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    autoDetectionMode = if (isChecked) WoundAutoDetectionMode.NONE else WoundGeniusSDK.getConfiguration().autoDetectionMode,
                    isLiveWoundDetectionEnabled = if (isChecked) false else WoundGeniusSDK.getConfiguration().isLiveWoundDetectionEnabled,
                    isStomaFlow = isChecked
                )
                WoundGeniusSDK.updateConfig(config)

                if (isChecked) {
                    liveDetectionS.isChecked = false
                    setupAutoDetectionModsUi()
                }
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }
            stomaFlowLayoutCL.setOnClickListener {
                stomaFlowS.isChecked = !stomaFlowS.isChecked
            }

            isLandScapeSupportedS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isLandscapeSupported = isChecked
                )
                WoundGeniusSDK.updateConfig(config)

                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
                activity?.let { activity ->
                    onConfigurationChange(activity)
                }
            }

            isLandscapeSupportedLayoutCL.setOnClickListener {
                isLandScapeSupportedS.isChecked = !isLandScapeSupportedS.isChecked
            }

            isMeasurementLineEnabledLayoutCL.setOnClickListener {
                isMeasurementLineEnabledS.isChecked = !isMeasurementLineEnabledS.isChecked
            }

            isMeasurementLineEnabledS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = config.copy(
                    isMeasurementLineEnabled = isChecked
                )
                WoundGeniusSDK.updateConfig(config)

                // Single Area depends on Measurement Line, so turning the line off also drops Single Area.
                if (!isChecked && isSingleAreaEnabledS.isChecked) {
                    isSingleAreaEnabledS.isChecked = false
                }

                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
            }


            isSingleAreaEnabledLayoutCL.setOnClickListener {
                isSingleAreaEnabledS.isChecked = !isSingleAreaEnabledS.isChecked

                // Single Area builds on Measurement Line, so enabling it also enables the line.
                if (isSingleAreaEnabledS.isChecked) {
                    isMeasurementLineEnabledS.isChecked = false
                }
            }

            isSingleAreaEnabledS.setOnCheckedChangeListener { _, isChecked ->
                var config = WoundGeniusSDK.getConfiguration()
                config = if (isChecked){
                    config.copy(
                        isSingleAreaEnabled = true,
                        isMeasurementLineEnabled = false
                    )
                } else {
                    config.copy(
                        isSingleAreaEnabled = false
                    )
                }

                WoundGeniusSDK.updateConfig(config)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
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
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            maxNumberOfMedia = maxMediaNumberValueACET.text.toString().toInt()
                        )
                        WoundGeniusSDK.updateConfig(config)
                    }
                }
            )

            minMediaNumberValueACET.addTextChangedListener(
                onTextChanged = { _, _, _, _ ->
                    if (minMediaNumberValueACET.text.toString().isNotEmpty()) {
                        val sizeValue = maxMediaNumberValueACET.text.toString().toInt()
                        if (sizeValue > 100) {
                            minMediaNumberValueACET.setText(MAX_MEDIA_CAPTURE_SIZE)
                        }
                        if (sizeValue < 1) {
                            minMediaNumberValueACET.setText("0")
                        }
                        if (minMediaNumberValueACET.text?.isNotEmpty() == true) {
                            minMediaNumberValueACET.setSelection(minMediaNumberValueACET.text.toString().length)
                        }
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            minNumberOfMedia = minMediaNumberValueACET.text.toString().toInt()
                        )
                        WoundGeniusSDK.updateConfig(config)
                    }
                }
            )

            licenseKeyValueACET.onDone {
                woundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus(woundGeniusSDK)
                }
                activity?.onBackPressed()
            }
            licenseKeyValueACET.doOnTextChanged { text, start, before, count ->
                woundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
                viewModel?.saveLicenseKey(licenseKeyValueACET.text.toString())
            }
            userIdValueACET.doOnTextChanged { text, start, before, count ->
                woundGeniusSDK.setCustomerUserId(userIdValueACET.text.toString())
                viewModel?.saveUserId(userIdValueACET.text.toString())
            }
        }
    }

    private fun onCameraModsChange(cameraMod: ImitoCameraMode, isChecked: Boolean) {
        val availableCameraMods = ArrayList(WoundGeniusSDK.getConfiguration().availableModes)
        if (isChecked) {
            if (!availableCameraMods.contains(cameraMod)) {
                availableCameraMods.add(cameraMod)
            }
        } else {
            availableCameraMods.removeIf {
                it == cameraMod
            }
        }

        var config = WoundGeniusSDK.getConfiguration()
        config = config.copy(
            availableModes = availableCameraMods
        )
        WoundGeniusSDK.updateConfig(config)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel?.apply {
            binding.apply {
                context?.let { context ->

                    activity?.let { activity ->

                        val container = binding.settingScreenContainer
                        container.addView(object : View(activity) {
                            override fun onConfigurationChanged(newConfig: Configuration?) {
                                super.onConfigurationChanged(newConfig)
                                onConfigurationChange(activity)
                            }
                        })
                    }

                    getUserId()
                    setUpUiTheme()
                    setupAutoDetectionModsUi()


                    licenseKeyValueACET.setText(woundGeniusSDK.getLicenseKey().toString())
                    primaryColorListLD.observe(viewLifecycleOwner) { colors ->
                        setUpPrimaryColorTheme(colors)
                        setUpBackgroundColorTheme(colors)
                        setUpTextColorTheme(colors)
                    }
                    secondaryColorListLD.observe(viewLifecycleOwner) { colors ->
                        setUpFormsColorTheme(colors)
                        setUpMeasurementValuesColorTheme(colors)
                        setUpValueDividersColorTheme(colors)
                    }

                    userIdLD.observe(viewLifecycleOwner) { userId ->
                        userIdValueACET.setText(userId)
                    }

                    newAvailableFeatures.observe(viewLifecycleOwner) { availableFeatures ->
                        if (availableFeatures.isNullOrEmpty()) {
                            wasLicenseIncorrect = true
                        }
                        sdkFeaturesStatusLD.value?.let {
                            onLicenseUpdate(availableFeatures, it)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.validateSDKCustomerLicense()
    }

    override fun onPause() {
        super.onPause()
        if (binding.licenseKeyValueACET.text.toString().isNotEmpty()) {
            viewModel?.saveFeatureStatus(woundGeniusSDK)
        }
    }

    private fun setUpTextColorTheme(textColors: List<Pair<String, Int?>>) {
        binding.apply {
            context?.let { context ->
                val colorNameList = ArrayList<String>()
                textColors.forEach {
                    colorNameList.add(it.first)
                }
                var textColor: Int? = null
                WoundGeniusSDK.getConfiguration().textColor?.let {
                    textColor = it.toInt()
                }
                val textColorAdapter = object : ArrayAdapter<String>(
                    context,
                    R.layout.sample_app_spinner_item,
                    colorNameList
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView: TextView = view.findViewById(R.id.spinner_item_text)
                        // You can now modify the TextView if needed

                        textView.setTextColor(context.getColor(R.color.sample_app_text_color))
                        return view
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        // Customize the dropdown item appearance here if needed
                        return view
                    }
                }
                textColorSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            textColor = textColors[position].second?.toString()
                        )
                        WoundGeniusSDK.updateConfig(config)
                        setUpUiTheme()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
                textColorSpinner.adapter = textColorAdapter
                val selectedPrimaryColor = textColors
                    .withIndex()
                    .find { it.value.second == textColor }?.index ?: 0

                textColorSpinner.setSelection(selectedPrimaryColor, false)
            }
        }
    }

    private fun setUpFormsColorTheme(textColors: List<Pair<String, Int?>>) {
        binding.apply {
            context?.let { context ->
                val colorNameList = ArrayList<String>()
                textColors.forEach {
                    colorNameList.add(it.first)
                }
                var textColor: Int? = null
                WoundGeniusSDK.getConfiguration().measurementFormsColor?.let {
                    textColor = it.toInt()
                }
                val textColorAdapter = object : ArrayAdapter<String>(
                    context,
                    R.layout.sample_app_spinner_item,
                    colorNameList
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView: TextView = view.findViewById(R.id.spinner_item_text)
                        // You can now modify the TextView if needed

                        textView.setTextColor(context.getColor(R.color.sample_app_text_color))
                        return view
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        // Customize the dropdown item appearance here if needed
                        return view
                    }
                }
                formsColorSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            measurementFormsColor = textColors[position].second?.toString()
                        )
                        WoundGeniusSDK.updateConfig(config)
                        setUpUiTheme()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
                formsColorSpinner.adapter = textColorAdapter
                val selectedPrimaryColor = textColors
                    .withIndex()
                    .find { it.value.second == textColor }?.index ?: 0

                formsColorSpinner.setSelection(selectedPrimaryColor, false)
            }
        }
    }

    private fun setUpMeasurementValuesColorTheme(textColors: List<Pair<String, Int?>>) {
        binding.apply {
            context?.let { context ->
                val colorNameList = ArrayList<String>()
                textColors.forEach {
                    colorNameList.add(it.first)
                }
                var textColor: Int? = null
                WoundGeniusSDK.getConfiguration().measurementResultColor?.let {
                    textColor = it.toInt()
                }
                val textColorAdapter = object : ArrayAdapter<String>(
                    context,
                    R.layout.sample_app_spinner_item,
                    colorNameList
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView: TextView = view.findViewById(R.id.spinner_item_text)
                        // You can now modify the TextView if needed

                        textView.setTextColor(context.getColor(R.color.sample_app_text_color))
                        return view
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        // Customize the dropdown item appearance here if needed
                        return view
                    }
                }
                measurementValuesColorSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            measurementResultColor = textColors[position].second?.toString()
                        )
                        WoundGeniusSDK.updateConfig(config)
                        setUpUiTheme()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
                measurementValuesColorSpinner.adapter = textColorAdapter
                val selectedPrimaryColor = textColors
                    .withIndex()
                    .find { it.value.second == textColor }?.index ?: 0

                measurementValuesColorSpinner.setSelection(selectedPrimaryColor, false)
            }
        }
    }

    private fun setUpValueDividersColorTheme(textColors: List<Pair<String, Int?>>) {
        binding.apply {
            context?.let { context ->
                val colorNameList = ArrayList<String>()
                textColors.forEach {
                    colorNameList.add(it.first)
                }
                var textColor: Int? = null
                WoundGeniusSDK.getConfiguration().valueDividersColor?.let {
                    textColor = it.toInt()
                }
                val textColorAdapter = object : ArrayAdapter<String>(
                    context,
                    R.layout.sample_app_spinner_item,
                    colorNameList
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView: TextView = view.findViewById(R.id.spinner_item_text)
                        // You can now modify the TextView if needed

                        textView.setTextColor(context.getColor(R.color.sample_app_text_color))
                        return view
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        // Customize the dropdown item appearance here if needed
                        return view
                    }
                }
                valueDividersColorSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            valueDividersColor = textColors[position].second?.toString()
                        )
                        WoundGeniusSDK.updateConfig(config)
                        setUpUiTheme()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
                valueDividersColorSpinner.adapter = textColorAdapter
                val selectedPrimaryColor = textColors
                    .withIndex()
                    .find { it.value.second == textColor }?.index ?: 0

                valueDividersColorSpinner.setSelection(selectedPrimaryColor, false)
            }
        }
    }

    private fun setUpPrimaryColorTheme(primaryColors: List<Pair<String, Int?>>) {
        binding.apply {
            context?.let { context ->
                val primaryButtonColor = WoundGeniusSDK.getConfiguration().primaryButtonColor?.toInt()
                val colorNameList = ArrayList<String>()

                primaryColors.forEach {
                    colorNameList.add(it.first)
                }

                val primaryColorAdapter = object : ArrayAdapter<String>(
                    context,
                    R.layout.sample_app_spinner_item,
                    colorNameList
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView: TextView = view.findViewById(R.id.spinner_item_text)
                        // You can now modify the TextView if needed
                        textView.setTextColor(context.getColor(R.color.sample_app_text_color))
                        return view
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        // Customize the dropdown item appearance here if needed
                        return view
                    }
                }

                primaryColorSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            primaryButtonColor = primaryColors[position].second?.toString()
                        )
                        WoundGeniusSDK.updateConfig(config)

                        setUpUiTheme()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
                primaryColorSpinner.adapter = primaryColorAdapter

                primaryButtonColor?.let {
                    context.getColor(it).let {
                        editSelectionButtonACTV.setTextColor(it)
                        shareLogsTextACTV.setTextColor(it)
                        editSelectionArrowIconACTV.backgroundTintList =
                            ColorStateList.valueOf(it)
                    }
                }
                val selectedPrimaryColor = primaryColors
                    .withIndex()
                    .find { it.value.second == primaryButtonColor }?.index ?: 0
                primaryColorSpinner.setSelection(selectedPrimaryColor, false)
            }
        }
    }

    private fun setUpBackgroundColorTheme(lightBGColors: List<Pair<String, Int?>>) {
        binding.apply {
            context?.let { context ->
                val lightBGColor = WoundGeniusSDK.getConfiguration().lightBackgroundColor?.toInt()
                    ?: context.getColor(R.color.sample_app_background)
                val colorNameList = ArrayList<String>()

                lightBGColors.forEach {
                    colorNameList.add(it.first)
                }
                val primaryBGColorAdapter = object : ArrayAdapter<String>(
                    context,
                    R.layout.sample_app_spinner_item,
                    colorNameList
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView: TextView = view.findViewById(R.id.spinner_item_text)
                        // You can now modify the TextView if needed

                        textView.setTextColor(context.getColor(R.color.sample_app_text_color))

                        return view
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        // Customize the dropdown item appearance here if needed
                        return view
                    }
                }
                lightBGSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            lightBackgroundColor = lightBGColors[position].second?.toString()
                        )
                        WoundGeniusSDK.updateConfig(config)
                        setUpUiTheme()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
                lightBGSpinner.adapter = primaryBGColorAdapter
                val selectedPrimaryColor = lightBGColors
                    .withIndex()
                    .find { it.value.second == lightBGColor }?.index ?: 0

                lightBGSpinner.setSelection(selectedPrimaryColor, false)
            }
        }
    }

    private fun setUpUiTheme() {
        binding.apply {
            val config = WoundGeniusSDK.getConfiguration()
            val ctx = context ?: return

            var backgroundColor: Int? = null
            var primaryButtonColor: Int? = null
            var textColor: Int? = null
            var dividerColor: Int? = null

            backgroundColor = config.lightBackgroundColor?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_background
            )

            primaryButtonColor = config.primaryButtonColor?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_button_color
            )

            textColor = config.textColor?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_text_color
            )


            dividerColor = config.valueDividersColor?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_grey
            )



            backgroundColor?.let { backgroundColor ->
                toolbarCL.setBackgroundColor(backgroundColor)
                NSV.setBackgroundColor(backgroundColor)
                settingContainerCL.setBackgroundColor(backgroundColor)
            }
            primaryButtonColor?.let { primaryButtonColor ->
                editSelectionButtonACTV.setTextColor(primaryButtonColor)
                shareLogsTextACTV.setTextColor(primaryButtonColor)
                editSelectionArrowIconACTV.backgroundTintList =
                    ColorStateList.valueOf(primaryButtonColor)
            }
            textColor?.let { textColor ->
                toolbarLabelACTV.setTextColor(textColor)
                licenseKeyACTV.setTextColor(textColor)
                licenseKeyValueACET.setTextColor(textColor)
                userIdACTV.setTextColor(textColor)
                userIdValueACET.setTextColor(textColor)
                addVideoModeLabelACTV.setTextColor(textColor)
                addMarkerModeLabelACTV.setTextColor(textColor)
                addPhotoModeLabelACTV.setTextColor(textColor)
                addRulerModeLabelACTV.setTextColor(textColor)
                maxMediaNumberValueLabelACTV.setTextColor(textColor)
                maxMediaNumberValueACET.setTextColor(textColor)
                minMediaNumberValueACET.setTextColor(textColor)
                stomaFlowLabelACTV.setTextColor(textColor)
                autoDetectionSelectorLabelACTV.setTextColor(textColor)
                liveDetectionLabelACTV.setTextColor(textColor)
                addFromGalleryACTV.setTextColor(textColor)
                addBodyPickerOnCameraACTV.setTextColor(textColor)
                isMultipleOutlinesEnabledACTV.setTextColor(textColor)
                addCameraSwitchACTV.setTextColor(textColor)
                primaryColorSelectorLabelACTV.setTextColor(textColor)
                lightBGSelectorLabelACTV.setTextColor(textColor)
                textColorSelectorLabelACTV.setTextColor(textColor)
                valueDividersColorSelectorLabelACTV.setTextColor(textColor)
                formsColorSelectorLabelACTV.setTextColor(textColor)
                measurementValuesColorSelectorLabelACTV.setTextColor(textColor)
            }
            dividerColor?.let { dividerColor ->
                licenseKeyValueTopV.setBackgroundColor(dividerColor)
                licenseKeyValueBottomV.setBackgroundColor(dividerColor)
                userIdValueTopV.setBackgroundColor(dividerColor)
                modesConfigurationsValueTopV.setBackgroundColor(dividerColor)
                addVideoModeValueBottomV.setBackgroundColor(dividerColor)
                addPhotoModeValueBottomV.setBackgroundColor(dividerColor)
                addRulerModeValueBottomV.setBackgroundColor(dividerColor)
                maxMediaNumberValueTopV.setBackgroundColor(dividerColor)
                maxMediaNumberValueBottomV.setBackgroundColor(dividerColor)
                minMediaNumberValueBottomV.setBackgroundColor(dividerColor)
                flowValueTopV.setBackgroundColor(dividerColor)
                stomaFlowValueBottomV.setBackgroundColor(dividerColor)
                machineLearningValueTopV.setBackgroundColor(dividerColor)
                autoDetectionValueBottomV.setBackgroundColor(dividerColor)
                liveDetectionValueBottomV.setBackgroundColor(dividerColor)
                otherValueTopV.setBackgroundColor(dividerColor)
                addFromGalleryValueBottomV.setBackgroundColor(dividerColor)
                addBodyPickerValueBottomV.setBackgroundColor(dividerColor)
                isMultipleOutlinesValueBottomV.setBackgroundColor(dividerColor)
                addCameraSwitchValueBottomV.setBackgroundColor(dividerColor)
                colorValueTopV.setBackgroundColor(dividerColor)
                primaryColorValueBottomV.setBackgroundColor(dividerColor)
                lightBGValueBottomV.setBackgroundColor(dividerColor)
                textColorValueBottomV.setBackgroundColor(dividerColor)
                formsColorValueBottomV.setBackgroundColor(dividerColor)
                measurementValuesColorValueBottomV.setBackgroundColor(dividerColor)
                valueDividersColorValueBottomV.setBackgroundColor(dividerColor)
            }
        }
    }

    private fun setupAutoDetectionModsUi() {
        binding.apply {
            context?.let { context ->
                val currentAutoDetectMod = WoundGeniusSDK.getConfiguration().autoDetectionMode
                val autoDetectionModList = ArrayList<String>()
                WoundAutoDetectionMode.values().forEach {
                    autoDetectionModList.add(it.modName)
                }
                val autoDetectionAdapter = object : ArrayAdapter<String>(
                    context,
                    R.layout.sample_app_spinner_item,
                    autoDetectionModList
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView: TextView = view.findViewById(R.id.spinner_item_text)
                        // You can now modify the TextView if needed

                        textView.setTextColor(context.getColor(R.color.sample_app_text_color))
                        return view
                    }

                    override fun getDropDownView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        // Customize the dropdown item appearance here if needed
                        return view
                    }
                }
                autoDetectionSpinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View, position: Int, id: Long
                    ) {
                        var config = WoundGeniusSDK.getConfiguration()
                        config = config.copy(
                            autoDetectionMode = WoundAutoDetectionMode.entries[position]
                        )
                        WoundGeniusSDK.updateConfig(config)

                        if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                            viewModel?.saveFeatureStatus(woundGeniusSDK)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No-op: the spinner always has a selection, so nothing to handle here
                    }
                }

                autoDetectionSpinner.adapter = autoDetectionAdapter
                val selectedWoundAutoDetectionMode = WoundAutoDetectionMode.values().withIndex()
                    .find { it.value == currentAutoDetectMod }?.index ?: 0

                autoDetectionSpinner.setSelection(selectedWoundAutoDetectionMode, false)
            }
        }
    }


    private fun onLicenseUpdate( // NOSONAR Cognitive Complexity — settings UI/view code, refactor requires on-device verification
        availableFeatures: List<String>,
        sdkFeaturesStatus: SdkFeatureStatus
    ) {
        binding.apply {
            viewModel?.apply {

                var config = WoundGeniusSDK.getConfiguration()
                var isStomaFlow: Boolean
                var isMultipleOutlinesEnabled: Boolean
                var isAddFromLocalStorageAvailable: Boolean
                var isBodyPartPickerAvailable: Boolean
                var isMeasurementLineEnabled: Boolean
                var isSingleAreaEnabled: Boolean
                var isFrontCameraUsageAllowed: Boolean
                var isLandScapeSupported = false
                var autoDetectionMode: WoundAutoDetectionMode

                var isEnabled =
                    sdkFeaturesStatus.availableModes?.contains(ImitoCameraMode.VIDEO_MODE) ?: false

                if (availableFeatures.contains(SdkFeature.VIDEO_CAPTURING.featureName)) {
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(ImitoCameraMode.VIDEO_MODE, isEnabled)
                    addVideoModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(ImitoCameraMode.VIDEO_MODE, false)
                    addVideoModeS.isChecked = false
                }

                addVideoModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.VIDEO_CAPTURING.featureName)

                if (availableFeatures.contains(SdkFeature.STOMA_DOCUMENTATION.featureName)) {
                    val isStomaFlowEnabled =
                        sdkFeaturesStatus.isStomaFlowEnable ?: false
                        isStomaFlow = isStomaFlowEnabled
                    stomaFlowS.isChecked = isStomaFlowEnabled
                } else {
                        isStomaFlow = false
                    stomaFlowS.isChecked = false
                }

                if (availableFeatures.contains(SdkFeature.PHOTO_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(ImitoCameraMode.PHOTO_MODE) ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(ImitoCameraMode.PHOTO_MODE, isEnabled)
                    addPhotoModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(ImitoCameraMode.PHOTO_MODE, false)
                    addPhotoModeS.isChecked = false
                }

                addPhotoModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.PHOTO_CAPTURING.featureName)


                if (availableFeatures.contains(SdkFeature.MARKER_MEASUREMENT_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(ImitoCameraMode.MARKER_DETECT_MODE)
                            ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(ImitoCameraMode.MARKER_DETECT_MODE, isEnabled)
                    addMarkerModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(ImitoCameraMode.MARKER_DETECT_MODE, false)
                    addMarkerModeS.isChecked = false
                }

                addMarkerModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.MARKER_MEASUREMENT_CAPTURING.featureName)

                if (availableFeatures.contains(SdkFeature.RULER_MEASUREMENT_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(ImitoCameraMode.MANUAL_MEASURE_MODE)
                            ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    onCameraModsChange(ImitoCameraMode.MANUAL_MEASURE_MODE, isEnabled)
                    addRulerModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(ImitoCameraMode.MANUAL_MEASURE_MODE, false)
                    addRulerModeS.isChecked = false
                }

                addRulerModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.RULER_MEASUREMENT_CAPTURING.featureName)

                if (availableFeatures.contains(SdkFeature.MULTIPLE_WOUNDS_PER_IMAGE.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isMultipleOutlinesSupported ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }

                        isMultipleOutlinesEnabled = isEnabled
                    isMultipleOutlinesEnabledS.isChecked = isEnabled
                } else {

                        isMultipleOutlinesEnabled = false
                    isMultipleOutlinesEnabledS.isChecked =
                        false
                }


                isMultipleOutlinesEnabledLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.MULTIPLE_WOUNDS_PER_IMAGE.featureName)

                if (availableFeatures.contains(SdkFeature.WOUND_DETECTION.featureName)) {
                    val woundAutoDetectionMode =
                        sdkFeaturesStatus.autoDetectionMode


                        autoDetectionMode = if (WoundGeniusSDK.getConfiguration().isStomaFlow) WoundAutoDetectionMode.NONE else woundAutoDetectionMode

                    setupAutoDetectionModsUi()
                } else {

                        autoDetectionMode = WoundAutoDetectionMode.NONE
                    autoDetectionSpinner.setSelection(0, false)
                }

                machineLearningLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.WOUND_DETECTION.featureName)


                if (availableFeatures.contains(SdkFeature.LIVE_WOUND_DETECTION.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isLiveDetectionEnabled ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    liveDetectionS.isChecked =
                        if (WoundGeniusSDK.getConfiguration().isStomaFlow) false else isEnabled
                } else {
                    liveDetectionS.isChecked = false
                }
                liveDetectionLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.LIVE_WOUND_DETECTION.featureName)

                maxMediaNumberValueACET.setText(sdkFeaturesStatus.maxNumberOfMedia.toString())

                minMediaNumberValueACET.setText(sdkFeaturesStatus.minNumberOfMedia.toString())

                stomaFlowLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.STOMA_DOCUMENTATION.featureName)


                if (availableFeatures.contains(SdkFeature.LOCAL_STORAGE_IMAGES.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isMediaFromGalleryAllowed ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }

                        isAddFromLocalStorageAvailable = isEnabled
                    addFromGalleryS.isChecked = isEnabled
                } else {
                        isAddFromLocalStorageAvailable = false
                    addFromGalleryS.isChecked = false
                }
                addFromGalleryLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.LOCAL_STORAGE_IMAGES.featureName)

                if (availableFeatures.contains(SdkFeature.BODY_PART_PICKER.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isBodyPickerAllowed ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }

                        isBodyPartPickerAvailable = isEnabled

                    addBodyPickerOnCameraS.isChecked = isEnabled
                } else {

                        isBodyPartPickerAvailable = false

                    addBodyPickerOnCameraS.isChecked = false
                }

                if (availableFeatures.contains(SdkFeature.LINE_MEASUREMENT.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isMeasurementLineEnabled ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }

                        isMeasurementLineEnabled = isEnabled
                    isMeasurementLineEnabledS.isChecked = isEnabled
                } else {
                   
                        isMeasurementLineEnabled = true

                    isMeasurementLineEnabledS.isChecked = true
                }

                isMeasurementLineEnabledBlockerCL.isVisible = !availableFeatures.contains(SdkFeature.LINE_MEASUREMENT.featureName)



                if (availableFeatures.contains(SdkFeature.SINGLE_AREA_MODE.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isSingleAreaEnabled ?: false

                    isSingleAreaEnabled = isEnabled
                    isSingleAreaEnabledS.isChecked = isEnabled
                } else {

                        isSingleAreaEnabled = false
                    isSingleAreaEnabledS.isChecked = false
                }

                isSingleAreaEnabledBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.SINGLE_AREA_MODE.featureName)

                addBodyPickerOnCameraLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.BODY_PART_PICKER.featureName)

                if (availableFeatures.contains(SdkFeature.FRONTAL_CAMERA.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isFrontalCameraSupported ?: false
                    if (wasLicenseIncorrect) {
                        isEnabled = true
                    }
                    
                        isFrontCameraUsageAllowed = isEnabled
                    
                    addCameraSwitchS.isChecked = isEnabled
                } else {
                   
                        isFrontCameraUsageAllowed = false
                    addCameraSwitchS.isChecked = false
                }

                addCameraSwitchLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.FRONTAL_CAMERA.featureName)

                activity?.let {
                    val isOnlyPortrait =
                        isSupportPortraitOnly(it)
                    if (isOnlyPortrait) {
                        isLandscapeSupportedBlockerCL.isVisible = true
                        isLandScapeSupportedS.isChecked = false
                       isLandScapeSupported = false
                        onConfigurationChange(it)
                    } else {
                        isLandScapeSupportedS.isChecked =
                            WoundGeniusSDK.getConfiguration().isLandscapeSupported && (
                                    sdkFeaturesStatus.isLandScapeSupported || it.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_FULL_USER)
                    }
                }
                
                config = config.copy(
                    isStomaFlow = isStomaFlow,
                    isMultipleOutlinesEnabled = isMultipleOutlinesEnabled,
                    isAddFromLocalStorageAvailable = isAddFromLocalStorageAvailable,
                    isBodyPartPickerAvailable = isBodyPartPickerAvailable,
                    isMeasurementLineEnabled = isMeasurementLineEnabled,
                    isSingleAreaEnabled = isSingleAreaEnabled,
                    isFrontCameraUsageAllowed = isFrontCameraUsageAllowed,
                    isLandscapeSupported = isLandScapeSupported,
                    autoDetectionMode = autoDetectionMode
                )

                WoundGeniusSDK.updateConfig(config)

                setupAutoDetectionModsUi()
                if (availableFeatures.isNotEmpty()) {
                    wasLicenseIncorrect = false
                    saveFeatureStatus(woundGeniusSDK)
                }
            }
        }
    }

    companion object {
        const val MAX_MEDIA_CAPTURE_SIZE = "100"
        const val MIN_MEDIA_CAPTURE_SIZE = "1"
        fun newInstance() = SettingsScreenFragment()
    }

}
