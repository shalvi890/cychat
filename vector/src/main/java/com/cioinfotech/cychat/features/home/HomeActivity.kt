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
import android.content.IntentSender.SendIntentException
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
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
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageAction
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageActivity
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageViewEvent
import com.cioinfotech.cychat.features.crypto.quads.SharedSecureStorageViewModel
import com.cioinfotech.cychat.features.crypto.recover.BootstrapActions
import com.cioinfotech.cychat.features.crypto.recover.BootstrapSharedViewModel
import com.cioinfotech.cychat.features.crypto.recover.BootstrapViewEvents
import com.cioinfotech.cychat.features.crypto.verification.VerificationAction
import com.cioinfotech.cychat.features.cycore.AES
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.home.room.RoleAddAlertDialogFragment
import com.cioinfotech.cychat.features.matrixto.MatrixToBottomSheet
import com.cioinfotech.cychat.features.notifications.NotificationDrawerManager
import com.cioinfotech.cychat.features.permalink.NavigationInterceptor
import com.cioinfotech.cychat.features.permalink.PermalinkHandler
import com.cioinfotech.cychat.features.popup.DefaultVectorAlert
import com.cioinfotech.cychat.features.popup.PopupAlertManager
import com.cioinfotech.cychat.features.settings.VectorPreferences
import com.cioinfotech.cychat.features.settings.VectorSettingsActivity
import com.cioinfotech.cychat.features.themes.ThemeUtils
import com.cioinfotech.cychat.features.workers.signout.ServerBackupStatusViewModel
import com.cioinfotech.cychat.features.workers.signout.ServerBackupStatusViewState
import com.cioinfotech.cychat.push.fcm.FcmHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
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
import org.matrix.android.sdk.internal.network.NetworkConstants.FULL_NAME
import org.matrix.android.sdk.internal.network.NetworkConstants.MOBILE
import org.matrix.android.sdk.internal.network.NetworkConstants.SECRET_KEY
import org.matrix.android.sdk.internal.network.NetworkConstants.SESSION_UPDATED
import org.matrix.android.sdk.internal.network.NetworkConstants.SIGNING_MODE
import org.matrix.android.sdk.internal.network.NetworkConstants.USER_ID
import org.matrix.android.sdk.internal.network.NetworkConstants.U_TYPE_MODE
import org.matrix.android.sdk.internal.network.NetworkConstants.U_TYPE_MODE_INDIVIDUAL
import timber.log.Timber
import javax.inject.Inject

private const val FLEXIBLE_APP_UPDATE_REQ_CODE = 123
private const val IMMEDIATE_APP_UPDATE_REQ_CODE = 124

@Parcelize
data class HomeActivityArgs(
        val clearNotification: Boolean,
        val accountCreation: Boolean,
        val inviteNotificationRoomId: String? = null,
        val keyId: String? = null,
        val requestedSecrets: List<String> = listOf(MASTER_KEY_SSSS_NAME, USER_SIGNING_KEY_SSSS_NAME, SELF_SIGNING_KEY_SSSS_NAME, KEYBACKUP_SECRET_SSSS_NAME),
        val resultKeyStoreAlias: String = SharedSecureStorageActivity.DEFAULT_RESULT_KEYSTORE_ALIAS
) : Parcelable

