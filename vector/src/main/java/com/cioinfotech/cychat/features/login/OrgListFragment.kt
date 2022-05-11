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

package com.cioinfotech.cychat.features.login

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.cioinfotech.cychat.databinding.FragmentOrgListBinding
import com.cioinfotech.cychat.features.login.adapter.OrgListAdapter

class OrgListFragment(private val tempItemClickListener: OrgListAdapter.ItemClickListener,
                      private val list: MutableList<String>,
                      private val text: String) : DialogFragment(), OrgListAdapter.ItemClickListener {

    companion object {
        fun getInstance(itemClickListener: OrgListAdapter.ItemClickListener, list: MutableList<String>, text: String) = OrgListFragment(itemClickListener, list, text)
    }

    private val adapter = OrgListAdapter()
    private lateinit var views: FragmentOrgListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        views = FragmentOrgListBinding.inflate(inflater, container, false)
        return views.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views.pbProgress.isVisible = true
        adapter.itemClickListener = this
        views.rvServerList.adapter = adapter
        adapter.updateData(list)
        views.pbProgress.isVisible = false
        views.etSearch.hint = text
        views.etSearch.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty())
                adapter.updateData(list)
            else
                adapter.updateData(list.filter { it.lowercase().contains(text) }.toMutableList())
        }

        views.ivDismiss.setOnClickListener { dismiss() }
    }

    override fun onClick(name: String) {
        tempItemClickListener.onClick(name)
        dismiss()
    }
}
