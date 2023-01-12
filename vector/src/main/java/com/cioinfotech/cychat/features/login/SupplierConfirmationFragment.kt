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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.extensions.hideKeyboard
import com.cioinfotech.cychat.databinding.FragmentSupplierConfirmationBinding
import com.cioinfotech.cychat.features.login.adapter.OrgListAdapter
import com.cioinfotech.cychat.features.login.adapter.ServerListAdapter
import org.matrix.android.sdk.internal.cy_auth.data.Group
import org.matrix.android.sdk.internal.cy_auth.data.Organization
import org.matrix.android.sdk.internal.cy_auth.data.UserType
import org.matrix.android.sdk.internal.cy_auth.data.UserTypeParent
import org.matrix.android.sdk.internal.network.NetworkConstants
import org.matrix.android.sdk.internal.network.NetworkConstants.U_REG_TITLE
import org.matrix.android.sdk.internal.network.RetrofitFactory.Companion.BASE_URL
import javax.inject.Inject

class SupplierConfirmationFragment @Inject constructor() : AbstractSSOLoginFragment<FragmentSupplierConfirmationBinding>(), ServerListAdapter.ItemClickListener {

    private var allSettings: UserTypeParent? = null
    private val userTypes = mutableListOf<UserType>()
    private var organizations = mutableListOf<Organization>()
    private var individualType: UserType? = null
    private var selectedUserType: UserType? = null
    private var hiddenCount = 0
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSupplierConfirmationBinding.inflate(layoutInflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel.getIndividualType()
        loginViewModel.getOrganizations()
        views.rbIndividual.isChecked = true
        views.btnSubmit.setOnClickListener { submit() }

        views.supplierField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        loginViewModel.observeViewEvents {
            when (it) {
                is LoginViewEvents.OnOrganizationConfirmed -> {
                    organizations = it.orgParent.data.userTypes
                    views.rbOrganization.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked)
                            views.rbIndividual.isChecked = false
                        setOrganizationsView()
                    }
                }
                is LoginViewEvents.OnIndividualConfirmed   -> {
                    it.userTypeParent.data?.userTypes?.let { individualTypes ->
                        individualType = individualTypes[0]
                        views.rbIndividual.isVisible = true
                        views.llRbOrganization.isVisible =false
                        individualTypesSelection();
                    }
                }
                is LoginViewEvents.OnUserTypeConfirmed     -> {
                    allSettings = it.userTypeParent
                    resetUserTypeView()
                }
                else                                       -> Unit
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

    private fun individualTypesSelection() {
            views.rbOrganization.isChecked = false
            views.tvUserDescription.isVisible = false
            views.tvOrgDescription.isVisible = false
            views.spinnerOrganization.isVisible = false
            views.tvSelectOrg.isVisible = false
            views.spinner.isVisible = false
            views.tvSelectUser.isVisible = false
//                                views.tvOrgDescription.isVisible = false
            selectedUserType = individualType
//                                views.tvUserDescription.isVisible = true
//                                views.tvUserDescription.text = selectedUserType?.ut_cat_desc
            selectedUserType?.verifyMode?.let { verifyMode ->
                views.tvSupplierTil.isVisible = verifyMode != NetworkConstants.NONE
                if (verifyMode != NetworkConstants.NONE && selectedUserType?.regTitle != null) {
                    views.tvSupplierTil.hint = selectedUserType?.regTitle
                }
                views.btnSubmit.text = if (verifyMode != NetworkConstants.NONE)
                    getString(R.string.next)
                else
                    getString(R.string.next)
            }
            views.btnSubmit.isEnabled = true
            views.btnSubmit.isVisible = true

    }

    private fun setOrganizationsView() {
        views.spinnerOrganization.setText("")
        views.spinnerOrganization.isVisible = true
        views.btnSubmit.isVisible = false
        views.tvSupplierTil.isVisible = false
        views.tvSelectOrg.isVisible = true
        views.spinnerOrganization.setOnClickListener {
            OrgListFragment.getInstance(object : OrgListAdapter.ItemClickListener {
                override fun onClick(name: String) {
                    var org: Organization? = null
                    for (tempOrg in organizations)
                        if (name == tempOrg.utCatName) {
                            org = tempOrg
                            break
                        }
                    org?.utCatID?.let { loginViewModel.getUserType(it) }
                    views.spinner.setText("")
                    views.spinnerOrganization.setText(org?.utCatName)
                    Log.e("@@", org?.utCatDesc.toString())
                    views.tvOrgDescription.isVisible = true
                    views.tvOrgDescription.text = org?.utCatDesc
                    views.spinner.isVisible = false
                    views.tvUserDescription.isVisible = false
                    views.tvSelectUser.isVisible = false
                    selectedUserType = null
                    views.btnSubmit.isEnabled = false
//                    views.tvUserDescription.text = null
                }
            },
                    organizations.map { org ->
                        org.utCatName
                    }.toMutableList(),
                    getString(R.string.search_organization)).show(parentFragmentManager, "")
        }
    }

    private fun resetUserTypeView() {
        views.spinner.isVisible = true
        views.tvSelectUser.isVisible = true
        val list = mutableListOf<String>()
        allSettings?.data?.userTypes?.forEach { type ->
            list.add(type.utypeName)
            userTypes.add(type)
        }

        views.spinner.setOnClickListener {
            OrgListFragment.getInstance(object : OrgListAdapter.ItemClickListener {
                override fun onClick(name: String) {
                    for (temp in userTypes)
                        if (name == temp.utypeName) {
                            selectedUserType = temp
                            break
                        }
                    views.tvUserDescription.text = selectedUserType?.utypeDesc
                    views.tvUserDescription.isVisible = true
                    views.spinner.setText(selectedUserType?.utCatName)
                    //  Log.e("@@ utypeDesc ", selectedUserType?.utypeDesc.toString())
                    selectedUserType?.verifyMode?.let { verifyMode ->
                        views.tvSupplierTil.isVisible = verifyMode != NetworkConstants.NONE
                        if (verifyMode != NetworkConstants.NONE && selectedUserType?.regTitle != null) {
                            views.tvSupplierTil.hint = selectedUserType?.regTitle
                        }
                        views.btnSubmit.text = if (verifyMode != NetworkConstants.NONE)
                            getString(R.string.check_code)
                        else
                            getString(R.string.auth_submit)
                    }
                    views.btnSubmit.isVisible = true
                    views.btnSubmit.isEnabled = selectedUserType != null
                    views.root.hideKeyboard()
                }
            }, list, getString(R.string.search_your_organization)).show(parentFragmentManager, "")
        }

        views.spinner.requestFocus()
    }

    private fun submit() {
        if (selectedUserType != null)
            selectedUserType?.let {
                BASE_URL = it.cychatURL
                DefaultSharedPreferences.getInstance(requireContext()).edit().apply {
                    putString(NetworkConstants.BASE_URL, it.cychatURL)
                    putString(NetworkConstants.U_TYPE_NAME, it.utypeName)
                    putString(NetworkConstants.U_TYPE_MODE, it.verifyMode)
                    putString(U_REG_TITLE, it.regTitle)
                    apply()
                }

                if (it.verifyMode == NetworkConstants.NONE)
                    loginViewModel.handleSupplierConfirmation(
                            "",
                            it.utypeID,
                            it.cychatToken,
                            it.setupID,
                            it.utypeName)
                else {
                    if (views.supplierField.text.toString().isEmpty())
                        views.tvSupplierTil.error = getString(R.string.please_enter_code)
                    else
                        loginViewModel.handleSupplierConfirmation(
                                views.supplierField.text.toString(),
                                it.utypeID,
                                it.cychatToken,
                                it.setupID,
                                it.utypeName)
                }
            } else
            Toast.makeText(requireContext(), getString(R.string.select_your_organization), Toast.LENGTH_LONG).show()
    }

    override fun onError(throwable: Throwable) {
        showErrorInSnackbar(
                if (throwable.message?.contains("502") == true)
                    Throwable(getString(R.string.something_went_wrong))
                else throwable
        )
    }

    override fun resetViewModel() {}

    override fun onClick(item: Group) {
        loginViewModel.groupValue = item.value
        loginViewModel.getOrganizations()
    }
}
