package com.federico.mylibrary.ads

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.federico.mylibrary.util.Logger

@Composable
fun AdBannerView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            Logger.d("AdBannerView", "Costruzione banner in corso...")
            val adView = AdView(context)
            adView.setAdSize(AdSize.BANNER)
            adView.adUnitId = "ca-app-pub-8207353706287420/3556553271"
            //adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" // ✅ Test ID
            adView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adView.loadAd(AdRequest.Builder().build())
            adView
        }
    )
}

