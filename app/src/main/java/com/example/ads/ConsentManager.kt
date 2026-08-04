package com.example.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConsentManager(private val activity: Activity) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(activity)
    
    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()
    
    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()


    init {
        _canRequestAds.value = consentInformation.canRequestAds()
        _isPrivacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun gatherConsent(onConsentGathered: () -> Unit) {
        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Log.w("ConsentManager", "${loadAndShowError.errorCode}: ${loadAndShowError.message}")
                    }
                    updateConsentStatus()
                    onConsentGathered()
                }
            },
            { requestConsentError ->
                Log.w("ConsentManager", "${requestConsentError.errorCode}: ${requestConsentError.message}")
                updateConsentStatus()
                onConsentGathered()
            }
        )
        
        if (consentInformation.canRequestAds()) {
            updateConsentStatus()
        }
    }

    private fun updateConsentStatus() {
        _canRequestAds.value = consentInformation.canRequestAds()
        _isPrivacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        
        if (consentInformation.canRequestAds()) {
        }
    }



    fun showPrivacyOptionsForm(onFormDismissed: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w("ConsentManager", "${formError.errorCode}: ${formError.message}")
            }
            onFormDismissed()
        }
    }
}