class HomeActivity :
        VectorBaseActivity<ActivityHomeBinding>(),
        ToolbarConfigurable,
        UnknownDeviceDetectorSharedViewModel.Factory,
        ServerBackupStatusViewModel.Factory,
        NavigationInterceptor, RoleAddAlertDialogFragment.ItemClickListener {

    private lateinit var sharedActionViewModel: HomeSharedActionViewModel

    /** Code Added For Our API Calls & Injected Backup factory for auto backup feature*/
    private lateinit var cyChatViewModel: CyCoreViewModel
    private val homeActivityViewModel: HomeActivityViewModel by viewModel()
    @Inject lateinit var viewModelFactory: HomeActivityViewModel.Factory
    @Inject lateinit var serverBackupviewModelFactory: ServerBackupStatusViewModel.Factory

    private val viewModel: SharedSecureStorageViewModel by viewModel()
    @Inject lateinit var sharedViewModelFactory: SharedSecureStorageViewModel.Factory

    /** Code Added To Recover Secure Backup Automatically*/
    @Inject
    lateinit var bootstrapViewModelFactory: BootstrapSharedViewModel.Factory
    private val bootStrapViewModel by viewModel(BootstrapSharedViewModel::class)

    @Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var pushManager: PushersManager
    @Inject lateinit var notificationDrawerManager: NotificationDrawerManager
    @Inject lateinit var vectorPreferences: VectorPreferences
    @Inject lateinit var popupAlertManager: PopupAlertManager
    @Inject lateinit var shortcutsHandler: ShortcutsHandler
    @Inject lateinit var unknownDeviceViewModelFactory: UnknownDeviceDetectorSharedViewModel.Factory
    @Inject lateinit var permalinkHandler: PermalinkHandler
    @Inject lateinit var avatarRenderer: AvatarRenderer
    @Inject lateinit var initSyncStepFormatter: InitSyncStepFormatter
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var installStateUpdatedListener: InstallStateUpdatedListener

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

    private fun checkUpdate() {
        if (!::appUpdateManager.isInitialized)
            appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))
                startUpdateFlow(appUpdateInfo)
            else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
                startImmediateUpdateFlow(appUpdateInfo)
            else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                startUpdateFlow(appUpdateInfo)
            else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED)
                popupSnackBarForCompleteUpdate()
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, FLEXIBLE_APP_UPDATE_REQ_CODE)
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    private fun startImmediateUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, IMMEDIATE_APP_UPDATE_REQ_CODE)
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FLEXIBLE_APP_UPDATE_REQ_CODE || requestCode == IMMEDIATE_APP_UPDATE_REQ_CODE) {
            when (resultCode) {
                RESULT_CANCELED -> Unit
                RESULT_OK       -> Unit
                else            -> checkUpdate()
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(views.root, "New app is ready!", Snackbar.LENGTH_INDEFINITE).let {
            it.setAction("Install") {
                appUpdateManager.completeUpdate()
            }
            it.setActionTextColor(ContextCompat.getColor(this, R.color.primary_color_light))
            it.show()
        }
    }

    private fun removeInstallStateUpdateListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkUpdate()
        installStateUpdatedListener = InstallStateUpdatedListener { state: InstallState ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADED -> popupSnackBarForCompleteUpdate()
                InstallStatus.INSTALLED  -> removeInstallStateUpdateListener()
                else                     -> Unit
            }
        }
        appUpdateManager.registerListener(installStateUpdatedListener)
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

    /** This Function has all Cychat functions which we have to call on following events:
     * -session startup
     * -register
     * -default server URL handler
     **/
    private fun onFirstSession() {
        val pref = DefaultSharedPreferences.getInstance(applicationContext)
        if (!::cyChatViewModel.isInitialized) {
            cyChatViewModel = viewModelProvider.get(CyCoreViewModel::class.java)
            cyChatViewModel.handleCyGetDetails()
        }
        Log.e("@@ session",pref.getBoolean(SESSION_UPDATED, false).toString())
        Log.e("@@ SIGNING_MODE",pref.getBoolean(SIGNING_MODE, false).toString())
        Log.e("@@ U_TYPE_MODE",pref.getString(U_TYPE_MODE, null).toString())
        if (!pref.getBoolean(SESSION_UPDATED, false)) {
            GlobalScope.launch(Dispatchers.IO) {
                activeSessionHolder.getActiveSession().sessionParams.deviceId?.let {
                    Log.e("@@","@@ getActiveSession ")
                    if (!::cyChatViewModel.isInitialized)
                        cyChatViewModel = viewModelProvider.get(CyCoreViewModel::class.java)
                    cyChatViewModel.handleDeleteOldSessions(it)
                }
            }
        }
        if (pref.getBoolean(SIGNING_MODE, false)) {
            if (pref.getString(U_TYPE_MODE, null) == U_TYPE_MODE_INDIVIDUAL)
                RoleAddAlertDialogFragment(this).show(supportFragmentManager, null)

            pref.getString(FULL_NAME, null)?.let { name ->
                lifecycleScope.launch {
                    val session = activeSessionHolder.getActiveSession()
                    session.setDisplayName(session.myUserId, name)
                    pref.edit().apply {
                        putString(FULL_NAME, null)
                        apply()
                    }
                }
            }
            if (!::cyChatViewModel.isInitialized)
                cyChatViewModel = viewModelProvider.get(CyCoreViewModel::class.java)
            val job = GlobalScope.launch(Dispatchers.IO) {
                while (pref.getBoolean(SIGNING_MODE, false)) {
                    delay(2000)
                    bootStrapViewModel.handle(BootstrapActions.Start(userWantsToEnterPassphrase = false))
                }
            }

            bootStrapViewModel.observeViewEvents { event ->
                if (event is BootstrapViewEvents.SyncWithServer) {
                    val key = AES.encrypt(
                            event.key.recoveryKey,
                            AES.createSecretKey(
                                    pref.getString(USER_ID, "")!!,
                                    pref.getString(MOBILE, "")!!
                            )
                    )

                    cyChatViewModel.handleUpdateRecoveryToken(key)
                    pref.edit().apply {
                        putBoolean(SIGNING_MODE, false)
                        apply()
                    }
                    job.cancel()
                }
            }
        } else {
            if (!::cyChatViewModel.isInitialized) {
                cyChatViewModel = viewModelProvider.get(CyCoreViewModel::class.java)
                cyChatViewModel.handleGetDefaultURLs()
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

    /** Code Commented As we have removed cross signing as only supporting single login*/
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
        val pref = DefaultSharedPreferences.getInstance(this)
        pref.getString(SECRET_KEY, null)?.let {
            val key = AES.decrypt(
                    it,
                    AES.createSecretKey(
                            pref.getString(USER_ID, "")!!,
                            pref.getString(MOBILE, "")!!
                    )
            )
            viewModel.handle(SharedSecureStorageAction.SubmitKey(key))
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

    /** Code Commented As no security issue can happen as User is verified on first login itself*/
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

//    override fun getMenuRes() = R.menu.home

//    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
//        menu.findItem(R.id.menu_home_init_sync_legacy)?.isVisible = vectorPreferences.developerMode()
//        menu.findItem(R.id.menu_home_init_sync_optimized)?.isVisible = vectorPreferences.developerMode()
//        return super.onPrepareOptionsMenu(menu)
//    }

    /** Removed Few Options As per requirements*/
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
//            R.id.menu_home_init_sync_legacy    -> {
//                // Configure the SDK
//                initialSyncStrategy = InitialSyncStrategy.Legacy
//                // And clear cache
//                MainActivity.restartApp(this, MainActivityArgs(clearCache = true))
//                return true
//            }
//            R.id.menu_home_init_sync_optimized -> {
//                // Configure the SDK
//                initialSyncStrategy = InitialSyncStrategy.Optimized()
//                // And clear cache
//                MainActivity.restartApp(this, MainActivityArgs(clearCache = true))
//                return true
//            }
            R.id.menu_home_filter  -> {
                navigator.openRoomsFiltering(this)
                return true
            }
            R.id.menu_home_setting -> {
                navigator.openSettings(this)
                return true
            }
            R.id.menu_plugins -> {
                navigator.openPlugins(this)
                return true
            }
//            R.id.menu_invite_by_qr -> {
//                UserCodeActivity.newIntent(this, sharedActionViewModel.session.myUserId).let {
//                    val options =
//                            ActivityOptionsCompat.makeSceneTransitionAnimation(
//                                    this,
//                                    views.root,
//                                    ViewCompat.getTransitionName(views.root) ?: ""
//                            )
//                    startActivity(it, options.toBundle())
//                }
//                return true
//            }
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

//    private fun isNetworkConnected(context: Context): Boolean {
//        val result: Boolean
//        val connectivityManager =
//                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkCapabilities = connectivityManager.activeNetwork ?: return false
//        val actNw =
//                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
//        result = when {
//            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> true
//            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
//            else                                                       -> false
//        }
//        return result
//    }

    companion object {
        fun newIntent(context: Context, clearNotification: Boolean = false,
                      accountCreation: Boolean = false,
                      inviteNotificationRoomId: String? = null
        ): Intent {
            val args = HomeActivityArgs(
                    clearNotification = clearNotification,
                    accountCreation = accountCreation,
                    inviteNotificationRoomId = inviteNotificationRoomId
            )

            return Intent(context, HomeActivity::class.java)
                    .apply {
                        putExtra(MvRx.KEY_ARG, args)
                    }
        }

        private const val MATRIX_TO_CUSTOM_SCHEME_URL_BASE = "element://"
        private const val ROOM_LINK_PREFIX = "${MATRIX_TO_CUSTOM_SCHEME_URL_BASE}room/"
        private const val USER_LINK_PREFIX = "${MATRIX_TO_CUSTOM_SCHEME_URL_BASE}user/"
        var isOneToOneChatOpen = true
    }

    override fun onStop() {
        super.onStop()
        removeInstallStateUpdateListener()
    }

    override fun onItemClicked() { // To show Add Role Dialog & Take to settings
        navigator.openSettings(this, VectorSettingsActivity.EXTRA_PROFILE)
    }
}
