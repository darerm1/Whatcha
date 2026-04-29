package com.darerm1.whatcha.data.sdui.analytics

import android.util.Log

class LogAnalyticsTracker : AnalyticsTracker {

    override fun trackEvent(analyticsId: String, action: String, extras: Map<String, String>) {
        Log.d(TAG, "Event: id=$analyticsId, action=$action, extras=$extras")
    }

    companion object {
        private const val TAG = "SDUIAnalytics"
    }
}
