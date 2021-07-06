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

package com.cioinfotech.cychat.features.home

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.viewModel
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.di.ScreenComponent
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.extensions.hideKeyboard
import com.cioinfotech.cychat.core.extensions.replaceFragment
import com.cioinfotech.cychat.core.platform.ToolbarConfigurable
import com.cioinfotech.cychat.core.platform.VectorBaseActivity
import com.cioinfotech.cychat.core.pushers.PushersManager
import com.cioinfotech.cychat.databinding.ActivityHomeBinding
import com.cioinfotech.cychat.features.MainActivity
import com.cioinfotech.cychat.features.MainActivityArgs
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageAction
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageActivity
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageViewEvent
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageViewModel
import com.cioinfotech.cychat.features.crypto.recover.BootstrapActions
import com.cioinfotech.cychat.features.crypto.recover.BootstrapSharedViewModel
import com.cioinfotech.cychat.features.crypto.recover.BootstrapViewEvents
import com.cioinfotech.cychat.features.crypto.verification.VerificationAction
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.matrixto.MatrixToBottomSheet
import com.cioinfotech.cychat.features.notifications.NotificationDrawerManager
import com.cioinfotech.cychat.features.permalink.NavigationInterceptor
import com.cioinfotech.cychat.features.permalink.PermalinkHandler
import com.cioinfotech.cychat.features.popup.DefaultVectorAlert
import com.cioinfotech.cychat.features.popup.PopupAlertManager
import com.cioinfotech.cychat.features.rageshake.VectorUncaughtExceptionHandler
import com.cioinfotech.cychat.features.settings.VectorPreferences
import com.cioinfotech.cychat.features.settings.VectorSettingsActivity
import com.cioinfotech.cychat.features.themes.ThemeUtils
import com.cioinfotech.cychat.features.workers.signout.ServerBackupStatusViewModel
import com.cioinfotech.cychat.features.workers.signout.ServerBackupStatusViewState
import com.cioinfotech.cychat.push.fcm.FcmHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.matrix.android.sdk.api.session.crypto.crosssigning.KEYBACKUP_SECRET_SSSS_NAME
import org.matrix.android.sdk.api.session.crypto.crosssigning.MASTER_KEY_SSSS_NAME
import org.matrix.android.sdk.api.session.crypto.crosssigning.SELF_SIGNING_KEY_SSSS_NAME
import org.matrix.android.sdk.api.session.crypto.crosssigning.USER_SIGNING_KEY_SSSS_NAME
import org.matrix.android.sdk.api.session.initsync.InitialSyncProgressService
import org.matrix.android.sdk.api.session.permalinks.PermalinkService
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.SECRET_KEY
import org.matrix.android.sdk.internal.session.sync.InitialSyncStrategy
import org.matrix.android.sdk.internal.session.sync.initialSyncStrategy
import timber.log.Timber
import javax.inject.Inject

@Parcelize
data class HomeActivityArgs(
        val clearNotification: Boolean,
        val accountCreation: Boolean,
        val keyId: String? = null,
        val requestedSecrets: List<String> = listOf(MASTER_KEY_SSSS_NAME, USER_SIGNING_KEY_SSSS_NAME, SELF_SIGNING_KEY_SSSS_NAME, KEYBACKUP_SECRET_SSSS_NAME),
        val resultKeyStoreAlias: String = SharedSecureStorageActivity.DEFAULT_RESULT_KEYSTORE_ALIAS
) : Parcelable

