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

package com.cioinfotech.cychat.features.share

import androidx.appcompat.widget.Toolbar
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.addFragment
import com.cioinfotech.cychat.core.platform.ToolbarConfigurable
import com.cioinfotech.cychat.core.platform.VectorBaseActivity
import com.cioinfotech.cychat.databinding.ActivitySimpleBinding

class IncomingShareActivity : VectorBaseActivity<ActivitySimpleBinding>(), ToolbarConfigurable {

    override fun getBinding() = ActivitySimpleBinding.inflate(layoutInflater)

    override fun getCoordinatorLayout() = views.coordinatorLayout

    override fun initUiAndData() {
        if (isFirstCreation()) {
            addFragment(R.id.simpleFragmentContainer, IncomingShareFragment::class.java)
        }
    }

    override fun configure(toolbar: Toolbar) {
        configureToolbar(toolbar, displayBack = false)
    }
}
