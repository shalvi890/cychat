/*
 * Copyright (c) 2022 New Vector Ltd
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

package com.cioinfotech.cychat.features.plugins

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ScreenComponent
import com.cioinfotech.cychat.core.extensions.replaceFragment
import com.cioinfotech.cychat.core.platform.SimpleFragmentActivity
import com.cioinfotech.cychat.features.plugins.ui.PluginListFragment

class PluginsActivity : SimpleFragmentActivity(), FragmentManager.OnBackStackChangedListener {

    companion object {
        fun getIntent(context: Context) = Intent(context, PluginsActivity::class.java)
    }

    override fun injectWith(injector: ScreenComponent) {
        super.injectWith(injector)
        injector.inject(this)
    }

    override fun initUiAndData() {
        super.initUiAndData()
        if (isFirstCreation())
            replaceFragment(R.id.container, PluginListFragment::class.java)
        supportFragmentManager.addOnBackStackChangedListener(this)
    }

    override fun onBackStackChanged() {
    }

    fun setToolbarTitle(title: String) {
        views.toolbar.title = title
    }

    override fun onDestroy() {
        supportFragmentManager.removeOnBackStackChangedListener(this)
        super.onDestroy()
    }
}
