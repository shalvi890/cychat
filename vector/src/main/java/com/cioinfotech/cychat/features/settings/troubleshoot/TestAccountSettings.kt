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
package com.cioinfotech.cychat.features.settings.troubleshoot

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ActiveSessionHolder
import com.cioinfotech.cychat.core.resources.StringProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.pushrules.RuleIds
import org.matrix.android.sdk.api.pushrules.RuleKind
import javax.inject.Inject

/**
 * Check that the main pushRule (RULE_ID_DISABLE_ALL) is correctly setup
 */
class TestAccountSettings @Inject constructor(private val stringProvider: StringProvider,
                                              private val activeSessionHolder: ActiveSessionHolder)
    : TroubleshootTest(R.string.settings_troubleshoot_test_account_settings_title) {

    override fun perform(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val session = activeSessionHolder.getSafeActiveSession() ?: return
        val defaultRule = session.getPushRules().getAllRules()
                .find { it.ruleId == RuleIds.RULE_ID_DISABLE_ALL }

        if (defaultRule != null) {
            if (!defaultRule.enabled) {
                description = stringProvider.getString(R.string.settings_troubleshoot_test_account_settings_success)
                quickFix = null
                status = TestStatus.SUCCESS
            } else {
                description = stringProvider.getString(R.string.settings_troubleshoot_test_account_settings_failed)
                quickFix = object : TroubleshootQuickFix(R.string.settings_troubleshoot_test_account_settings_quickfix) {
                    override fun doFix() {
                        if (manager?.diagStatus == TestStatus.RUNNING) return // wait before all is finished

                        GlobalScope.launch {
                            runCatching {
                                session.updatePushRuleEnableStatus(RuleKind.OVERRIDE, defaultRule, !defaultRule.enabled)
                            }
                            manager?.retry(activityResultLauncher)
                        }
                    }
                }
                status = TestStatus.FAILED
            }
        } else {
            // should not happen?
            status = TestStatus.FAILED
        }
    }
}
