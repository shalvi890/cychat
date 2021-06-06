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

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import com.cioinfotech.cychat.core.dialogs.UnrecognizedCertificateDialog
import com.cioinfotech.cychat.core.error.ErrorFormatter
import com.cioinfotech.cychat.core.preference.UserAvatarPreference
import com.cioinfotech.cychat.features.MainActivity
import com.cioinfotech.cychat.features.auth.ReAuthActivity
import com.cioinfotech.cychat.features.call.CallControlsBottomSheet
import com.cioinfotech.cychat.features.call.VectorCallActivity
import com.cioinfotech.cychat.features.call.conference.VectorJitsiActivity
import com.cioinfotech.cychat.features.call.transfer.CallTransferActivity
import com.cioinfotech.cychat.features.createdirect.CreateDirectRoomActivity
import com.cioinfotech.cychat.features.crypto.keysbackup.settings.KeysBackupManageActivity
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageActivity
import com.cioinfotech.cychat.features.crypto.recover.BootstrapBottomSheet
import com.cioinfotech.cychat.features.crypto.verification.VerificationBottomSheet
import com.cioinfotech.cychat.features.debug.DebugMenuActivity
import com.cioinfotech.cychat.features.devtools.RoomDevToolActivity
import com.cioinfotech.cychat.features.home.HomeActivity
import com.cioinfotech.cychat.features.home.HomeModule
import com.cioinfotech.cychat.features.home.room.detail.RoomDetailActivity
import com.cioinfotech.cychat.features.home.room.detail.readreceipts.DisplayReadReceiptsBottomSheet
import com.cioinfotech.cychat.features.home.room.detail.search.SearchActivity
import com.cioinfotech.cychat.features.home.room.detail.timeline.action.MessageActionsBottomSheet
import com.cioinfotech.cychat.features.home.room.detail.timeline.edithistory.ViewEditHistoryBottomSheet
import com.cioinfotech.cychat.features.home.room.detail.timeline.reactions.ViewReactionsBottomSheet
import com.cioinfotech.cychat.features.home.room.detail.widget.RoomWidgetsBottomSheet
import com.cioinfotech.cychat.features.home.room.filtered.FilteredRoomsActivity
import com.cioinfotech.cychat.features.home.room.list.RoomListModule
import com.cioinfotech.cychat.features.home.room.list.actions.RoomListQuickActionsBottomSheet
import com.cioinfotech.cychat.features.invite.InviteUsersToRoomActivity
import com.cioinfotech.cychat.features.invite.VectorInviteView
import com.cioinfotech.cychat.features.link.LinkHandlerActivity
import com.cioinfotech.cychat.features.login.LoginActivity
import com.cioinfotech.cychat.features.matrixto.MatrixToBottomSheet
import com.cioinfotech.cychat.features.media.BigImageViewerActivity
import com.cioinfotech.cychat.features.media.VectorAttachmentViewerActivity
import com.cioinfotech.cychat.features.navigation.Navigator
import com.cioinfotech.cychat.features.permalink.PermalinkHandlerActivity
import com.cioinfotech.cychat.features.pin.PinLocker
import com.cioinfotech.cychat.features.qrcode.QrCodeScannerActivity
import com.cioinfotech.cychat.features.rageshake.BugReportActivity
import com.cioinfotech.cychat.features.rageshake.BugReporter
import com.cioinfotech.cychat.features.rageshake.RageShake
import com.cioinfotech.cychat.features.reactions.EmojiReactionPickerActivity
import com.cioinfotech.cychat.features.reactions.widget.ReactionButton
import com.cioinfotech.cychat.features.roomdirectory.RoomDirectoryActivity
import com.cioinfotech.cychat.features.roomdirectory.createroom.CreateRoomActivity
import com.cioinfotech.cychat.features.roommemberprofile.RoomMemberProfileActivity
import com.cioinfotech.cychat.features.roommemberprofile.devices.DeviceListBottomSheet
import com.cioinfotech.cychat.features.roomprofile.RoomProfileActivity
import com.cioinfotech.cychat.features.roomprofile.alias.detail.RoomAliasBottomSheet
import com.cioinfotech.cychat.features.roomprofile.settings.historyvisibility.RoomHistoryVisibilityBottomSheet
import com.cioinfotech.cychat.features.roomprofile.settings.joinrule.RoomJoinRuleBottomSheet
import com.cioinfotech.cychat.features.settings.VectorSettingsActivity
import com.cioinfotech.cychat.features.settings.devices.DeviceVerificationInfoBottomSheet
import com.cioinfotech.cychat.features.share.IncomingShareActivity
import com.cioinfotech.cychat.features.signout.soft.SoftLogoutActivity
import com.cioinfotech.cychat.features.terms.ReviewTermsActivity
import com.cioinfotech.cychat.features.ui.UiStateRepository
import com.cioinfotech.cychat.features.usercode.UserCodeActivity
import com.cioinfotech.cychat.features.widgets.WidgetActivity
import com.cioinfotech.cychat.features.widgets.permissions.RoomWidgetPermissionBottomSheet
import com.cioinfotech.cychat.features.workers.signout.SignOutBottomSheetDialogFragment