class HomeActivity :
        VectorBaseActivity<ActivityHomeBinding>(),
        ToolbarConfigurable,
        UnknownDeviceDetectorSharedViewModel.Factory,
        ServerBackupStatusViewModel.Factory,
        NavigationInterceptor {

    private lateinit var sharedActionViewModel: HomeSharedActionViewModel
    private lateinit var cyChatViewModel: CyCoreViewModel

    private val homeActivityViewModel: HomeActivityViewModel by viewModel()

    @Inject lateinit var viewModelFactory: HomeActivityViewModel.Factory

    @Inject lateinit var serverBackupviewModelFactory: ServerBackupStatusViewModel.Factory

    private val viewModel: SharedSecureStorageViewModel by viewModel()
    @Inject lateinit var sharedViewModelFactory: SharedSecureStorageViewModel.Factory

//    private lateinit var keyViewModel: KeysBackupRestoreFromKeyViewModel
//    private lateinit var sharedViewModel: KeysBackupRestoreSharedViewModel

    @Inject
    lateinit var bootstrapViewModelFactory: BootstrapSharedViewModel.Factory
    private val bootStrapViewModel by viewModel(BootstrapSharedViewModel::class)

    @Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var vectorUncaughtExceptionHandler: VectorUncaughtExceptionHandler
    @Inject lateinit var pushManager: PushersManager
    @Inject lateinit var notificationDrawerManager: NotificationDrawerManager
    @Inject lateinit var vectorPreferences: VectorPreferences
    @Inject lateinit var popupAlertManager: PopupAlertManager
    @Inject lateinit var shortcutsHandler: ShortcutsHandler
    @Inject lateinit var unknownDeviceViewModelFactory: UnknownDeviceDetectorSharedViewModel.Factory
    @Inject lateinit var permalinkHandler: PermalinkHandler
    @Inject lateinit var avatarRenderer: AvatarRenderer
    @Inject lateinit var initSyncStepFormatter: InitSyncStepFormatter

    private val drawerListener = object : DrawerLayout.SimpleDrawerListener() {
        override fun onDrawerStateChanged(newState: Int) {
            hideKeyboard()
        }
    }

    override fun getBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun create(initialState: UnknownDevicesState): UnknownDeviceDetectorSharedViewModel {
        return unknownDeviceViewModelFactory.create(initialState)
    }

    override fun create(initialState: ServerBackupStatusViewState): ServerBackupStatusViewModel {
        return serverBackupviewModelFactory.create(initialState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FcmHelper.ensureFcmTokenIsRetrieved(this, pushManager, vectorPreferences.areNotificationEnabledForDevice())
        sharedActionViewModel = viewModelProvider.get(HomeSharedActionViewModel::class.java)

        if (isFirstCreation()) {
            replaceFragment(R.id.homeDetailFragmentContainer, LoadingFragment::class.java)
            replaceFragment(R.id.homeDrawerFragmentContainer, HomeDrawerFragment::class.java)
            handleIntent(intent)
        }

        sharedActionViewModel
                .observe()
                .subscribe { sharedAction ->
                    when (sharedAction) {
//                        is HomeActivitySharedAction.OpenDrawer  -> views.drawerLayout.openDrawer(GravityCompat.START)
//                        is HomeActivitySharedAction.CloseDrawer -> views.drawerLayout.closeDrawer(GravityCompat.START)
                        is HomeActivitySharedAction.OpenGroup -> {
                            views.drawerLayout.closeDrawer(GravityCompat.START)
                            replaceFragment(R.id.homeDetailFragmentContainer, HomeDetailFragment::class.java, allowStateLoss = true)
                        }
                        else                                  -> null
                    }.exhaustive
                }
                .disposeOnDestroy()

        val args = intent.getParcelableExtra<HomeActivityArgs>(MvRx.KEY_ARG)

        if (args?.clearNotification == true)
            notificationDrawerManager.clearAllEvents()

        homeActivityViewModel.observeViewEvents {
            when (it) {
                is HomeActivityViewEvents.AskPasswordToInitCrossSigning -> Unit //handleAskPasswordToInitCrossSigning(it)
                is HomeActivityViewEvents.OnNewSession                  -> handleOnNewSession()
                HomeActivityViewEvents.PromptToEnableSessionPush        -> handlePromptToEnablePush()
                is HomeActivityViewEvents.OnCrossSignedInvalidated      -> Unit //handleCrossSigningInvalidated(it)
            }.exhaustive
        }
        homeActivityViewModel.subscribe(this) { renderState(it) }

        shortcutsHandler.observeRoomsAndBuildShortcuts().disposeOnDestroy()

        viewModel.observeViewEvents { observeViewEvents(it) }

        onFirstSession()
    }

    private fun onFirstSession() {
        val pref = DefaultSharedPreferences.getInstance(applicationContext)
        if (pref.getBoolean(NetworkConstants.SIGNING_MODE, false)) {
            cyChatViewModel = viewModelProvider.get(CyCoreViewModel::class.java)
            val job = GlobalScope.launch(Dispatchers.IO) {
                while (pref.getBoolean(NetworkConstants.SIGNING_MODE, false)) {
                    delay(2000)
                    bootStrapViewModel.handle(BootstrapActions.Start(userWantsToEnterPassphrase = false))
                }
            }

            bootStrapViewModel.observeViewEvents { event ->
                if (event is BootstrapViewEvents.SyncWithServer) {
                    cyChatViewModel.handleUpdateRecoveryToken(event.key.recoveryKey)
                    pref.edit().apply {
                        putBoolean(NetworkConstants.SIGNING_MODE, false)
                        apply()
                    }
                    job.cancel()
                }
            }
        }
    }

    private fun observeViewEvents(it: SharedSecureStorageViewEvent?) {
        when (it) {
            is SharedSecureStorageViewEvent.FinishSuccess -> homeActivityViewModel.handleSecretBackFromSSSS(VerificationAction.GotResultFromSsss(it.cypherResult, SharedSecureStorageActivity.DEFAULT_RESULT_KEYSTORE_ALIAS))
            else                                          -> Unit
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.dataString?.let { deepLink ->
            val resolvedLink = when {
                deepLink.startsWith(PermalinkService.MATRIX_TO_URL_BASE) -> deepLink
                deepLink.startsWith(MATRIX_TO_CUSTOM_SCHEME_URL_BASE)    -> {
                    // This is a bit ugly, but for now just convert to matrix.to link for compatibility
                    when {
                        deepLink.startsWith(USER_LINK_PREFIX) -> deepLink.substring(USER_LINK_PREFIX.length)
                        deepLink.startsWith(ROOM_LINK_PREFIX) -> deepLink.substring(ROOM_LINK_PREFIX.length)
                        else                                  -> null
                    }?.let {
                        activeSessionHolder.getSafeActiveSession()?.permalinkService()?.createPermalink(it)
                    }
                }
                else                                                     -> return@let
            }

            permalinkHandler.launch(
                    context = this,
                    deepLink = resolvedLink,
                    navigationInterceptor = this,
                    buildTask = true
            )
                    // .delay(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { isHandled ->
                        if (!isHandled) {
                            AlertDialog.Builder(this)
                                    .setTitle(R.string.dialog_title_error)
                                    .setMessage(R.string.permalink_malformed)
                                    .setPositiveButton(R.string.ok, null)
                                    .show()
                        }
                    }
                    .disposeOnDestroy()
        }
    }

    private fun renderState(state: HomeActivityViewState) {
        when (val status = state.initialSyncProgressServiceStatus) {
            is InitialSyncProgressService.Status.Idle        -> {
                views.waitingView.root.isVisible = false
            }
            is InitialSyncProgressService.Status.Progressing -> {
                val initSyncStepStr = initSyncStepFormatter.format(status.initSyncStep)
                Timber.v("$initSyncStepStr ${status.percentProgress}")
                views.waitingView.root.setOnClickListener {
                    // block interactions
                }
                views.waitingView.waitingHorizontalProgress.apply {
                    isIndeterminate = false
                    max = 100
                    progress = status.percentProgress
                    isVisible = true
                }
                views.waitingView.waitingStatusText.apply {
                    text = initSyncStepStr
                    isVisible = true
                }
                views.waitingView.root.isVisible = true
            }
        }.exhaustive
    }

//    private fun handleAskPasswordToInitCrossSigning(events: HomeActivityViewEvents.AskPasswordToInitCrossSigning) {
//        // We need to ask
//        promptSecurityEvent(
//                events.userItem,
//                R.string.upgrade_security,
//                R.string.security_prompt_text
//        ) {
//            it.navigator.upgradeSessionSecurity(it, true)
//        }
//    }

//    private fun handleCrossSigningInvalidated(event: HomeActivityViewEvents.OnCrossSignedInvalidated) {
//        // We need to ask
//        promptSecurityEvent(
//                event.userItem,
//                R.string.crosssigning_verify_this_session,
//                R.string.confirm_your_identity
//        ) {
//            it.navigator.waitSessionVerification(it)
//        }
//    }

    private fun handleOnNewSession() {//event: HomeActivityViewEvents.OnNewSession
        DefaultSharedPreferences.getInstance(this).getString(SECRET_KEY, null)?.let {
            viewModel.handle(SharedSecureStorageAction.SubmitKey(it))
        }
//        promptSecurityEvent(
//                event.userItem,
//                R.string.crosssigning_verify_this_session,
//                R.string.confirm_your_identity
//        ) {
//            if (event.waitForIncomingRequest) {
//                it.navigator.waitSessionVerification(it)
//            } else {
//                it.navigator.requestSelfSessionVerification(it)
//            }
//        }
    }

    private fun handlePromptToEnablePush() {
        popupAlertManager.postVectorAlert(
                DefaultVectorAlert(
                        uid = "enablePush",
                        title = getString(R.string.alert_push_are_disabled_title),
                        description = getString(R.string.alert_push_are_disabled_description),
                        iconId = R.drawable.ic_room_actions_notifications_mutes,
                        shouldBeDisplayedIn = {
                            it is HomeActivity
                        }
                ).apply {
                    colorInt = ThemeUtils.getColor(this@HomeActivity, R.attr.vctr_notice_secondary)
                    contentAction = Runnable {
                        (weakCurrentActivity?.get() as? VectorBaseActivity<*>)?.let {
                            // action(it)
                            homeActivityViewModel.handle(HomeActivityViewActions.PushPromptHasBeenReviewed)
                            it.navigator.openSettings(it, VectorSettingsActivity.EXTRA_DIRECT_ACCESS_NOTIFICATIONS)
                        }
                    }
                    dismissedAction = Runnable {
                        homeActivityViewModel.handle(HomeActivityViewActions.PushPromptHasBeenReviewed)
                    }
                    addButton(getString(R.string.dismiss), {
                        homeActivityViewModel.handle(HomeActivityViewActions.PushPromptHasBeenReviewed)
                    }, true)
                    addButton(getString(R.string.settings), {
                        (weakCurrentActivity?.get() as? VectorBaseActivity<*>)?.let {
                            // action(it)
                            homeActivityViewModel.handle(HomeActivityViewActions.PushPromptHasBeenReviewed)
                            it.navigator.openSettings(it, VectorSettingsActivity.EXTRA_DIRECT_ACCESS_NOTIFICATIONS)
                        }
                    }, true)
                }
        )
    }

//    private fun promptSecurityEvent(userItem: MatrixItem.UserItem?, titleRes: Int, descRes: Int, action: ((VectorBaseActivity<*>) -> Unit)) {
//        popupAlertManager.postVectorAlert(
//                VerificationVectorAlert(
//                        uid = "upgradeSecurity",
//                        title = getString(titleRes),
//                        description = getString(descRes),
//                        iconId = R.drawable.ic_shield_warning
//                ).apply {
//                    viewBinder = VerificationVectorAlert.ViewBinder(userItem, avatarRenderer)
//                    colorInt = ContextCompat.getColor(this@HomeActivity, R.color.riotx_positive_accent)
//                    contentAction = Runnable {
//                        (weakCurrentActivity?.get() as? VectorBaseActivity<*>)?.let {
//                            action(it)
//                        }
//                    }
//                    dismissedAction = Runnable {}
//                }
//        )
//    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getParcelableExtra<HomeActivityArgs>(MvRx.KEY_ARG)?.clearNotification == true) {
            notificationDrawerManager.clearAllEvents()
        }
        handleIntent(intent)
    }

    override fun onDestroy() {
        views.drawerLayout.removeDrawerListener(drawerListener)
        super.onDestroy()
    }

