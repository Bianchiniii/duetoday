package br.com.contaemdia.presentation.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {
    fun requestConsent(activity: Activity, onComplete: () -> Unit) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable ||
                    consentInformation.privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                ) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                        onComplete()
                    }
                } else {
                    onComplete()
                }
            },
            {
                onComplete()
            },
        )
    }
}
