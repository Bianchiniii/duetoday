package br.com.contaemdia.presentation.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

enum class AdBannerFormat {
    InlineLarge,
    BottomAnchored,
}

@Composable
fun AdBanner(
    placement: AdPlacement,
    adsEnabled: Boolean,
    modifier: Modifier = Modifier,
    format: AdBannerFormat = AdBannerFormat.InlineLarge,
) {
    if (!adsEnabled) return

    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val adSize = remember(format, widthDp) {
        when (format) {
            AdBannerFormat.InlineLarge -> AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
            AdBannerFormat.BottomAnchored -> AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
    }
    val adView = remember(placement, format, widthDp) {
        AdView(context).apply {
            adUnitId = placement.adUnitId
            setAdSize(adSize)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(adView) {
        onDispose { adView.destroy() }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(adSize.height.dp)) {
            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
