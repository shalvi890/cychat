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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.databinding.FragmentSupplierConfirmationBinding
import com.cioinfotech.cychat.features.login.adapter.ServerListAdapter
import org.matrix.android.sdk.internal.cy_auth.data.Group
import org.matrix.android.sdk.internal.cy_auth.data.UserType
import org.matrix.android.sdk.internal.cy_auth.data.UserTypeParent
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.RetrofitFactory.Companion.BASE_URL
import javax.inject.Inject

class SupplierConfirmationFragment @Inject constructor() : AbstractSSOLoginFragment<FragmentSupplierConfirmationBinding>(), AdapterView.OnItemSelectedListener, ServerListAdapter.ItemClickListener {

    private var allSettings: UserTypeParent? = null
    private var selectedCountry: UserType? = null
    private var hiddenCount = 0
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSupplierConfirmationBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var spinnerArrayAdapter = ArrayAdapter(requireContext(),
                R.layout.item_spinner_country,
                mutableListOf<String>())

        views.spinner.adapter = spinnerArrayAdapter
        loginViewModel.getUserType()
        views.btnSubmit.setOnClickListener { submit() }

        views.supplierField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        loginViewModel.observeViewEvents {
            if (it is LoginViewEvents.OnUserTypeConfirmed) {
                allSettings = it.userTypeParent
                val list = mutableListOf<String>()
                it.userTypeParent.data.forEach { name -> list.add(name.name) }
                spinnerArrayAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_country,
                        mutableListOf(getString(R.string.please_select_type_of_user)) + list)
                views.spinner.adapter = spinnerArrayAdapter
                views.spinner.onItemSelectedListener = this
            }

            if (it is LoginViewEvents.OnGetGroupsConfirmed)
                SelectEnvironmentDialog(it.groupParent, this).show(childFragmentManager, null)
        }

        views.btnHidden.setOnClickListener {
            hiddenCount++
            if (hiddenCount >= 4)
                loginViewModel.getGroup()
        }
    }

    private fun submit() {
        if (selectedCountry != null)
            selectedCountry?.let {
                BASE_URL = it.cychat_url
                DefaultSharedPreferences.getInstance(requireContext()).edit().apply {
                    putString(NetworkConstants.BASE_URL, it.cychat_url)
                    putString(NetworkConstants.U_TYPE_NAME,it.name)
                    apply()
                }

                if (it.verify_mode == NetworkConstants.NONE)
                    loginViewModel.handleSupplierConfirmation(
                            it.verify_mode == NetworkConstants.NONE,
                            "",
                            it.utype_id,
                            it.cychat_token,
                            it.setup_id)
                else {
                    if (views.supplierField.text.toString().isEmpty())
                        views.tvSupplierTil.error = getString(R.string.please_enter_code)
                    else
                        loginViewModel.handleSupplierConfirmation(
                                it.verify_mode == NetworkConstants.NONE,
                                views.supplierField.text.toString(),
                                it.utype_id,
                                it.cychat_token,
                                it.setup_id)
                }
            } else
            Toast.makeText(requireContext(), getString(R.string.please_select_type_of_user), Toast.LENGTH_LONG).show()
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
        allSettings?.data?.let {
            if (position != 0) {
                selectedCountry = it[position - 1]
                views.tvUserDescription.text = selectedCountry?.utype_desc
                selectedCountry?.verify_mode?.let { verifyMode ->
                    views.tvSupplierTil.isVisible = verifyMode != NetworkConstants.NONE
                    views.btnSubmit.text = if (verifyMode != NetworkConstants.NONE)
                        getString(R.string.check_code)
                    else
                        getString(R.string.auth_submit)
                }
            } else
                selectedCountry = null
        }
        views.btnSubmit.isEnabled = selectedCountry != null
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onClick(item: Group) {
        loginViewModel.groupValue = item.value
        loginViewModel.getUserType()
    }
}
