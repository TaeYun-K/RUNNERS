package com.runners.app

import android.app.Application
import android.content.Context
import android.os.Build
import com.google.android.gms.ads.MobileAds
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class RunnersApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        MobileAds.initialize(this)
    }

    companion object {
        @Volatile
        lateinit var appContext: Context
            private set

        /** 알림 클릭 시 이동할 게시글 ID (cold start 시 사용) */
        @Volatile
        var pendingNotificationPostId: Long? = null
            private set

        /** 알림 클릭 시 게시글 ID 전달 (앱 실행 중일 때 RunnersNavHost에서 수집) */
        private val _pendingNotificationPostIdFlow = MutableSharedFlow<Long>(replay = 0)
        val pendingNotificationPostIdFlow: SharedFlow<Long> = _pendingNotificationPostIdFlow

        fun setPendingNotificationPostId(postId: Long) {
            pendingNotificationPostId = postId
            _pendingNotificationPostIdFlow.tryEmit(postId)
        }

        /** 저장된 pending postId를 반환하고 초기화. cold start 후 네비게이션에 사용. */
        fun takePendingNotificationPostId(): Long? {
            val value = pendingNotificationPostId
            pendingNotificationPostId = null
            return value
        }
    }
}
