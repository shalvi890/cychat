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
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.databinding.FragmentSupplierConfirmationBinding
import org.matrix.android.sdk.internal.cy_auth.data.CountryCode
import org.matrix.android.sdk.internal.cy_auth.data.GetSettingsParent
import javax.inject.Inject

class SupplierConfirmationFragment @Inject constructor() : AbstractSSOLoginFragment<FragmentSupplierConfirmationBinding>(), AdapterView.OnItemSelectedListener {

    private var allSettings: GetSettingsParent? = null
    private var selectedCountry: CountryCode? = null
    private var firstTime = true
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSupplierConfirmationBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var spinnerArrayAdapter: ArrayAdapter<*> = ArrayAdapter(requireContext(),
                R.layout.item_spinner_country,
                mutableListOf<String>())

        views.spinner.adapter = spinnerArrayAdapter
        loginViewModel.handleGetSettings()
        loginViewModel.countryCodeList.observe(viewLifecycleOwner) {
            if (it != null && it.data.countries.isNotEmpty()) {
                allSettings = it
                val list = mutableListOf<String>()
                it.data.countries.forEach { countryCode -> list.add(countryCode.code + " " + countryCode.calling_code) }
                spinnerArrayAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_country,
                        list)
                views.spinner.adapter = spinnerArrayAdapter
                views.spinner.onItemSelectedListener = this
            }
        }

        views.btnSubmit.setOnClickListener { submit() }

        views.supplierField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    private fun submit() {
        loginViewModel.handleSupplierConfirmation()
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
        allSettings?.data?.countries?.let {
            selectedCountry = it[position]
        }
        if (firstTime)
            firstTime = false
        else
            checkIfCodeNeeded()
    }

    private fun checkIfCodeNeeded() {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
