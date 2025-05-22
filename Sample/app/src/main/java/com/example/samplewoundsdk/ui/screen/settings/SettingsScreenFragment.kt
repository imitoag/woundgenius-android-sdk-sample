package com.example.samplewoundsdk.ui.screen.settings

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.data.pojo.license.SdkFeatureStatus
import com.example.samplewoundsdk.databinding.SampleAppFragmentSettingsScreenBinding
import com.example.samplewoundsdk.ui.screen.base.AbsFragment
import com.example.samplewoundsdk.utils.FileLogTree
import com.example.woundsdk.data.pojo.autodetectionmod.WoundAutoDetectionMode
import com.example.woundsdk.data.pojo.cameramod.CameraMods
import com.example.woundsdk.di.WoundGeniusSDK
import com.example.woundsdk.utils.SdkFeature


class SettingsScreenFragment : AbsFragment<SettingsScreenViewModel>() {

    override fun provideViewModelClass() = SettingsScreenViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_fragment_settings_screen

    private lateinit var binding: SampleAppFragmentSettingsScreenBinding

    private var wasLicenseIncorrect = false

    override fun initListeners() {
        binding.apply {
            backButtonACTV.setOnClickListener {
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
//                    viewModel?.saveFeatureStatus()
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
                WoundGeniusSDK.configure(
                    isAddBodyPickerOnCaptureScreenAvailable = isChecked,
                    captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE)
                )
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            addFromGalleryLayoutCL.setOnClickListener {
                WoundGeniusSDK.configure(
                    isAddFromLocalStorageAvailable = !addFromGalleryS.isChecked,
                    captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE)
                )
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
                addFromGalleryS.isChecked = !addFromGalleryS.isChecked
            }
            isMultipleOutlinesEnabledLayoutCL.setOnClickListener {
                isMultipleOutlinesEnabledS.isChecked = !isMultipleOutlinesEnabledS.isChecked
            }
            addFromGalleryS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    isAddFromLocalStorageAvailable = isChecked,
                )
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            isMultipleOutlinesEnabledS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    isMultipleOutlinesEnabled = isMultipleOutlinesEnabledS.isChecked,
                )
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }

            addVideoModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.VIDEO_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            addVideoModeLayoutCL.setOnClickListener {
                addVideoModeS.isChecked = !addVideoModeS.isChecked
            }

            addMarkerModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.MARKER_DETECT_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            addMarkerModeLayoutCL.setOnClickListener {
                addMarkerModeS.isChecked = !addMarkerModeS.isChecked
            }

            addPhotoModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.PHOTO_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            addPhotoModeLayoutCL.setOnClickListener {
                addPhotoModeS.isChecked = !addPhotoModeS.isChecked
            }

            liveDetectionLayoutCL.setOnClickListener {
                liveDetectionS.isChecked = !liveDetectionS.isChecked
            }

            addRulerModeS.setOnCheckedChangeListener { _, isChecked ->
                onCameraModsChange(CameraMods.MANUAL_MEASURE_MODE, isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            liveDetectionS.setOnCheckedChangeListener { _, isChecked ->
                Log.d("settings", "liveDetection switch = $isChecked")
                WoundGeniusSDK.configure(isLiveDetectionEnabled = isChecked)
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            addRulerModeLayoutCL.setOnClickListener {
                addRulerModeS.isChecked = !addRulerModeS.isChecked
            }

            stomaFlowS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    woundAutoDetectionMode = if (isChecked) WoundAutoDetectionMode.NONE else WoundGeniusSDK.getAutoDetectionMod(),
                    isLiveDetectionEnabled = if (isChecked) false else WoundGeniusSDK.getIsLiveDetectionEnabled(),
                    isStomaFlow = isChecked,
                    captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE)
                )
                if (isChecked) {
                    liveDetectionS.isChecked = false
                    setupAutoDetectionModsUi()
                }
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
            }
            stomaFlowLayoutCL.setOnClickListener {
                stomaFlowS.isChecked = !stomaFlowS.isChecked
            }

            addCameraSwitchS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(
                    isFrontCameraUsageAllowed = isChecked,
                    captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE)
                )
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
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
                            captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE)
                        )
                    }
                }
            )
            licenseKeyValueACET.onDone {
                WoundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
                if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                    viewModel?.saveFeatureStatus()
                }
                activity?.onBackPressed()
            }
            licenseKeyValueACET.doOnTextChanged { text, start, before, count ->
                WoundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
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
            captureScreenTitle = getString(R.string.WOUND_GENIUS_SDK_CAPTURE_SCREEN_TITLE)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel?.apply {
            binding.apply {
                context?.let { context ->
                    getUserId()
                    setUpUiTheme()
                    setupAutoDetectionModsUi()
                    licenseKeyValueACET.setText(WoundGeniusSDK.getLicenseKey().toString())
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
                        if (availableFeatures.isNullOrEmpty()){
                            wasLicenseIncorrect = true
                        }
                        sdkFeaturesStatusLD.value?.let {
                            onLicenseUpdate(availableFeatures,it)
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
            viewModel?.saveFeatureStatus()
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
                WoundGeniusSDK.getTextColor()?.let {
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
                        WoundGeniusSDK.setTextColor(textColors[position].second)
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
                WoundGeniusSDK.getFormsColor()?.let {
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
                        WoundGeniusSDK.setFormsColor(textColors[position].second)
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
                WoundGeniusSDK.getMeasurementResultColor()?.let {
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
                        WoundGeniusSDK.setMeasurementResultColor(textColors[position].second)
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
                WoundGeniusSDK.getValueDividersColor()?.let {
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
                        WoundGeniusSDK.setDividerColor(textColors[position].second)
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
                val primaryButtonColor = WoundGeniusSDK.getPrimaryButtonColor()?.toInt()
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
                        WoundGeniusSDK.setPrimaryButtonColor(primaryColors[position].second)
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
                val lightBGColor = WoundGeniusSDK.getLightBackgroundColor()?.toInt()
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
                        WoundGeniusSDK.setLightBackgroundColor(lightBGColors[position].second)
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

            var backgroundColor: Int? = null
            var primaryButtonColor: Int? = null
            var textColor: Int? = null
            var dividerColor: Int? = null

            backgroundColor = WoundGeniusSDK.getLightBackgroundColor()?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_background
            )

            primaryButtonColor = WoundGeniusSDK.getPrimaryButtonColor()?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_button_color
            )

            textColor = WoundGeniusSDK.getTextColor()?.let {
                context?.getColor(
                    it.toInt()
                )
            } ?: context?.getColor(
                R.color.sample_app_text_color
            )


            dividerColor = WoundGeniusSDK.getValueDividersColor()?.let {
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
                val currentAutoDetectMod = WoundGeniusSDK.getAutoDetectionMod()
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
                        WoundGeniusSDK.configure(woundAutoDetectionMode = WoundAutoDetectionMode.values()[position])
                        if (licenseKeyValueACET.text.toString().isNotEmpty()) {
                            viewModel?.saveFeatureStatus()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }
                Log.d("selectedMod", currentAutoDetectMod?.modName ?: "")
                autoDetectionSpinner.adapter = autoDetectionAdapter
                val selectedWoundAutoDetectionMode = WoundAutoDetectionMode.values().withIndex()
                    .find { it.value == currentAutoDetectMod }?.index ?: 0

                autoDetectionSpinner.setSelection(selectedWoundAutoDetectionMode, false)
            }
        }
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
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.VIDEO_MODE, isEnabled)
                    addVideoModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(CameraMods.VIDEO_MODE, false)
                    addVideoModeS.isChecked = false
                }

                addVideoModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.VIDEO_CAPTURING.featureName)

                if (availableFeatures.contains(SdkFeature.STOMA_DOCUMENTATION.featureName)) {
                    val isStomaFlowEnabled =
                        sdkFeaturesStatus.isStomaFlowEnable ?: false
                    WoundGeniusSDK.configure(
                        isStomaFlow = isStomaFlowEnabled
                    )
                    stomaFlowS.isChecked = isStomaFlowEnabled
                } else {
                    WoundGeniusSDK.configure(
                        isStomaFlow = false
                    )
                    stomaFlowS.isChecked = false
                }

                if (availableFeatures.contains(SdkFeature.PHOTO_CAPTURING.featureName)) {
                    isEnabled = sdkFeaturesStatus.availableModes?.contains(CameraMods.PHOTO_MODE) ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.PHOTO_MODE, isEnabled)
                    addPhotoModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(CameraMods.PHOTO_MODE, false)
                    addPhotoModeS.isChecked = false
                }

                addPhotoModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.PHOTO_CAPTURING.featureName)


                if (availableFeatures.contains(SdkFeature.MARKER_MEASUREMENT_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(CameraMods.MARKER_DETECT_MODE)
                            ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.MARKER_DETECT_MODE, isEnabled)
                    addMarkerModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(CameraMods.MARKER_DETECT_MODE, false)
                    addMarkerModeS.isChecked = false
                }

                addMarkerModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.MARKER_MEASUREMENT_CAPTURING.featureName)

                if (availableFeatures.contains(SdkFeature.RULER_MEASUREMENT_CAPTURING.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.availableModes?.contains(CameraMods.MANUAL_MEASURE_MODE)
                            ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    onCameraModsChange(CameraMods.MANUAL_MEASURE_MODE, isEnabled)
                    addRulerModeS.isChecked = isEnabled
                } else {
                    onCameraModsChange(CameraMods.MANUAL_MEASURE_MODE, false)
                    addRulerModeS.isChecked = false
                }

                addRulerModeLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.RULER_MEASUREMENT_CAPTURING.featureName)

                if (availableFeatures.contains(SdkFeature.MULTIPLE_WOUNDS_PER_IMAGE.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isMultipleOutlinesSupported ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    WoundGeniusSDK.configure(
                        isMultipleOutlinesEnabled = isEnabled
                    )
                    isMultipleOutlinesEnabledS.isChecked = isEnabled
                } else {
                    WoundGeniusSDK.configure(
                        isMultipleOutlinesEnabled = false
                    )
                    isMultipleOutlinesEnabledS.isChecked =
                        false
                }

                isMultipleOutlinesEnabledLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.MULTIPLE_WOUNDS_PER_IMAGE.featureName)

                if (availableFeatures.contains(SdkFeature.WOUND_DETECTION.featureName)) {
                    val woundAutoDetectionMode =
                        sdkFeaturesStatus.autoDetectionMode

                    WoundGeniusSDK.configure(
                        woundAutoDetectionMode = if (WoundGeniusSDK.getIsStomaFlow()) WoundAutoDetectionMode.NONE else woundAutoDetectionMode
                    )

                    setupAutoDetectionModsUi()
                } else {
                    WoundGeniusSDK.configure(
                        woundAutoDetectionMode = WoundAutoDetectionMode.NONE
                    )
                    autoDetectionSpinner.setSelection(0, false)
                }

                machineLearningLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.WOUND_DETECTION.featureName)


                if (availableFeatures.contains(SdkFeature.LIVE_WOUND_DETECTION.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isLiveDetectionEnabled ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    Log.d(
                        "settings",
                        "liveDetection license = ${if (WoundGeniusSDK.getIsStomaFlow()) false else isEnabled}"
                    )
                    liveDetectionS.isChecked =
                        if (WoundGeniusSDK.getIsStomaFlow()) false else isEnabled
                } else {
                    liveDetectionS.isChecked = false
                }
                liveDetectionLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.LIVE_WOUND_DETECTION.featureName)

                maxMediaNumberValueACET.setText(sdkFeaturesStatus.maxNumberOfMedia.toString())

                stomaFlowLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.STOMA_DOCUMENTATION.featureName)


                if (availableFeatures.contains(SdkFeature.LOCAL_STORAGE_IMAGES.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isMediaFromGalleryAllowed ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    WoundGeniusSDK.configure(
                        isAddFromLocalStorageAvailable = isEnabled
                    )
                    addFromGalleryS.isChecked = isEnabled
                } else {
                    WoundGeniusSDK.configure(
                        isAddFromLocalStorageAvailable = false
                    )
                    addFromGalleryS.isChecked = false
                }
                addFromGalleryLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.LOCAL_STORAGE_IMAGES.featureName)

                if (availableFeatures.contains(SdkFeature.BODY_PART_PICKER.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isBodyPickerAllowed ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    WoundGeniusSDK.configure(
                        isAddBodyPickerOnCaptureScreenAvailable = isEnabled
                    )
                    addBodyPickerOnCameraS.isChecked = isEnabled
                } else {
                    WoundGeniusSDK.configure(
                        isAddBodyPickerOnCaptureScreenAvailable = false
                    )
                    addBodyPickerOnCameraS.isChecked = false
                }
                addBodyPickerOnCameraLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.BODY_PART_PICKER.featureName)

                if (availableFeatures.contains(SdkFeature.FRONTAL_CAMERA.featureName)) {
                    isEnabled =
                        sdkFeaturesStatus.isFrontalCameraSupported ?: false
                    if (wasLicenseIncorrect){
                        isEnabled = true
                    }
                    WoundGeniusSDK.configure(
                        isFrontCameraUsageAllowed = isEnabled
                    )
                    addCameraSwitchS.isChecked = isEnabled
                } else {
                    WoundGeniusSDK.configure(
                        isFrontCameraUsageAllowed = false
                    )
                    addCameraSwitchS.isChecked = false
                }
                addCameraSwitchLayoutBlockerCL.isVisible =
                    !availableFeatures.contains(SdkFeature.FRONTAL_CAMERA.featureName)
                setupAutoDetectionModsUi()
                if (availableFeatures.isNotEmpty()) {
                    wasLicenseIncorrect = false
                    saveFeatureStatus()
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
