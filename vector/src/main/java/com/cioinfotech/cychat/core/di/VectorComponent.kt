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

package com.cioinfotech.cychat.core.di

import android.content.Context
import android.content.res.Resources
import com.cioinfotech.cychat.ActiveSessionDataSource
import com.cioinfotech.cychat.EmojiCompatFontProvider
import com.cioinfotech.cychat.EmojiCompatWrapper
import com.cioinfotech.cychat.VectorApplication
import com.cioinfotech.cychat.core.dialogs.UnrecognizedCertificateDialog
import com.cioinfotech.cychat.core.error.ErrorFormatter
import com.cioinfotech.cychat.core.network.WifiDetector
import com.cioinfotech.cychat.core.pushers.PushersManager
import com.cioinfotech.cychat.core.utils.AssetReader
import com.cioinfotech.cychat.core.utils.DimensionConverter
import com.cioinfotech.cychat.features.call.webrtc.WebRtcCallManager
import com.cioinfotech.cychat.features.configuration.VectorConfiguration
import com.cioinfotech.cychat.features.crypto.keysrequest.KeyRequestHandler
import com.cioinfotech.cychat.features.crypto.verification.IncomingVerificationRequestHandler
import com.cioinfotech.cychat.features.cycore.CyChatModule
import com.cioinfotech.cychat.features.cycore.NetworkModule
import com.cioinfotech.cychat.features.cycore.service.CyCoreService
import com.cioinfotech.cychat.features.grouplist.SelectedGroupDataSource
import com.cioinfotech.cychat.features.home.AvatarRenderer
import com.cioinfotech.cychat.features.home.room.detail.RoomDetailPendingActionStore
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.MatrixItemColorProvider
import com.cioinfotech.cychat.features.home.room.detail.timeline.helper.RoomSummariesHolder
import com.cioinfotech.cychat.features.html.EventHtmlRenderer
import com.cioinfotech.cychat.features.html.VectorHtmlCompressor
import com.cioinfotech.cychat.features.login.ReAuthHelper
import com.cioinfotech.cychat.features.navigation.Navigator
import com.cioinfotech.cychat.features.notifications.NotifiableEventResolver
import com.cioinfotech.cychat.features.notifications.NotificationBroadcastReceiver
import com.cioinfotech.cychat.features.notifications.NotificationDrawerManager
import com.cioinfotech.cychat.features.notifications.NotificationUtils
import com.cioinfotech.cychat.features.notifications.PushRuleTriggerListener
import com.cioinfotech.cychat.features.pin.PinCodeStore
import com.cioinfotech.cychat.features.pin.PinLocker
import com.cioinfotech.cychat.features.popup.PopupAlertManager
import com.cioinfotech.cychat.features.rageshake.BugReporter
import com.cioinfotech.cychat.features.rageshake.VectorFileLogger
import com.cioinfotech.cychat.features.rageshake.VectorUncaughtExceptionHandler
import com.cioinfotech.cychat.features.reactions.data.EmojiDataSource
import com.cioinfotech.cychat.features.session.SessionListener
import com.cioinfotech.cychat.features.settings.VectorPreferences
import com.cioinfotech.cychat.features.ui.UiStateRepository
import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.auth.HomeServerHistoryService
import org.matrix.android.sdk.api.raw.RawService
import org.matrix.android.sdk.api.session.Session
import javax.inject.Singleton

@Component(modules = [VectorModule::class, NetworkModule::class, CyChatModule::class])
@Singleton
interface VectorComponent {

    fun inject(notificationBroadcastReceiver: NotificationBroadcastReceiver)

    fun inject(vectorApplication: VectorApplication)

    fun matrix(): Matrix

    fun matrixItemColorProvider(): MatrixItemColorProvider

    fun sessionListener(): SessionListener

    fun currentSession(): Session

    fun notificationUtils(): NotificationUtils

    fun notificationDrawerManager(): NotificationDrawerManager

    fun appContext(): Context

    fun resources(): Resources

    fun assetReader(): AssetReader

    fun dimensionConverter(): DimensionConverter

    fun vectorConfiguration(): VectorConfiguration

    fun avatarRenderer(): AvatarRenderer

    fun activeSessionHolder(): ActiveSessionHolder

    fun unrecognizedCertificateDialog(): UnrecognizedCertificateDialog

    fun emojiCompatFontProvider(): EmojiCompatFontProvider

    fun emojiCompatWrapper(): EmojiCompatWrapper

    fun eventHtmlRenderer(): EventHtmlRenderer

    fun vectorHtmlCompressor(): VectorHtmlCompressor

    fun navigator(): Navigator

    fun errorFormatter(): ErrorFormatter

    fun selectedGroupStore(): SelectedGroupDataSource

    fun roomDetailPendingActionStore(): RoomDetailPendingActionStore

    fun activeSessionObservableStore(): ActiveSessionDataSource

    fun incomingVerificationRequestHandler(): IncomingVerificationRequestHandler

    fun incomingKeyRequestHandler(): KeyRequestHandler

    fun authenticationService(): AuthenticationService

    fun rawService(): RawService

    fun homeServerHistoryService(): HomeServerHistoryService

    fun bugReporter(): BugReporter

    fun vectorUncaughtExceptionHandler(): VectorUncaughtExceptionHandler

    fun pushRuleTriggerListener(): PushRuleTriggerListener

    fun pusherManager(): PushersManager

    fun notifiableEventResolver(): NotifiableEventResolver

    fun vectorPreferences(): VectorPreferences

    fun wifiDetector(): WifiDetector

    fun vectorFileLogger(): VectorFileLogger

    fun uiStateRepository(): UiStateRepository

    fun pinCodeStore(): PinCodeStore

    fun emojiDataSource(): EmojiDataSource

    fun alertManager(): PopupAlertManager

    fun reAuthHelper(): ReAuthHelper

    fun pinLocker(): PinLocker

    fun webRtcCallManager(): WebRtcCallManager

    fun roomSummaryHolder(): RoomSummariesHolder

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): VectorComponent
    }

    fun okHttpClient(): OkHttpClient

    fun cyChatService(): CyCoreService
}
