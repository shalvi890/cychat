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

package com.cioinfotech.cychat.features.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.error.fatalError
import com.cioinfotech.cychat.core.platform.VectorBaseActivity
import com.cioinfotech.cychat.core.utils.toast
import com.cioinfotech.cychat.features.call.conference.JitsiCallViewModel
import com.cioinfotech.cychat.features.call.conference.VectorJitsiActivity
import com.cioinfotech.cychat.features.call.transfer.CallTransferActivity
import com.cioinfotech.cychat.features.createdirect.CreateDirectRoomActivity
import com.cioinfotech.cychat.features.crypto.keysbackup.settings.KeysBackupManageActivity
import com.cioinfotech.cychat.features.crypto.keysbackup.setup.KeysBackupSetupActivity
import com.cioinfotech.cychat.features.crypto.recover.BootstrapBottomSheet
import com.cioinfotech.cychat.features.crypto.recover.SetupMode
import com.cioinfotech.cychat.features.crypto.verification.SupportedVerificationMethodsProvider
import com.cioinfotech.cychat.features.crypto.verification.VerificationBottomSheet
import com.cioinfotech.cychat.features.devtools.RoomDevToolActivity
import com.cioinfotech.cychat.features.home.notice.NoticeBoardActivity
import com.cioinfotech.cychat.features.home.room.detail.RoomDetailActivity
import com.cioinfotech.cychat.features.home.room.detail.RoomDetailArgs
import com.cioinfotech.cychat.features.home.room.detail.search.SearchActivity
import com.cioinfotech.cychat.features.home.room.detail.search.SearchArgs
import com.cioinfotech.cychat.features.home.room.filtered.FilteredRoomsActivity
import com.cioinfotech.cychat.features.invite.InviteUsersToRoomActivity
import com.cioinfotech.cychat.features.media.AttachmentData
import com.cioinfotech.cychat.features.media.BigImageViewerActivity
import com.cioinfotech.cychat.features.media.VectorAttachmentViewerActivity
import com.cioinfotech.cychat.features.pin.PinActivity
import com.cioinfotech.cychat.features.pin.PinArgs
import com.cioinfotech.cychat.features.pin.PinMode
import com.cioinfotech.cychat.features.roomdirectory.RoomDirectoryActivity
import com.cioinfotech.cychat.features.roomdirectory.createroom.CreateRoomActivity
import com.cioinfotech.cychat.features.roomdirectory.roompreview.RoomPreviewActivity
import com.cioinfotech.cychat.features.roomdirectory.roompreview.RoomPreviewData
import com.cioinfotech.cychat.features.roommemberprofile.RoomMemberProfileActivity
import com.cioinfotech.cychat.features.roommemberprofile.RoomMemberProfileArgs
import com.cioinfotech.cychat.features.roomprofile.RoomProfileActivity
import com.cioinfotech.cychat.features.settings.VectorPreferences
import com.cioinfotech.cychat.features.settings.VectorSettingsActivity
import com.cioinfotech.cychat.features.share.SharedData
import com.cioinfotech.cychat.features.terms.ReviewTermsActivity
import com.cioinfotech.cychat.features.widgets.WidgetActivity
import com.cioinfotech.cychat.features.widgets.WidgetArgsBuilder
import org.matrix.android.sdk.api.session.crypto.verification.IncomingSasVerificationTransaction
import org.matrix.android.sdk.api.session.room.model.roomdirectory.PublicRoom
import org.matrix.android.sdk.api.session.room.model.thirdparty.RoomDirectoryData
import org.matrix.android.sdk.api.session.terms.TermsService
import org.matrix.android.sdk.api.session.widgets.model.Widget
import org.matrix.android.sdk.api.session.widgets.model.WidgetType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNavigator @Inject constructor(
        private val sessionHolder: ActiveSessionHolder,
        private val vectorPreferences: VectorPreferences,
        private val widgetArgsBuilder: WidgetArgsBuilder,
        private val supportedVerificationMethodsProvider: SupportedVerificationMethodsProvider
) : Navigator {

    override fun openRoom(context: Context, roomId: String, eventId: String?, buildTask: Boolean) {
        if (sessionHolder.getSafeActiveSession()?.getRoom(roomId) == null) {
            fatalError("Trying to open an unknown room $roomId", vectorPreferences.failFast())
            return
        }
        val args = RoomDetailArgs(roomId, eventId)
        val intent = RoomDetailActivity.newIntent(context, args)
        startActivity(context, intent, buildTask)
    }

    override fun performDeviceVerification(context: Context, otherUserId: String, sasTransactionId: String) {
        val session = sessionHolder.getSafeActiveSession() ?: return
        val tx = session.cryptoService().verificationService().getExistingTransaction(otherUserId, sasTransactionId)
                ?: return
        (tx as? IncomingSasVerificationTransaction)?.performAccept()
        if (context is AppCompatActivity) {
            VerificationBottomSheet.withArgs(
                    roomId = null,
                    otherUserId = otherUserId,
                    transactionId = sasTransactionId
            ).show(context.supportFragmentManager, "REQPOP")
        }
    }

    override fun requestSessionVerification(context: Context, otherSessionId: String) {
        val session = sessionHolder.getSafeActiveSession() ?: return
        val pr = session.cryptoService().verificationService().requestKeyVerification(
                supportedVerificationMethodsProvider.provide(),
                session.myUserId,
                listOf(otherSessionId)
        )
        if (context is AppCompatActivity) {
            VerificationBottomSheet.withArgs(
                    roomId = null,
                    otherUserId = session.myUserId,
                    transactionId = pr.transactionId
            ).show(context.supportFragmentManager, VerificationBottomSheet.WAITING_SELF_VERIF_TAG)
        }
    }

    override fun requestSelfSessionVerification(context: Context) {
        val session = sessionHolder.getSafeActiveSession() ?: return
        val otherSessions = session.cryptoService()
                .getCryptoDeviceInfo(session.myUserId)
                .filter { it.deviceId != session.sessionParams.deviceId }
                .map { it.deviceId }
        if (context is AppCompatActivity) {
            if (otherSessions.isNotEmpty()) {
                val pr = session.cryptoService().verificationService().requestKeyVerification(
                        supportedVerificationMethodsProvider.provide(),
                        session.myUserId,
                        otherSessions)
                VerificationBottomSheet.forSelfVerification(session, pr.transactionId ?: pr.localId)
                        .show(context.supportFragmentManager, VerificationBottomSheet.WAITING_SELF_VERIF_TAG)
            } else {
                VerificationBottomSheet.forSelfVerification(session)
                        .show(context.supportFragmentManager, VerificationBottomSheet.WAITING_SELF_VERIF_TAG)
            }
        }
    }

    override fun waitSessionVerification(context: Context) {
        val session = sessionHolder.getSafeActiveSession() ?: return
        if (context is AppCompatActivity) {
            VerificationBottomSheet.forSelfVerification(session)
                    .show(context.supportFragmentManager, VerificationBottomSheet.WAITING_SELF_VERIF_TAG)
        }
    }

    override fun upgradeSessionSecurity(context: Context, initCrossSigningOnly: Boolean) {
        if (context is AppCompatActivity) {
            BootstrapBottomSheet.show(
                    context.supportFragmentManager,
                    if (initCrossSigningOnly) SetupMode.CROSS_SIGNING_ONLY else SetupMode.NORMAL
            )
        }
    }

    override fun openGroupDetail(groupId: String, context: Context, buildTask: Boolean) {
        if (context is VectorBaseActivity<*>) {
            context.notImplemented("Open group detail")
        } else {
            context.toast(R.string.not_implemented)
        }
    }

    override fun openRoomMemberProfile(userId: String, roomId: String?, context: Context, buildTask: Boolean) {
        val args = RoomMemberProfileArgs(userId = userId, roomId = roomId)
        val intent = RoomMemberProfileActivity.newIntent(context, args)
        startActivity(context, intent, buildTask)
    }

    override fun openRoomForSharingAndFinish(activity: Activity, roomId: String, sharedData: SharedData) {
        val args = RoomDetailArgs(roomId, null, sharedData)
        val intent = RoomDetailActivity.newIntent(activity, args)
        activity.startActivity(intent)
        activity.finish()
    }

    override fun openRoomPreview(context: Context, publicRoom: PublicRoom, roomDirectoryData: RoomDirectoryData) {
        val intent = RoomPreviewActivity.newIntent(context, publicRoom, roomDirectoryData)
        context.startActivity(intent)
    }

    override fun openRoomPreview(context: Context, roomPreviewData: RoomPreviewData) {
        val intent = RoomPreviewActivity.newIntent(context, roomPreviewData)
        context.startActivity(intent)
    }

    override fun openRoomDirectory(context: Context, initialFilter: String) {
        val intent = RoomDirectoryActivity.getIntent(context, initialFilter)
        context.startActivity(intent)
    }

    override fun openCreateRoom(context: Context, initialName: String) {
        val intent = CreateRoomActivity.getIntent(context, initialName)
        context.startActivity(intent)
    }

    override fun openCreateDirectRoom(context: Context) {
        val intent = CreateDirectRoomActivity.getIntent(context)
        context.startActivity(intent)
    }

    override fun openInviteUsersToRoom(context: Context, roomId: String) {
        val intent = InviteUsersToRoomActivity.getIntent(context, roomId)
        context.startActivity(intent)
    }

    override fun openRoomsFiltering(context: Context) {
        val intent = FilteredRoomsActivity.newIntent(context)
        context.startActivity(intent)
    }

    override fun openSettings(context: Context, directAccess: Int) {
        val intent = VectorSettingsActivity.getIntent(context, directAccess)
        context.startActivity(intent)
    }

    override fun openKeysBackupSetup(context: Context, showManualExport: Boolean) {
        // if cross signing is enabled and trusted or not set up at all we should propose full 4S
        sessionHolder.getSafeActiveSession()?.let { session ->
            if (session.cryptoService().crossSigningService().getMyCrossSigningKeys() == null
                    || session.cryptoService().crossSigningService().canCrossSign()) {
                (context as? AppCompatActivity)?.let {
                    BootstrapBottomSheet.show(it.supportFragmentManager, SetupMode.NORMAL)
                }
            } else {
                context.startActivity(KeysBackupSetupActivity.intent(context, showManualExport))
            }
        }
    }

    override fun open4SSetup(context: Context, setupMode: SetupMode) {
        if (context is AppCompatActivity) {
            BootstrapBottomSheet.show(context.supportFragmentManager, setupMode)
        }
    }

    override fun openKeysBackupManager(context: Context) {
        context.startActivity(KeysBackupManageActivity.intent(context))
    }

    override fun openRoomProfile(context: Context, roomId: String, directAccess: Int?) {
        context.startActivity(RoomProfileActivity.newIntent(context, roomId, directAccess))
    }

    override fun openBigImageViewer(activity: Activity, sharedElement: View?, mxcUrl: String?, title: String?) {
        mxcUrl
                ?.takeIf { it.isNotBlank() }
                ?.let { avatarUrl ->
                    val intent = BigImageViewerActivity.newIntent(activity, title, avatarUrl)
                    val options = sharedElement?.let {
                        ActivityOptionsCompat.makeSceneTransitionAnimation(activity, it, ViewCompat.getTransitionName(it) ?: "")
                    }
                    activity.startActivity(intent, options?.toBundle())
                }
    }

    override fun openTerms(context: Context,
                           activityResultLauncher: ActivityResultLauncher<Intent>,
                           serviceType: TermsService.ServiceType,
                           baseUrl: String,
                           token: String?) {
        val intent = ReviewTermsActivity.intent(context, serviceType, baseUrl, token)
        activityResultLauncher.launch(intent)
    }

    override fun openStickerPicker(context: Context,
                                   activityResultLauncher: ActivityResultLauncher<Intent>,
                                   roomId: String,
                                   widget: Widget) {
        val widgetArgs = widgetArgsBuilder.buildStickerPickerArgs(roomId, widget)
        val intent = WidgetActivity.newIntent(context, widgetArgs)
        activityResultLauncher.launch(intent)
    }

    override fun openIntegrationManager(context: Context,
                                        activityResultLauncher: ActivityResultLauncher<Intent>,
                                        roomId: String,
                                        integId: String?,
                                        screen: String?) {
        val widgetArgs = widgetArgsBuilder.buildIntegrationManagerArgs(roomId, integId, screen)
        val intent = WidgetActivity.newIntent(context, widgetArgs)
        activityResultLauncher.launch(intent)
    }

    override fun openRoomWidget(context: Context, roomId: String, widget: Widget, options: Map<String, Any>?) {
        if (widget.type is WidgetType.Jitsi) {
            val enableVideo = options?.get(JitsiCallViewModel.ENABLE_VIDEO_OPTION) == true
            context.startActivity(VectorJitsiActivity.newIntent(context, roomId = roomId, widgetId = widget.widgetId, enableVideo = enableVideo))
        } else {
            val widgetArgs = widgetArgsBuilder.buildRoomWidgetArgs(roomId, widget)
            context.startActivity(WidgetActivity.newIntent(context, widgetArgs))
        }
    }

    override fun openPinCode(context: Context,
                             activityResultLauncher: ActivityResultLauncher<Intent>,
                             pinMode: PinMode) {
        val intent = PinActivity.newIntent(context, PinArgs(pinMode))
        activityResultLauncher.launch(intent)
    }

    override fun openMediaViewer(activity: Activity,
                                 roomId: String,
                                 mediaData: AttachmentData,
                                 view: View,
                                 inMemory: List<AttachmentData>,
                                 options: ((MutableList<Pair<View, String>>) -> Unit)?) {
        VectorAttachmentViewerActivity.newIntent(activity,
                mediaData,
                roomId,
                mediaData.eventId,
                inMemory,
                ViewCompat.getTransitionName(view)).let { intent ->
            val pairs = ArrayList<Pair<View, String>>()
            activity.window.decorView.findViewById<View>(android.R.id.statusBarBackground)?.let {
                pairs.add(Pair(it, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME))
            }
            activity.window.decorView.findViewById<View>(android.R.id.navigationBarBackground)?.let {
                pairs.add(Pair(it, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME))
            }

            pairs.add(Pair(view, ViewCompat.getTransitionName(view) ?: ""))
            options?.invoke(pairs)

            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *pairs.toTypedArray()).toBundle()
            activity.startActivity(intent, bundle)
        }
    }

    override fun openSearch(context: Context, roomId: String) {
        val intent = SearchActivity.newIntent(context, SearchArgs(roomId))
        context.startActivity(intent)
    }

    override fun openDevTools(context: Context, roomId: String) {
        context.startActivity(RoomDevToolActivity.intent(context, roomId))
    }

    override fun openCallTransfer(context: Context, callId: String) {
        val intent = CallTransferActivity.newIntent(context, callId)
        context.startActivity(intent)
    }

    override fun openNoticeBoardActivity(context: Context, createMode: Boolean) {
        context.startActivity(NoticeBoardActivity.getIntent(context, createMode))
    }

    private fun startActivity(context: Context, intent: Intent, buildTask: Boolean) {
        if (buildTask) {
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addNextIntentWithParentStack(intent)
            stackBuilder.startActivities()
        } else {
            context.startActivity(intent)
        }
    }
}
