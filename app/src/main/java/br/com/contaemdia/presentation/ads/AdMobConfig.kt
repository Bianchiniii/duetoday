package br.com.contaemdia.presentation.ads

enum class AdPlacement(val adUnitId: String) {
    DashboardInlineBanner(TEST_BANNER_AD_UNIT_ID),
    DashboardBottomBanner(TEST_BANNER_AD_UNIT_ID),
    SummaryInlineBanner(TEST_BANNER_AD_UNIT_ID),
    SummaryBottomBanner(TEST_BANNER_AD_UNIT_ID),
    DetailBottomBanner(TEST_BANNER_AD_UNIT_ID),
}

object AdMobConfig {
    const val DASHBOARD_INLINE_BANNER_PLACEHOLDER = "dashboard_inline_banner"
    const val DASHBOARD_BOTTOM_BANNER_PLACEHOLDER = "dashboard_bottom_banner"
    const val SUMMARY_INLINE_BANNER_PLACEHOLDER = "summary_inline_banner"
    const val SUMMARY_BOTTOM_BANNER_PLACEHOLDER = "summary_bottom_banner"
    const val DETAIL_BOTTOM_BANNER_PLACEHOLDER = "detail_bottom_banner"
}

private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
