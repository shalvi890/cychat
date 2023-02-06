/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cioinfotech.cychat

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.StrictMode
import android.util.Log
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.EpoxyController
import com.facebook.stetho.Stetho
import com.gabrielittner.threetenbp.LazyThreeTen
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.di.DaggerVectorComponent
import com.cioinfotech.cychat.core.di.HasVectorInjector
import com.cioinfotech.cychat.core.di.VectorComponent
import com.cioinfotech.cychat.core.extensions.configureAndStart
import com.cioinfotech.cychat.core.rx.RxConfig
import com.cioinfotech.cychat.features.call.webrtc.WebRtcCallManager
import com.cioinfotech.cychat.features.configuration.VectorConfiguration
import com.cioinfotech.cychat.features.disclaimer.doNotShowDisclaimerDialog
import com.cioinfotech.cychat.features.lifecycle.VectorActivityLifecycleCallbacks
import com.cioinfotech.cychat.features.notifications.NotificationDrawerManager
import com.cioinfotech.cychat.features.notifications.NotificationUtils
import com.cioinfotech.cychat.features.pin.PinLocker
import com.cioinfotech.cychat.features.popup.PopupAlertManager
import com.cioinfotech.cychat.features.rageshake.VectorUncaughtExceptionHandler
import com.cioinfotech.cychat.features.room.VectorRoomDisplayNameFallbackProvider
import com.cioinfotech.cychat.features.settings.VectorLocale
import com.cioinfotech.cychat.features.settings.VectorPreferences
import com.cioinfotech.cychat.features.themes.ThemeUtils
import com.cioinfotech.cychat.push.fcm.FcmHelper
import org.jitsi.meet.sdk.log.JitsiMeetDefaultLogHandler
import org.matrix.android.sdk.api.MatrixConfiguration
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.legacy.LegacySessionImporter
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject
import androidx.work.Configuration as WorkConfiguration

class VectorApplication :
        Application(),
        HasVectorInjector,
        MatrixConfiguration.Provider,
        WorkConfiguration.Provider ,Application.ActivityLifecycleCallbacks{

    lateinit var appContext: Context
    @Inject lateinit var legacySessionImporter: LegacySessionImporter
    @Inject lateinit var authenticationService: AuthenticationService
    @Inject lateinit var vectorConfiguration: VectorConfiguration
    @Inject lateinit var emojiCompatFontProvider: EmojiCompatFontProvider
    @Inject lateinit var emojiCompatWrapper: EmojiCompatWrapper
    @Inject lateinit var vectorUncaughtExceptionHandler: VectorUncaughtExceptionHandler
    @Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var notificationDrawerManager: NotificationDrawerManager
    @Inject lateinit var vectorPreferences: VectorPreferences
//    @Inject lateinit var versionProvider: VersionProvider
    @Inject lateinit var notificationUtils: NotificationUtils
    @Inject lateinit var appStateHandler: AppStateHandler
    @Inject lateinit var rxConfig: RxConfig
    @Inject lateinit var popupAlertManager: PopupAlertManager
    @Inject lateinit var pinLocker: PinLocker
    @Inject lateinit var callManager: WebRtcCallManager

    lateinit var vectorComponent: VectorComponent

    // font thread handler
    private var fontThreadHandler: Handler? = null

    private val powerKeyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF
                    && vectorPreferences.useFlagPinCode()) {
                pinLocker.screenIsOff()
            }
        }
    }

    override fun onCreate() {
        enableStrictModeIfNeeded()
        super.onCreate()
        appContext = this
        vectorComponent = DaggerVectorComponent.factory().create(this)
        vectorComponent.inject(this)
        vectorUncaughtExceptionHandler.activate(this)
        rxConfig.setupRxPlugin()

        // Remove Log handler statically added by Jitsi
        Timber.forest()
                .filterIsInstance(JitsiMeetDefaultLogHandler::class.java)
                .forEach { Timber.uproot(it) }

        if (BuildConfig.debug_mode) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(vectorComponent.vectorFileLogger())

        if (BuildConfig.debug_mode) {
            Stetho.initializeWithDefaults(this)
        }
//        logInfo()
        LazyThreeTen.init(this)

        EpoxyController.defaultDiffingHandler = EpoxyAsyncUtil.getAsyncBackgroundHandler()
        EpoxyController.defaultModelBuildingHandler = EpoxyAsyncUtil.getAsyncBackgroundHandler()
        registerActivityLifecycleCallbacks(VectorActivityLifecycleCallbacks(popupAlertManager))
        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs
        )
        FontsContractCompat.requestFont(this, fontRequest, emojiCompatFontProvider, getFontThreadHandler())
        VectorLocale.init(this)
        ThemeUtils.init(this)
        vectorConfiguration.applyToApplicationContext()

        emojiCompatWrapper.init(fontRequest)

        notificationUtils.createNotificationChannels()

        // It can takes time, but do we care?
        val sessionImported = legacySessionImporter.process()
        if (!sessionImported) {
            // Do not display the name change popup
            doNotShowDisclaimerDialog(this)
        }

        if (authenticationService.hasAuthenticatedSessions() && !activeSessionHolder.hasActiveSession()) {
            val lastAuthenticatedSession = authenticationService.getLastAuthenticatedSession()!!
            activeSessionHolder.setActiveSession(lastAuthenticatedSession)
            lastAuthenticatedSession.configureAndStart(applicationContext)
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun entersForeground() {
                Timber.i("App entered foreground")
                FcmHelper.onEnterForeground(appContext, activeSessionHolder)
                activeSessionHolder.getSafeActiveSession()?.also {
                    it.stopAnyBackgroundSync()
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun entersBackground() {
                Timber.i("App entered background") // call persistInfo
                notificationDrawerManager.persistInfo()
                FcmHelper.onEnterBackground(appContext, vectorPreferences, activeSessionHolder)
            }
        })
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateHandler)
        ProcessLifecycleOwner.get().lifecycle.addObserver(pinLocker)
        ProcessLifecycleOwner.get().lifecycle.addObserver(callManager)
        // This should be done as early as possible
        // initKnownEmojiHashSet(appContext)

        applicationContext.registerReceiver(powerKeyReceiver, IntentFilter().apply {
            // Looks like i cannot receive OFF, if i don't have both ON and OFF
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        })

