package io.imito.woundgenius.sample.ui.screen.homescreen

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.imito.woundgenius.sample.data.pojo.assessment.SampleAssessmentEntity
import io.imito.woundgenius.sample.data.pojo.license.SdkFeatureStatus
import io.imito.woundgenius.sample.data.usecase.assessment.DeleteDraftAssessmentByIdUseCase
import io.imito.woundgenius.sample.data.usecase.assessment.GetAssessmentsUseCase
import io.imito.woundgenius.sample.data.usecase.assessment.SaveAssessmentToDBUseCase
import io.imito.woundgenius.sample.data.usecase.license.GetLicenseKeyUseCase
import io.imito.woundgenius.sample.data.usecase.license.GetSdkFeaturesUseCase
import io.imito.woundgenius.sample.ui.screen.base.AbsViewModel
import io.imito.woundgenius.sample.utils.toSampleAssessmentEntity
import io.imito.woundgenius.sdk.internal.data.pojo.bodypart.WoundGeniusBodyPart
import io.imito.woundgenius.sdk.internal.data.pojo.license.LicenseErrorType
import io.imito.woundgenius.sdk.internal.data.pojo.license.LicenseValidateResult
import io.imito.woundgenius.sdk.internal.data.pojo.measurement.MeasurementResult
import io.imito.woundgenius.sdk.api.WoundGeniusSDK
import io.imito.woundgenius.sdk.internal.managers.wizard.AssessmentWizardResult
import javax.inject.Inject

