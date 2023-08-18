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
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.example.samplewoundsdk.R
import com.example.samplewoundsdk.databinding.SampleAppFragmentSettingsScreenBinding
import com.example.samplewoundsdk.ui.screen.base.AbsFragment
import com.example.woundsdk.di.WoundGeniusSDK


class SettingsScreenFragment : AbsFragment<SettingsScreenViewModel>() {

    override fun provideViewModelClass() = SettingsScreenViewModel::class

    override fun provideLayoutId() = R.layout.sample_app_fragment_settings_screen

    lateinit var binding: SampleAppFragmentSettingsScreenBinding

    override fun initListeners() {
        binding.apply {
            backButtonACTV.setOnClickListener {
                activity?.onBackPressed()
            }
            addBodyPickerOnCameraLayoutCL.setOnClickListener {
                addBodyPickerOnCameraS.isChecked = !addBodyPickerOnCameraS.isChecked
            }
            addBodyPickerOnCameraS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(isAddBodyPickerOnCaptureScreenAvailable = isChecked)
            }
            addFromGalleryLayoutCL.setOnClickListener {
                addFromGalleryS.isChecked = !addFromGalleryS.isChecked
            }
            addFromGalleryS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(isAddFromLocalStorageAvailable = isChecked)
            }
            isMultipleOutlinesEnabledS.setOnCheckedChangeListener { _, isChecked ->
                WoundGeniusSDK.configure(isMultipleOutlinesEnabled = isChecked)
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
                            maxNumberOfMedia = maxMediaNumberValueACET.text.toString().toInt()
                        )
                    }
                }
            )
            licenseKeyValueACET.onDone {
                WoundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
                activity?.onBackPressed()
            }
            licenseKeyValueACET.doOnTextChanged { text, start, before, count ->
                WoundGeniusSDK.setLicenseKey(licenseKeyValueACET.text.toString())
                viewModel?.saveLicenseKey(licenseKeyValueACET.text.toString())
            }
        }
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

    private fun setupPrimaryColorUi(){
        viewModel?.apply {
            binding.apply {
                primaryButtonColorList.observe(viewLifecycleOwner) { primaryColors ->
                    val primaryButtonColor = WoundGeniusSDK.getPrimaryButtonColor()?.toInt() ?: R.color.sample_app_red
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
                    context?.getColor(primaryButtonColor)?.let { editSelectionButtonACTV.setTextColor(it)
                            editSelectionArrowIconACTV.backgroundTintList = ColorStateList.valueOf(it)
                        }
                    val selectedPrimaryColor = primaryColors
                        .withIndex()
                        .find { it.value.second == primaryButtonColor }?.index ?:0
                    primaryColorSpinner.setSelection(selectedPrimaryColor, false)
                }
            }
        }
    }

    private fun setupLightBGUi(){
        viewModel?.apply {
            binding.apply {
                lightBGColorList.observe(viewLifecycleOwner) { lightBGColors ->
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
                        .find { it.value.second == lightBGColor }?.index ?:0

                    lightBGSpinner.setSelection(selectedPrimaryColor, false)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel?.apply {
            binding.apply {
                setupPrimaryColorUi()
                setupLightBGUi()
                if (WoundGeniusSDK.getLicenseKey()?.isNotEmpty() == true){
                    licenseKeyValueACET.setText(WoundGeniusSDK.getLicenseKey())
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
                val maxMediaNumber = WoundGeniusSDK.getMaxNumberOfMedia()
                maxMediaNumberValueACET.setText(maxMediaNumber.toString())
            }
        }
    }

    companion object {
        const val MAX_MEDIA_CAPTURE_SIZE = "100"
        const val MIN_MEDIA_CAPTURE_SIZE = "1"
        fun newInstance() = SettingsScreenFragment()
    }

}