//        RxJavaPlugins.setErrorHandler { e ->
//            if (e is UndeliverableException) {
//                // Merely log undeliverable exceptions
//                log.error(e.message)
//            } else {
//                // Forward all others to current thread's uncaught exception handler
//                Thread.currentThread().also { thread ->
//                    thread.uncaughtExceptionHandler.uncaughtException(thread, e)
//                }
//            }

        EmojiManager.install(GoogleEmojiProvider())
    }

    private fun enableStrictModeIfNeeded() {
        if (BuildConfig.ENABLE_STRICT_MODE_LOGS) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
    }

    override fun providesMatrixConfiguration(): MatrixConfiguration {
        return MatrixConfiguration(
                applicationFlavor = BuildConfig.FLAVOR_DESCRIPTION,
                roomDisplayNameFallbackProvider = VectorRoomDisplayNameFallbackProvider(this)
        )
    }

    override fun getWorkManagerConfiguration(): WorkConfiguration {
        return WorkConfiguration.Builder()
                .setExecutor(Executors.newCachedThreadPool())
                .build()
    }

    override fun injector(): VectorComponent {
        return vectorComponent
    }

    private fun logInfo() {
//        val appVersion = versionProvider.getVersion(longFormat = true, useBuildNumber = true)
//        val sdkVersion = Matrix.getSdkVersion()
//        val date = SimpleDateFormat("MM-dd HH:mm:ss.SSSZ", Locale.US).format(Date())
//
//        Timber.v("----------------------------------------------------------------")
//        Timber.v("----------------------------------------------------------------")
//        Timber.v(" Application version: $appVersion")
//        Timber.v(" SDK version: $sdkVersion")
//        Timber.v(" Local time: $date")
//        Timber.v("----------------------------------------------------------------")
//        Timber.v("----------------------------------------------------------------\n\n\n\n")
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        vectorConfiguration.onConfigurationChanged()
    }

    private fun getFontThreadHandler(): Handler {
        return fontThreadHandler ?: createFontThreadHandler().also {
            fontThreadHandler = it
        }
    }

    private fun createFontThreadHandler(): Handler {
        val handlerThread = HandlerThread("fonts")
        handlerThread.start()
        return Handler(handlerThread.looper)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        //Log.d("ActivityCreated", p0.localClassName);
    }

    override fun onActivityStarted(p0: Activity) {
       
    }

    override fun onActivityResumed(p0: Activity) {
       //// Log.d("ActivityCreated", p0.localClassName);

    }

    override fun onActivityPaused(p0: Activity) {
       
    }

    override fun onActivityStopped(p0: Activity) {
       
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
       
    }

    override fun onActivityDestroyed(p0: Activity) {
       
    }
}
