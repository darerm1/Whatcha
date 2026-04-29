package com.darerm1.whatcha.data.sdui.analytics

interface AnalyticsTracker {
    fun trackEvent(analyticsId: String, action: String, extras: Map<String, String> = emptyMap())
}
