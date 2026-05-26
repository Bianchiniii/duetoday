package br.com.contaemdia.presentation.ads

enum class AdPlacement(val adUnitId: String) {
    DashboardBanner(TEST_BANNER_AD_UNIT_ID),
    SummaryBanner(TEST_BANNER_AD_UNIT_ID),
}

object AdMobConfig {
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val DASHBOARD_BANNER_PLACEHOLDER = "dashboard_banner"
    const val SUMMARY_BANNER_PLACEHOLDER = "summary_banner"
}

private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
