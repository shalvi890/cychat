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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.databinding.FragmentUserMappingBinding
import javax.inject.Inject

class UserMappingFragment @Inject constructor() : AbstractSSOLoginFragment<FragmentUserMappingBinding>(), AdapterView.OnItemSelectedListener {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUserMappingBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        views.btnCheckCode.setOnClickListener { submit() }

        views.supplierField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun submit() {
        loginViewModel.handleUserMappingConfirmed()
    }

    override fun onError(throwable: Throwable) {
        showErrorInSnackbar(
                if (throwable.message?.contains("502") == true)
                    Throwable(getString(R.string.something_went_wrong))
                else throwable
        )
    }

    override fun resetViewModel() {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
