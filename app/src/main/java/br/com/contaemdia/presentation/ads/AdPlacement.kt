package br.com.contaemdia.presentation.ads

import br.com.contaemdia.BuildConfig

enum class AdPlacement(val adUnitId: String) {
    DashboardInlineBanner(BuildConfig.ADMOB_DASHBOARD_INLINE_BANNER_ID),
    DashboardBottomBanner(BuildConfig.ADMOB_DASHBOARD_BOTTOM_BANNER_ID),
    SummaryInlineBanner(BuildConfig.ADMOB_SUMMARY_INLINE_BANNER_ID),
    SummaryBottomBanner(BuildConfig.ADMOB_SUMMARY_BOTTOM_BANNER_ID),
    DetailBottomBanner(BuildConfig.ADMOB_DETAIL_BOTTOM_BANNER_ID),
}
