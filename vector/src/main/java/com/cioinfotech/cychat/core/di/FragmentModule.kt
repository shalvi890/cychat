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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.cioinfotech.cychat.features.attachments.preview.AttachmentsPreviewFragment
import com.cioinfotech.cychat.features.contactsbook.ContactsBookFragment
import com.cioinfotech.cychat.features.crypto.keysbackup.settings.KeysBackupSettingsFragment
import com.cioinfotech.cychat.features.crypto.quads.SharedSecuredStorageKeyFragment
import com.cioinfotech.cychat.features.crypto.quads.SharedSecuredStoragePassphraseFragment
import com.cioinfotech.cychat.features.crypto.quads.SharedSecuredStorageResetAllFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapConclusionFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapConfirmPassphraseFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapEnterPassphraseFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapMigrateBackupFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapReAuthFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapSaveRecoveryKeyFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapSetupRecoveryKeyFragment
import com.cioinfotech.cychat.features.crypto.recover.BootstrapWaitingFragment
import com.cioinfotech.cychat.features.crypto.verification.QuadSLoadingFragment
import com.cioinfotech.cychat.features.crypto.verification.cancel.VerificationCancelFragment
import com.cioinfotech.cychat.features.crypto.verification.cancel.VerificationNotMeFragment
import com.cioinfotech.cychat.features.crypto.verification.choose.VerificationChooseMethodFragment
import com.cioinfotech.cychat.features.crypto.verification.conclusion.VerificationConclusionFragment
import com.cioinfotech.cychat.features.crypto.verification.emoji.VerificationEmojiCodeFragment
import com.cioinfotech.cychat.features.crypto.verification.qrconfirmation.VerificationQRWaitingFragment
import com.cioinfotech.cychat.features.crypto.verification.qrconfirmation.VerificationQrScannedByOtherFragment
import com.cioinfotech.cychat.features.crypto.verification.request.VerificationRequestFragment
import com.cioinfotech.cychat.features.devtools.RoomDevToolEditFragment
import com.cioinfotech.cychat.features.devtools.RoomDevToolFragment
import com.cioinfotech.cychat.features.devtools.RoomDevToolSendFormFragment
import com.cioinfotech.cychat.features.devtools.RoomDevToolStateEventListFragment
import com.cioinfotech.cychat.features.discovery.DiscoverySettingsFragment
import com.cioinfotech.cychat.features.discovery.change.SetIdentityServerFragment
import com.cioinfotech.cychat.features.grouplist.GroupListFragment
import com.cioinfotech.cychat.features.home.HomeDetailFragment
import com.cioinfotech.cychat.features.home.HomeDrawerFragment
import com.cioinfotech.cychat.features.home.LoadingFragment
import com.cioinfotech.cychat.features.home.room.breadcrumbs.BreadcrumbsFragment
import com.cioinfotech.cychat.features.home.room.detail.RoomDetailFragment
import com.cioinfotech.cychat.features.home.room.detail.search.SearchFragment
import com.cioinfotech.cychat.features.home.room.list.RoomListFragment
import com.cioinfotech.cychat.features.login.LoginCaptchaFragment
import com.cioinfotech.cychat.features.login.LoginFragment
import com.cioinfotech.cychat.features.login.LoginGenericTextInputFormFragment
import com.cioinfotech.cychat.features.login.LoginResetPasswordFragment
import com.cioinfotech.cychat.features.login.LoginResetPasswordMailConfirmationFragment
import com.cioinfotech.cychat.features.login.LoginResetPasswordSuccessFragment
import com.cioinfotech.cychat.features.login.LoginServerSelectionFragment
import com.cioinfotech.cychat.features.login.LoginServerUrlFormFragment
import com.cioinfotech.cychat.features.login.LoginSignUpSignInSelectionFragment
import com.cioinfotech.cychat.features.login.LoginSplashFragment
import com.cioinfotech.cychat.features.login.LoginWaitForEmailFragment
import com.cioinfotech.cychat.features.login.LoginWebFragment
import com.cioinfotech.cychat.features.login.terms.LoginTermsFragment
import com.cioinfotech.cychat.features.pin.PinFragment
import com.cioinfotech.cychat.features.qrcode.QrCodeScannerFragment
import com.cioinfotech.cychat.features.reactions.EmojiChooserFragment
import com.cioinfotech.cychat.features.reactions.EmojiSearchResultFragment
import com.cioinfotech.cychat.features.roomdirectory.PublicRoomsFragment
import com.cioinfotech.cychat.features.roomdirectory.createroom.CreateRoomFragment
import com.cioinfotech.cychat.features.roomdirectory.picker.RoomDirectoryPickerFragment
import com.cioinfotech.cychat.features.roomdirectory.roompreview.RoomPreviewNoPreviewFragment
import com.cioinfotech.cychat.features.roommemberprofile.RoomMemberProfileFragment
import com.cioinfotech.cychat.features.roommemberprofile.devices.DeviceListFragment
import com.cioinfotech.cychat.features.roommemberprofile.devices.DeviceTrustInfoActionFragment
import com.cioinfotech.cychat.features.roomprofile.RoomProfileFragment
import com.cioinfotech.cychat.features.roomprofile.alias.RoomAliasFragment
import com.cioinfotech.cychat.features.roomprofile.banned.RoomBannedMemberListFragment
import com.cioinfotech.cychat.features.roomprofile.members.RoomMemberListFragment
import com.cioinfotech.cychat.features.roomprofile.permissions.RoomPermissionsFragment
import com.cioinfotech.cychat.features.roomprofile.settings.RoomSettingsFragment
import com.cioinfotech.cychat.features.roomprofile.uploads.RoomUploadsFragment
import com.cioinfotech.cychat.features.roomprofile.uploads.files.RoomUploadsFilesFragment
import com.cioinfotech.cychat.features.roomprofile.uploads.media.RoomUploadsMediaFragment
import com.cioinfotech.cychat.features.settings.CyverseAddRoleFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsAdvancedNotificationPreferenceFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsGeneralFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsHelpAboutFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsLabsFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsNotificationPreferenceFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsNotificationsTroubleshootFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsPinFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsPreferencesFragment
import com.cioinfotech.cychat.features.settings.CyverseSettingsProfileFragment
import com.cioinfotech.cychat.features.settings.VectorSettingsSecurityPrivacyFragment
import com.cioinfotech.cychat.features.settings.account.deactivation.DeactivateAccountFragment
import com.cioinfotech.cychat.features.settings.crosssigning.CrossSigningSettingsFragment
import com.cioinfotech.cychat.features.settings.devices.VectorSettingsDevicesFragment
import com.cioinfotech.cychat.features.settings.devtools.AccountDataFragment
import com.cioinfotech.cychat.features.settings.devtools.GossipingEventsPaperTrailFragment
import com.cioinfotech.cychat.features.settings.devtools.IncomingKeyRequestListFragment
import com.cioinfotech.cychat.features.settings.devtools.KeyRequestsFragment
import com.cioinfotech.cychat.features.settings.devtools.OutgoingKeyRequestListFragment
import com.cioinfotech.cychat.features.settings.homeserver.HomeserverSettingsFragment
import com.cioinfotech.cychat.features.settings.ignored.VectorSettingsIgnoredUsersFragment
import com.cioinfotech.cychat.features.settings.locale.LocalePickerFragment
import com.cioinfotech.cychat.features.settings.push.PushGatewaysFragment
import com.cioinfotech.cychat.features.settings.push.PushRulesFragment
import com.cioinfotech.cychat.features.settings.threepids.ThreePidsSettingsFragment
import com.cioinfotech.cychat.features.share.IncomingShareFragment
import com.cioinfotech.cychat.features.signout.soft.SoftLogoutFragment
import com.cioinfotech.cychat.features.terms.ReviewTermsFragment
import com.cioinfotech.cychat.features.usercode.ShowUserCodeFragment
import com.cioinfotech.cychat.features.userdirectory.UserListFragment
import com.cioinfotech.cychat.features.widgets.WidgetFragment
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface FragmentModule {
    /**
     * Fragments with @IntoMap will be injected by this factory
     */
    @Binds
    fun bindFragmentFactory(factory: VectorFragmentFactory): FragmentFactory

    @Binds
    @IntoMap
    @FragmentKey(RoomListFragment::class)
    fun bindRoomListFragment(fragment: RoomListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LocalePickerFragment::class)
    fun bindLocalePickerFragment(fragment: LocalePickerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(GroupListFragment::class)
    fun bindGroupListFragment(fragment: GroupListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDetailFragment::class)
    fun bindRoomDetailFragment(fragment: RoomDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDirectoryPickerFragment::class)
    fun bindRoomDirectoryPickerFragment(fragment: RoomDirectoryPickerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CreateRoomFragment::class)
    fun bindCreateRoomFragment(fragment: CreateRoomFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomPreviewNoPreviewFragment::class)
    fun bindRoomPreviewNoPreviewFragment(fragment: RoomPreviewNoPreviewFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(KeysBackupSettingsFragment::class)
    fun bindKeysBackupSettingsFragment(fragment: KeysBackupSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoadingFragment::class)
    fun bindLoadingFragment(fragment: LoadingFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(HomeDrawerFragment::class)
    fun bindHomeDrawerFragment(fragment: HomeDrawerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(HomeDetailFragment::class)
    fun bindHomeDetailFragment(fragment: HomeDetailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EmojiSearchResultFragment::class)
    fun bindEmojiSearchResultFragment(fragment: EmojiSearchResultFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginFragment::class)
    fun bindLoginFragment(fragment: LoginFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginCaptchaFragment::class)
    fun bindLoginCaptchaFragment(fragment: LoginCaptchaFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginTermsFragment::class)
    fun bindLoginTermsFragment(fragment: LoginTermsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginServerUrlFormFragment::class)
    fun bindLoginServerUrlFormFragment(fragment: LoginServerUrlFormFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginResetPasswordMailConfirmationFragment::class)
    fun bindLoginResetPasswordMailConfirmationFragment(fragment: LoginResetPasswordMailConfirmationFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginResetPasswordFragment::class)
    fun bindLoginResetPasswordFragment(fragment: LoginResetPasswordFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginResetPasswordSuccessFragment::class)
    fun bindLoginResetPasswordSuccessFragment(fragment: LoginResetPasswordSuccessFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginServerSelectionFragment::class)
    fun bindLoginServerSelectionFragment(fragment: LoginServerSelectionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginSignUpSignInSelectionFragment::class)
    fun bindLoginSignUpSignInSelectionFragment(fragment: LoginSignUpSignInSelectionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginSplashFragment::class)
    fun bindLoginSplashFragment(fragment: LoginSplashFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginWebFragment::class)
    fun bindLoginWebFragment(fragment: LoginWebFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginGenericTextInputFormFragment::class)
    fun bindLoginGenericTextInputFormFragment(fragment: LoginGenericTextInputFormFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(LoginWaitForEmailFragment::class)
    fun bindLoginWaitForEmailFragment(fragment: LoginWaitForEmailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(UserListFragment::class)
    fun bindUserListFragment(fragment: UserListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PushGatewaysFragment::class)
    fun bindPushGatewaysFragment(fragment: PushGatewaysFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsNotificationsTroubleshootFragment::class)
    fun bindVectorSettingsNotificationsTroubleshootFragment(fragment: VectorSettingsNotificationsTroubleshootFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsAdvancedNotificationPreferenceFragment::class)
    fun bindVectorSettingsAdvancedNotificationPreferenceFragment(fragment: VectorSettingsAdvancedNotificationPreferenceFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsNotificationPreferenceFragment::class)
    fun bindVectorSettingsNotificationPreferenceFragment(fragment: VectorSettingsNotificationPreferenceFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsLabsFragment::class)
    fun bindVectorSettingsLabsFragment(fragment: VectorSettingsLabsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(HomeserverSettingsFragment::class)
    fun bindHomeserverSettingsFragment(fragment: HomeserverSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsPinFragment::class)
    fun bindVectorSettingsPinFragment(fragment: VectorSettingsPinFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsGeneralFragment::class)
    fun bindVectorSettingsGeneralFragment(fragment: VectorSettingsGeneralFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CyverseSettingsProfileFragment::class)
    fun bindCyverseSettingsProfileFragment(fragment: CyverseSettingsProfileFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CyverseAddRoleFragment::class)
    fun bindVectorCyverseAddRoleFragment(fragment: CyverseAddRoleFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PushRulesFragment::class)
    fun bindPushRulesFragment(fragment: PushRulesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsPreferencesFragment::class)
    fun bindVectorSettingsPreferencesFragment(fragment: VectorSettingsPreferencesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsSecurityPrivacyFragment::class)
    fun bindVectorSettingsSecurityPrivacyFragment(fragment: VectorSettingsSecurityPrivacyFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsHelpAboutFragment::class)
    fun bindVectorSettingsHelpAboutFragment(fragment: VectorSettingsHelpAboutFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsIgnoredUsersFragment::class)
    fun bindVectorSettingsIgnoredUsersFragment(fragment: VectorSettingsIgnoredUsersFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VectorSettingsDevicesFragment::class)
    fun bindVectorSettingsDevicesFragment(fragment: VectorSettingsDevicesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ThreePidsSettingsFragment::class)
    fun bindThreePidsSettingsFragment(fragment: ThreePidsSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PublicRoomsFragment::class)
    fun bindPublicRoomsFragment(fragment: PublicRoomsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomProfileFragment::class)
    fun bindRoomProfileFragment(fragment: RoomProfileFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomMemberListFragment::class)
    fun bindRoomMemberListFragment(fragment: RoomMemberListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomUploadsFragment::class)
    fun bindRoomUploadsFragment(fragment: RoomUploadsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomUploadsMediaFragment::class)
    fun bindRoomUploadsMediaFragment(fragment: RoomUploadsMediaFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomUploadsFilesFragment::class)
    fun bindRoomUploadsFilesFragment(fragment: RoomUploadsFilesFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomSettingsFragment::class)
    fun bindRoomSettingsFragment(fragment: RoomSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomAliasFragment::class)
    fun bindRoomAliasFragment(fragment: RoomAliasFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomPermissionsFragment::class)
    fun bindRoomPermissionsFragment(fragment: RoomPermissionsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomMemberProfileFragment::class)
    fun bindRoomMemberProfileFragment(fragment: RoomMemberProfileFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BreadcrumbsFragment::class)
    fun bindBreadcrumbsFragment(fragment: BreadcrumbsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(EmojiChooserFragment::class)
    fun bindEmojiChooserFragment(fragment: EmojiChooserFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SoftLogoutFragment::class)
    fun bindSoftLogoutFragment(fragment: SoftLogoutFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationRequestFragment::class)
    fun bindVerificationRequestFragment(fragment: VerificationRequestFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationChooseMethodFragment::class)
    fun bindVerificationChooseMethodFragment(fragment: VerificationChooseMethodFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationEmojiCodeFragment::class)
    fun bindVerificationEmojiCodeFragment(fragment: VerificationEmojiCodeFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationQrScannedByOtherFragment::class)
    fun bindVerificationQrScannedByOtherFragment(fragment: VerificationQrScannedByOtherFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationQRWaitingFragment::class)
    fun bindVerificationQRWaitingFragment(fragment: VerificationQRWaitingFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationConclusionFragment::class)
    fun bindVerificationConclusionFragment(fragment: VerificationConclusionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationCancelFragment::class)
    fun bindVerificationCancelFragment(fragment: VerificationCancelFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(QuadSLoadingFragment::class)
    fun bindQuadSLoadingFragment(fragment: QuadSLoadingFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(VerificationNotMeFragment::class)
    fun bindVerificationNotMeFragment(fragment: VerificationNotMeFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(QrCodeScannerFragment::class)
    fun bindQrCodeScannerFragment(fragment: QrCodeScannerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceListFragment::class)
    fun bindDeviceListFragment(fragment: DeviceListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeviceTrustInfoActionFragment::class)
    fun bindDeviceTrustInfoActionFragment(fragment: DeviceTrustInfoActionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(CrossSigningSettingsFragment::class)
    fun bindCrossSigningSettingsFragment(fragment: CrossSigningSettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(AttachmentsPreviewFragment::class)
    fun bindAttachmentsPreviewFragment(fragment: AttachmentsPreviewFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(IncomingShareFragment::class)
    fun bindIncomingShareFragment(fragment: IncomingShareFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(AccountDataFragment::class)
    fun bindAccountDataFragment(fragment: AccountDataFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(OutgoingKeyRequestListFragment::class)
    fun bindOutgoingKeyRequestListFragment(fragment: OutgoingKeyRequestListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(IncomingKeyRequestListFragment::class)
    fun bindIncomingKeyRequestListFragment(fragment: IncomingKeyRequestListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(KeyRequestsFragment::class)
    fun bindKeyRequestsFragment(fragment: KeyRequestsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(GossipingEventsPaperTrailFragment::class)
    fun bindGossipingEventsPaperTrailFragment(fragment: GossipingEventsPaperTrailFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapEnterPassphraseFragment::class)
    fun bindBootstrapEnterPassphraseFragment(fragment: BootstrapEnterPassphraseFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapConfirmPassphraseFragment::class)
    fun bindBootstrapConfirmPassphraseFragment(fragment: BootstrapConfirmPassphraseFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapWaitingFragment::class)
    fun bindBootstrapWaitingFragment(fragment: BootstrapWaitingFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapSetupRecoveryKeyFragment::class)
    fun bindBootstrapSetupRecoveryKeyFragment(fragment: BootstrapSetupRecoveryKeyFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapSaveRecoveryKeyFragment::class)
    fun bindBootstrapSaveRecoveryKeyFragment(fragment: BootstrapSaveRecoveryKeyFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapConclusionFragment::class)
    fun bindBootstrapConclusionFragment(fragment: BootstrapConclusionFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapReAuthFragment::class)
    fun bindBootstrapReAuthFragment(fragment: BootstrapReAuthFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(BootstrapMigrateBackupFragment::class)
    fun bindBootstrapMigrateBackupFragment(fragment: BootstrapMigrateBackupFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DeactivateAccountFragment::class)
    fun bindDeactivateAccountFragment(fragment: DeactivateAccountFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SharedSecuredStoragePassphraseFragment::class)
    fun bindSharedSecuredStoragePassphraseFragment(fragment: SharedSecuredStoragePassphraseFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SharedSecuredStorageKeyFragment::class)
    fun bindSharedSecuredStorageKeyFragment(fragment: SharedSecuredStorageKeyFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SharedSecuredStorageResetAllFragment::class)
    fun bindSharedSecuredStorageResetAllFragment(fragment: SharedSecuredStorageResetAllFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SetIdentityServerFragment::class)
    fun bindSetIdentityServerFragment(fragment: SetIdentityServerFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DiscoverySettingsFragment::class)
    fun bindDiscoverySettingsFragment(fragment: DiscoverySettingsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ReviewTermsFragment::class)
    fun bindReviewTermsFragment(fragment: ReviewTermsFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(WidgetFragment::class)
    fun bindWidgetFragment(fragment: WidgetFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ContactsBookFragment::class)
    fun bindPhoneBookFragment(fragment: ContactsBookFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(PinFragment::class)
    fun bindPinFragment(fragment: PinFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomBannedMemberListFragment::class)
    fun bindRoomBannedMemberListFragment(fragment: RoomBannedMemberListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SearchFragment::class)
    fun bindSearchFragment(fragment: SearchFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(ShowUserCodeFragment::class)
    fun bindShowUserCodeFragment(fragment: ShowUserCodeFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDevToolFragment::class)
    fun bindRoomDevToolFragment(fragment: RoomDevToolFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDevToolStateEventListFragment::class)
    fun bindRoomDevToolStateEventListFragment(fragment: RoomDevToolStateEventListFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDevToolEditFragment::class)
    fun bindRoomDevToolEditFragment(fragment: RoomDevToolEditFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(RoomDevToolSendFormFragment::class)
    fun bindRoomDevToolSendFormFragment(fragment: RoomDevToolSendFormFragment): Fragment
}
