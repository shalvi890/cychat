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

package com.cioinfotech.cychat.features.plugins.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.addFragmentToBackstack
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentPluginListBinding
import com.cioinfotech.cychat.features.plugins.PluginsActivity
import com.cioinfotech.cychat.features.plugins.adapter.PluginsAdapter

class PluginListFragment : VectorBaseFragment<FragmentPluginListBinding>(), PluginsAdapter.ItemClickListener {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentPluginListBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as PluginsActivity).setToolbarTitle("Plugins")
        views.rvPlugins.adapter = PluginsAdapter().apply {
            setData(mutableListOf("Plugin 1", "Plugin 2", "Plugin 3", "Plugin 4"))
            itemClickListener = this@PluginListFragment
        }
    }

    override fun onItemClicked(model: String) {
        addFragmentToBackstack(R.id.container, PluginsDetailFragment::class.java, allowStateLoss = false)
    }
}
