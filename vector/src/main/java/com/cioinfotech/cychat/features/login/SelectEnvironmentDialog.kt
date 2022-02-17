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
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.cioinfotech.cychat.databinding.DialogSelectEnvironmentBinding
import com.cioinfotech.cychat.features.login.adapter.ServerListAdapter
import org.matrix.android.sdk.internal.cy_auth.data.Group
import org.matrix.android.sdk.internal.cy_auth.data.GroupParent

class SelectEnvironmentDialog(private val groupParentData: GroupParent, private val itemClickListener: ServerListAdapter.ItemClickListener) : DialogFragment(), ServerListAdapter.ItemClickListener {

    private val viewBinding: DialogSelectEnvironmentBinding get() = binding!!
    private var binding: DialogSelectEnvironmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogSelectEnvironmentBinding.inflate(inflater, container, false)
        return viewBinding.root
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
        val adapter = ServerListAdapter().apply {
            updateData(groupParentData.data)
            itemClickListener = this@SelectEnvironmentDialog
        }
        viewBinding.rvServerList.adapter = adapter
        viewBinding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty())
                adapter.updateData(groupParentData.data)
            else
                adapter.updateData(groupParentData.data.filter { it.desc.lowercase().contains(text) }.toMutableList())
        }

        viewBinding.ivDismiss.setOnClickListener { dismiss() }
    }

    override fun onClick(item: Group) {
        itemClickListener.onClick(item)
        dismiss()
    }
}
