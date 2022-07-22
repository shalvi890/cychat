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
import com.cioinfotech.cychat.databinding.FragmentPluginDocumentsBinding
import com.cioinfotech.cychat.features.plugins.PluginsActivity
import com.cioinfotech.cychat.features.plugins.adapter.PluginDocumentAdapter
import com.google.android.material.tabs.TabLayout

class PluginDocumentsFragment : VectorBaseFragment<FragmentPluginDocumentsBinding>(), PluginDocumentAdapter.ItemClickListener {
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentPluginDocumentsBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as PluginsActivity).setToolbarTitle(getString(R.string.my_documents))
        views.tabLayout.addTab(views.tabLayout.newTab().setText("Uploaded Documents"))
        views.tabLayout.addTab(views.tabLayout.newTab().setText("Downloaded Documents"))
        views.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        views.rvDocuments.adapter = PluginDocumentAdapter().apply {
            itemClickListener = this@PluginDocumentsFragment
        }
        views.btnNewDoc.setOnClickListener {
            addFragmentToBackstack(R.id.container, PluginAttachDocumentFragment::class.java, allowStateLoss = false)
        }
    }

    override fun onItemClicked() {
    }
}