@Component(
        dependencies = [
            VectorComponent::class
        ],
        modules = [
            ViewModelModule::class,
            FragmentModule::class,
            HomeModule::class,
            RoomListModule::class,
            ScreenModule::class
        ]
)
@ScreenScope
interface ScreenComponent {

    /* ==========================================================================================
     * Shortcut to VectorComponent elements
     * ========================================================================================== */

    fun activeSessionHolder(): ActiveSessionHolder
    fun fragmentFactory(): FragmentFactory
    fun viewModelFactory(): ViewModelProvider.Factory
    fun bugReporter(): BugReporter
    fun rageShake(): RageShake
    fun navigator(): Navigator
    fun pinLocker(): PinLocker
    fun errorFormatter(): ErrorFormatter
    fun uiStateRepository(): UiStateRepository
    fun unrecognizedCertificateDialog(): UnrecognizedCertificateDialog

    /* ==========================================================================================
     * Activities
     * ========================================================================================== */

    fun inject(activity: HomeActivity)
    fun inject(activity: RoomDetailActivity)
    fun inject(activity: RoomProfileActivity)
    fun inject(activity: RoomMemberProfileActivity)
    fun inject(activity: VectorSettingsActivity)
    fun inject(activity: KeysBackupManageActivity)
    fun inject(activity: EmojiReactionPickerActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: LinkHandlerActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: RoomDirectoryActivity)
    fun inject(activity: BugReportActivity)
    fun inject(activity: FilteredRoomsActivity)
    fun inject(activity: CreateRoomActivity)
    fun inject(activity: CreateDirectRoomActivity)
    fun inject(activity: IncomingShareActivity)
    fun inject(activity: SoftLogoutActivity)
    fun inject(activity: PermalinkHandlerActivity)
    fun inject(activity: QrCodeScannerActivity)
    fun inject(activity: DebugMenuActivity)
    fun inject(activity: SharedSecureStorageActivity)
    fun inject(activity: BigImageViewerActivity)
    fun inject(activity: InviteUsersToRoomActivity)
    fun inject(activity: ReviewTermsActivity)
    fun inject(activity: WidgetActivity)
    fun inject(activity: VectorCallActivity)
    fun inject(activity: VectorAttachmentViewerActivity)
    fun inject(activity: VectorJitsiActivity)
    fun inject(activity: SearchActivity)
    fun inject(activity: UserCodeActivity)
    fun inject(activity: CallTransferActivity)
    fun inject(activity: ReAuthActivity)
    fun inject(activity: RoomDevToolActivity)

    /* ==========================================================================================
     * BottomSheets
     * ========================================================================================== */

    fun inject(bottomSheet: MessageActionsBottomSheet)
    fun inject(bottomSheet: ViewReactionsBottomSheet)
    fun inject(bottomSheet: ViewEditHistoryBottomSheet)
    fun inject(bottomSheet: DisplayReadReceiptsBottomSheet)
    fun inject(bottomSheet: RoomListQuickActionsBottomSheet)
    fun inject(bottomSheet: RoomAliasBottomSheet)
    fun inject(bottomSheet: RoomHistoryVisibilityBottomSheet)
    fun inject(bottomSheet: RoomJoinRuleBottomSheet)
    fun inject(bottomSheet: VerificationBottomSheet)
    fun inject(bottomSheet: DeviceVerificationInfoBottomSheet)
    fun inject(bottomSheet: DeviceListBottomSheet)
    fun inject(bottomSheet: BootstrapBottomSheet)
    fun inject(bottomSheet: RoomWidgetPermissionBottomSheet)
    fun inject(bottomSheet: RoomWidgetsBottomSheet)
    fun inject(bottomSheet: CallControlsBottomSheet)
    fun inject(bottomSheet: SignOutBottomSheetDialogFragment)
    fun inject(bottomSheet: MatrixToBottomSheet)

    /* ==========================================================================================
     * Others
     * ========================================================================================== */

    fun inject(view: VectorInviteView)
    fun inject(preference: UserAvatarPreference)
    fun inject(button: ReactionButton)

    /* ==========================================================================================
     * Factory
     * ========================================================================================== */

    @Component.Factory
    interface Factory {
        fun create(vectorComponent: VectorComponent,
                   @BindsInstance context: AppCompatActivity
        ): ScreenComponent
    }
}