//    override fun onResume() {
//        super.onResume()

//        if (vectorUncaughtExceptionHandler.didAppCrash(this)) {
//            vectorUncaughtExceptionHandler.clearAppCrashStatus(this)
//
//            AlertDialog.Builder(this)
//                    .setMessage(R.string.send_bug_report_app_crashed)
//                    .setCancelable(false)
//                    .setPositiveButton(R.string.yes) { _, _ -> bugReporter.openBugReportScreen(this) }
//                    .setNegativeButton(R.string.no) { _, _ -> bugReporter.deleteCrashFile(this) }
//                    .show()
//        } else {
//            showDisclaimerDialog(this)
//        }

    // Force remote backup state update to update the banner if needed
//        serverBackupStatusViewModel.refreshRemoteStateIfNeeded()
//    }

    override fun configure(toolbar: Toolbar) {
        configureToolbar(toolbar, false)
    }

    override fun getMenuRes() = R.menu.home

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_home_init_sync_legacy)?.isVisible = vectorPreferences.developerMode()
        menu.findItem(R.id.menu_home_init_sync_optimized)?.isVisible = vectorPreferences.developerMode()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.menu_home_suggestion          -> {
//                bugReporter.openBugReportScreen(this, true)
//                return true
//            }
//            R.id.menu_home_report_bug          -> {
//                bugReporter.openBugReportScreen(this, false)
//                return true
//            }
            R.id.menu_home_init_sync_legacy    -> {
                // Configure the SDK
                initialSyncStrategy = InitialSyncStrategy.Legacy
                // And clear cache
                MainActivity.restartApp(this, MainActivityArgs(clearCache = true))
                return true
            }
            R.id.menu_home_init_sync_optimized -> {
                // Configure the SDK
                initialSyncStrategy = InitialSyncStrategy.Optimized()
                // And clear cache
                MainActivity.restartApp(this, MainActivityArgs(clearCache = true))
                return true
            }
            R.id.menu_home_filter              -> {
                navigator.openRoomsFiltering(this)
                return true
            }
            R.id.menu_home_setting             -> {
                navigator.openSettings(this)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (views.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            views.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun navToMemberProfile(userId: String, deepLink: Uri): Boolean {
        val listener = object : MatrixToBottomSheet.InteractionListener {
            override fun navigateToRoom(roomId: String) {
                navigator.openRoom(this@HomeActivity, roomId)
            }
        }
        // TODO check if there is already one??
        MatrixToBottomSheet.withLink(deepLink.toString(), listener)
                .show(supportFragmentManager, "HA#MatrixToBottomSheet")
        return true
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val result: Boolean
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else                                                       -> false
        }
        return result
    }

    companion object {
        fun newIntent(context: Context, clearNotification: Boolean = false, accountCreation: Boolean = false): Intent {
            val args = HomeActivityArgs(
                    clearNotification = clearNotification,
                    accountCreation = accountCreation
            )

            return Intent(context, HomeActivity::class.java)
                    .apply {
                        putExtra(MvRx.KEY_ARG, args)
                    }
        }

        private const val MATRIX_TO_CUSTOM_SCHEME_URL_BASE = "element://"
        private const val ROOM_LINK_PREFIX = "${MATRIX_TO_CUSTOM_SCHEME_URL_BASE}room/"
        private const val USER_LINK_PREFIX = "${MATRIX_TO_CUSTOM_SCHEME_URL_BASE}user/"
        var isOneToOneChatOpen = false
    }
}
