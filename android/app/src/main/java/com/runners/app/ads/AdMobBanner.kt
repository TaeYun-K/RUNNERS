package com.runners.app.ads

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.runners.app.R

@Composable
fun CommunityTopBannerAd(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val adUnitId = context.getString(R.string.admob_banner_ad_unit_id)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        AdaptiveBannerAd(
            adUnitId = adUnitId,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
fun InlineBannerAd(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val adUnitId = context.getString(R.string.admob_banner_ad_unit_id)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        AdaptiveBannerAd(
            adUnitId = adUnitId,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AdaptiveBannerAd(
    adUnitId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    var containerWidthPx by remember { mutableIntStateOf(0) }
    val adWidthDp = remember(containerWidthPx, configuration.screenWidthDp, density) {
        val widthDp = if (containerWidthPx > 0) {
            with(density) { containerWidthPx.toDp().value }
        } else {
            configuration.screenWidthDp.toFloat()
        }
        widthDp.toInt().coerceAtLeast(1)
    }

    val adSize = remember(adWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }
    val adHeightDp = with(density) { adSize.getHeightInPixels(context).toDp() }

    val adRequest = remember { AdRequest.Builder().build() }
    var statusText by remember { mutableStateOf("광고 로딩중…") }
    var isLoaded by remember { mutableStateOf(false) }

    val logTag = "AdMobBanner"

    val adView = remember(adUnitId, adSize) {
        AdView(context).apply {
            setAdUnitId(adUnitId)
            setAdSize(adSize)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isLoaded = true
                    statusText = ""
                    Log.d(logTag, "Banner loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoaded = false
                    statusText = "광고 로드 실패: ${error.code}"
                    Log.w(logTag, "Banner failed to load: $error")
                }
            }
        }
    }

    LaunchedEffect(adView, adRequest) {
        isLoaded = false
        statusText = "광고 로딩중…"
        adView.loadAd(adRequest)
    }

    DisposableEffect(lifecycle, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(
        factory = { adView },
        modifier = modifier
            .fillMaxWidth()
            .height(adHeightDp)
            .onSizeChanged { containerWidthPx = it.width },
    )

    if (!isLoaded) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(adHeightDp)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(bottom = 8.dp),
                strokeWidth = 2.dp,
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
