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

package com.cioinfotech.cychat.features.home.room

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.databinding.FragmentRoleAddAlertDialogBinding
import org.matrix.android.sdk.internal.network.NetworkConstants

class RoleAddAlertDialogFragment(private val itemClickListener: ItemClickListener) : DialogFragment() {

    private var viewBinding: FragmentRoleAddAlertDialogBinding? = null
    private val binding get() = viewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewBinding = FragmentRoleAddAlertDialogBinding.inflate(inflater, container, false)
        return binding.root
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
        val pref = DefaultSharedPreferences.getInstance(requireContext().applicationContext)
        pref.edit().apply {
            putBoolean(NetworkConstants.SIGNING_MODE, false)
            apply()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnAddRole.setOnClickListener {

            itemClickListener.onItemClicked()
            dismiss()
        }
    }

    interface ItemClickListener {
        fun onItemClicked()
    }
}
