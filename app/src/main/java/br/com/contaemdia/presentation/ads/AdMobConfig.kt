package br.com.contaemdia.presentation.ads

import br.com.contaemdia.BuildConfig

enum class AdPlacement(val adUnitId: String) {
    DashboardInlineBanner(BuildConfig.ADMOB_DASHBOARD_INLINE_BANNER_ID),
    DashboardBottomBanner(BuildConfig.ADMOB_DASHBOARD_BOTTOM_BANNER_ID),
    SummaryInlineBanner(BuildConfig.ADMOB_SUMMARY_INLINE_BANNER_ID),
    SummaryBottomBanner(BuildConfig.ADMOB_SUMMARY_BOTTOM_BANNER_ID),
    DetailBottomBanner(BuildConfig.ADMOB_DETAIL_BOTTOM_BANNER_ID),
}

object AdMobConfig {
    const val DASHBOARD_INLINE_BANNER_PLACEHOLDER = "dashboard_inline_banner"
    const val DASHBOARD_BOTTOM_BANNER_PLACEHOLDER = "dashboard_bottom_banner"
    const val SUMMARY_INLINE_BANNER_PLACEHOLDER = "summary_inline_banner"
    const val SUMMARY_BOTTOM_BANNER_PLACEHOLDER = "summary_bottom_banner"
    const val DETAIL_BOTTOM_BANNER_PLACEHOLDER = "detail_bottom_banner"
}