class HomeScreenViewModel @Inject constructor(
    private val getAssessmentsUseCase: GetAssessmentsUseCase,
    private val deleteDraftAssessmentByIdUseCase: DeleteDraftAssessmentByIdUseCase,
    private val saveAssessmentToDBUseCase: SaveAssessmentToDBUseCase,
    private val getSdkFeaturesUseCase: GetSdkFeaturesUseCase,
    private val getLicenseKeyUseCase: GetLicenseKeyUseCase
) : AbsViewModel(

) {
    private val _bodyPartSelectedLD = MutableLiveData<List<WoundGeniusBodyPart>?>()
    val bodyPartSelectedLD: LiveData<List<WoundGeniusBodyPart>?>
        get() = _bodyPartSelectedLD

    private val _assessmentsResponseLD = MutableLiveData<List<SampleAssessmentEntity>>()
    val assessmentsResponseLD: LiveData<List<SampleAssessmentEntity>>
        get() = _assessmentsResponseLD

    private val _assessmentProgress = MutableLiveData<Boolean>()
    val assessmentProgress: LiveData<Boolean>
        get() = _assessmentProgress

    private val _isMeasurementChartExpandLD = MutableLiveData(false)
    val isMeasurementChartExpandLD: LiveData<Boolean>
        get() = _isMeasurementChartExpandLD

    private val _noLicenseKeyErrorDialog = MutableLiveData<Unit?>()
    val noLicenseKeyErrorDialog: LiveData<Unit?>
        get() = _noLicenseKeyErrorDialog

    private val _licenseErrorDialog = MutableLiveData<Triple<Boolean, LicenseErrorType?, Unit?>>()
    val licenseErrorDialog: LiveData<Triple<Boolean, LicenseErrorType?, Unit?>>
        get() = _licenseErrorDialog

    private val _isNoLicenseError = MutableLiveData(false)
    val isNoLicenseError: LiveData<Boolean>
        get() = _isNoLicenseError

    private val _availableFeatures = MutableLiveData<List<String>>()
    val availableFeatures: LiveData<List<String>>
        get() = _availableFeatures

    private val _onSavedLicenseKeyReceived = MutableLiveData<Unit?>()
    val onSavedLicenseKeyReceived: LiveData<Unit?>
        get() = _onSavedLicenseKeyReceived

    private val _sdkFeaturesStatusLD = MutableLiveData<SdkFeatureStatus?>()
    val sdkFeaturesStatusLD: LiveData<SdkFeatureStatus?>
        get() = _sdkFeaturesStatusLD

    fun changeSelectedBodyParts(bodyParts: List<WoundGeniusBodyPart>) {
        _bodyPartSelectedLD.value = bodyParts
    }

    fun getLicenseKey() {
        val params = GetLicenseKeyUseCase.Params.forGetLicenseKey()
        add(
            getLicenseKeyUseCase.execute(params)
                .subscribe({
                    WoundGeniusSDK.setLicenseKey(it)
                    _onSavedLicenseKeyReceived.value = Unit
                    _onSavedLicenseKeyReceived.value = null
                }, {
                    Log.e("woundGeniusError", it.stackTraceToString())
                })
        )
    }

    fun getFeatureStatus() {

        val params = GetSdkFeaturesUseCase.Params.forGetSdkFeatures()
        add(
            getSdkFeaturesUseCase.execute(params)
                .subscribe({
                    if (sdkFeaturesStatusLD.value != it) {
                        _sdkFeaturesStatusLD.value = it
                    }
                }, {
                    Log.e("woundGeniusError", it.stackTraceToString())
                })
        )
    }


    fun openLicenseIssueDialog(dialogType: LicenseErrorType?) {
        _licenseErrorDialog.value = Triple(true, dialogType, Unit)
        _licenseErrorDialog.value = Triple(true, dialogType, null)
    }

    fun openNoLicenseKeyDialog() {
        _noLicenseKeyErrorDialog.value = Unit
        _noLicenseKeyErrorDialog.value = null
    }

    fun handleLicenseResult(result: LicenseValidateResult) {
        if (result is LicenseValidateResult.onError) {
            when (result.info) {
                LicenseErrorType.NO_LICENSE_KEY.value -> {
                    _isNoLicenseError.value = true
                    openNoLicenseKeyDialog()
                }

                LicenseErrorType.ILLEGALLY_MODIFIED.value -> {
                    openLicenseIssueDialog(LicenseErrorType.ILLEGALLY_MODIFIED)
                }

                LicenseErrorType.FAILED_TO_ENCODE_CONTENT.value -> {
                    openLicenseIssueDialog(LicenseErrorType.FAILED_TO_ENCODE_CONTENT)
                }

                LicenseErrorType.CORRUPTED_DATE_FORMAT.value -> {
                    openLicenseIssueDialog(LicenseErrorType.CORRUPTED_DATE_FORMAT)
                }

                LicenseErrorType.LICENSE_EXPIRED.value -> {
                    openLicenseIssueDialog(LicenseErrorType.LICENSE_EXPIRED)
                }

                LicenseErrorType.APP_ID_IS_LOCKED.value -> {
                    openLicenseIssueDialog(LicenseErrorType.APP_ID_IS_LOCKED)
                }

                LicenseErrorType.NO_UNLOCKED_FEATURES.value -> {
                    openLicenseIssueDialog(LicenseErrorType.NO_UNLOCKED_FEATURES)
                }
            }
        } else {
            if (result is LicenseValidateResult.onSuccess) {
                _availableFeatures.value = result.features
                _isNoLicenseError.value = false
                _licenseErrorDialog.value = Triple(false, null, null)
            }
        }
    }

    fun onExpandChartClick(isExpand: Boolean) {
        _isMeasurementChartExpandLD.value = !isExpand
    }

    fun getAssessmentList() {
        val param = GetAssessmentsUseCase.Params.forGetDraftAssessments()
        add(
            getAssessmentsUseCase.execute(param)
                .subscribe({ draftAssessments ->
                    val newList = ArrayList(draftAssessments.sortedBy { it.id })
                    _assessmentsResponseLD.value = newList
                }, {
                    Log.e("woundGeniusError", it.stackTraceToString())
                    handleError(it)
                })
        )
    }

    fun saveAssessmentToDB(
        measurements: List<MeasurementResult>
    ) {
        measurements.forEach {measurement ->
            val params =
                SaveAssessmentToDBUseCase.Params.forSaveAssessmentToDB(
                    measurement.toSampleAssessmentEntity()
                )
            _assessmentProgress.value = false
            add(
                saveAssessmentToDBUseCase.execute(params)
                    .subscribe({
                    }, {
                        Log.e("woundGeniusError", it.stackTraceToString())
                    })
            )
        }
    }

    fun saveMagicAssessmentToDB(
        context: Context,
        result: AssessmentWizardResult.Success
    ) {
        val entity = result.toSampleAssessmentEntity(context)
        val params = SaveAssessmentToDBUseCase.Params.forSaveAssessmentToDB(entity)
        _assessmentProgress.value = false
        add(
            saveAssessmentToDBUseCase.execute(params)
                .subscribe({
                }, {
                    Log.e("woundGeniusError", it.stackTraceToString())
                })
        )
    }

    fun deleteAssessment(id: Long) {
        _assessmentProgress.value = true
        val params = DeleteDraftAssessmentByIdUseCase.Params.forDeleteAssessment(id)

        add(
            deleteDraftAssessmentByIdUseCase.execute(params)
                .subscribe({
                    _assessmentProgress.value = false
                }, {
                    Log.e("woundGeniusError", it.stackTraceToString())
                    handleError(it)
                })
        )
    }


}
