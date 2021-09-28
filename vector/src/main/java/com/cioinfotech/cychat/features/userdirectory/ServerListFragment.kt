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

package com.cioinfotech.cychat.features.userdirectory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.cioinfotech.cychat.core.platform.VectorBaseDialogFragment
import com.cioinfotech.cychat.databinding.FragmentServerListBinding
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.userdirectory.adapter.ServerListAdapter
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomain

class ServerListFragment(private val tempItemClickListener: ServerListAdapter.ItemClickListener) : VectorBaseDialogFragment<FragmentServerListBinding>(), ServerListAdapter.ItemClickListener {

    companion object {
        fun getInstance(itemClickListener: ServerListAdapter.ItemClickListener) = ServerListFragment(itemClickListener)
    }

    private lateinit var cyCoreViewModel: CyCoreViewModel
    private var federatedDomainList = mutableListOf<FederatedDomain>()
    val adapter = ServerListAdapter()

    override fun invalidate() {}

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentServerListBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cyCoreViewModel = fragmentViewModelProvider.get(CyCoreViewModel::class.java)
        cyCoreViewModel.getFederatedDomains()
        views.pbProgress.isVisible = true
        adapter.itemClickListener = this
        views.rvServerList.adapter = adapter

        cyCoreViewModel.federatedDomainList.observe(viewLifecycleOwner) {
            federatedDomainList = it.data.toMutableList()
            adapter.updateData(federatedDomainList)
            views.pbProgress.isVisible = false
        }

        views.etSearch.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty())
                adapter.updateData(federatedDomainList)
            else
                adapter.updateData(federatedDomainList.filter { it.name.lowercase().contains(text) }.toMutableList())
        }

        views.ivDismiss.setOnClickListener { dismiss() }
    }

    override fun onClick(item: FederatedDomain) {
        tempItemClickListener.onClick(item)
        dismiss()
    }
}
