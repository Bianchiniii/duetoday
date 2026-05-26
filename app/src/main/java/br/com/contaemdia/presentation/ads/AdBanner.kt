package br.com.contaemdia.presentation.ads

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

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
    var showContainer by remember(placement, format, widthDp) { mutableStateOf(true) }
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
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    showContainer = true
                    Log.d(TAG, "Ad loaded: placement=$placement adUnitId=$adUnitId")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    showContainer = false
                    Log.w(
                        TAG,
                        "Ad failed: placement=$placement adUnitId=$adUnitId " +
                            "code=${error.code} domain=${error.domain} message=${error.message}",
                    )
                }
            }
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

    if (!showContainer) return

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

private const val TAG = "ContaEmDiaAds"
