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

package com.cioinfotech.cychat.features.settings

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.databinding.FragmentCyverseAddRoleBinding
import com.cioinfotech.cychat.features.cycore.data.AvailableUtype
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import org.matrix.android.sdk.internal.network.NetworkConstants.COMMON
import org.matrix.android.sdk.internal.network.NetworkConstants.INDIVIDUAL
import org.matrix.android.sdk.internal.network.NetworkConstants.NONE
import javax.inject.Inject

class CyverseAddRoleFragment @Inject constructor(
) : CyverseSettingsBaseFragment(), AdapterView.OnItemSelectedListener {
    override var titleRes = R.string.add_user_role

    private var allTypes = mutableListOf<AvailableUtype>()
    private var selectedType: AvailableUtype? = null
    private var reqId: String = ""
    private val binding: FragmentCyverseAddRoleBinding get() = viewBinding!!
    private var viewBinding: FragmentCyverseAddRoleBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentCyverseAddRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var spinnerArrayAdapter: ArrayAdapter<String>
        binding.progressBar.isVisible = true
        if (arguments?.getString("typeId") != null) {
            arguments?.let {
                selectedType = AvailableUtype(
                        it.getString("typeId") ?: "",
                        it.getString("typeName") ?: "",
                        INDIVIDUAL,
                        it.getString("typeDescription") ?: "",
                )
                reqId = it.getString("reqId", "") ?: ""
            }
            binding.progressBar.isVisible = false
            binding.clSelectRole.isVisible = false
            binding.clOtp.isVisible = true
        } else {
            cyCoreViewModel.handleAddUserTypes()
            cyCoreViewModel.addUserTypesLiveData.observe(viewLifecycleOwner) {
                allTypes = it.data.available_utypes
                val list = mutableListOf<String>()
                it.data.available_utypes.forEach { name -> list.add(name.utype_name) }
                spinnerArrayAdapter = ArrayAdapter(requireContext(), R.layout.item_spinner_country,
                        mutableListOf(getString(R.string.please_select_type_of_user)) + list)
                binding.spinner.adapter = spinnerArrayAdapter
                binding.spinner.onItemSelectedListener = this
                binding.progressBar.isVisible = false
            }
        }

        cyCoreViewModel.verifyAddUserTypeResponse.observe(viewLifecycleOwner) {
            it.data.req_id?.let { tempReqId ->
                reqId = tempReqId
            }
            binding.progressBar.isVisible = false
            if (selectedType?.verify_mode == NONE || selectedType?.verify_mode == COMMON) {
                Snackbar.make(requireView(), getString(R.string.role_added), BaseTransientBottomBar.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else
                setUpperSectionDisable()
        }

        cyCoreViewModel.otpVerifiedData.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), getString(R.string.role_added), BaseTransientBottomBar.LENGTH_SHORT).show()
            binding.progressBar.isVisible = false
            parentFragmentManager.popBackStack()
        }

        cyCoreViewModel.resendVerificationCode.observe(viewLifecycleOwner) {
            startCountDownForEmailOTP()
            binding.progressBar.isVisible = false
            Snackbar.make(requireView(), getString(R.string.verification_code_sent_to_supplier), BaseTransientBottomBar.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        binding.btnSubmit.setOnClickListener { submit() }

        binding.supplierField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.btnVerifyOTP.setOnClickListener { submitOTP() }

        binding.otpField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitOTP()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.btnResendOTP.setOnClickListener {
            cyCoreViewModel.handleResendVerificationCode()
        }

        cyCoreViewModel.errorData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false
            if (it != null) {
                Snackbar.make(requireView(), it.error ?: getString(R.string.something_went_wrong), BaseTransientBottomBar.LENGTH_SHORT).show()
//                when(it.error){
//
//                }
            }
        }
    }

    private fun submit() {
        if (selectedType != null)
            selectedType?.let {
                if (it.verify_mode == NONE) {
                    binding.progressBar.isVisible = true
                    cyCoreViewModel.handleVerifyAddUserType(
                            it.utype_id,
                            ""
                    )
                } else {
                    if (binding.supplierField.text.toString().isEmpty()) {
                        binding.tvSupplierTil.requestFocus()
                        binding.tvSupplierTil.error = getString(R.string.please_enter_code)
                    } else {
                        binding.progressBar.isVisible = true
                        cyCoreViewModel.handleVerifyAddUserType(
                                it.utype_id,
                                binding.supplierField.text.toString()
                        )
                    }
                }
            }
        else
            Toast.makeText(requireContext(), getString(R.string.please_select_type_of_user), Toast.LENGTH_LONG).show()
    }

    private fun startCountDownForEmailOTP() {
        var counter = 300
        binding.emailOTPTimer.isVisible = true
        binding.btnResendOTP.isEnabled = false
        object : CountDownTimer((counter * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    binding.emailOTPTimer.text = if (counter > 60)
                        getString(R.string.auth_resend_email_otp_query_min, counter / 60, counter % 60)
                    else
                        getString(R.string.auth_resend_mobile_otp_query, counter)
                    counter--
                } catch (ex: Exception) {
                }
            }

            override fun onFinish() {
                try {
                    binding.emailOTPTimer.isVisible = false
                    binding.btnResendOTP.isEnabled = true
                } catch (ex: Exception) {
                }
            }
        }.start()
    }

    private fun submitOTP() {
        when {
            binding.otpField.text.toString().isEmpty()   -> {
                binding.otpField.requestFocus()
                Toast.makeText(requireContext(), getString(R.string.please_enter_otp), Toast.LENGTH_LONG).show()
            }
            binding.otpField.text.toString().length != 4 -> {
                binding.otpField.requestFocus()
                Toast.makeText(requireContext(), getString(R.string.please_enter_valid_otp), Toast.LENGTH_LONG).show()
            }
            else                                         -> {
                binding.progressBar.isVisible = true
                cyCoreViewModel.handleVerifyOTP(
                        reqId,
                        selectedType?.utype_id ?: "",
                        binding.otpField.text.toString()
                )
            }
        }
    }

    fun setUpperSectionDisable(isEnable: Boolean = false) {
        binding.clOtp.isVisible = !isEnable
        binding.btnSubmit.isEnabled = isEnable
        binding.spinner.isEnabled = isEnable
        binding.supplierField.isEnabled = isEnable
        startCountDownForEmailOTP()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        allTypes.let {
            if (position != 0) {
                selectedType = it[position - 1]
                binding.tvUserDescription.text = selectedType?.description
                selectedType?.verify_mode?.let { verifyMode ->
                    binding.tvSupplierTil.isVisible = verifyMode != NONE
                    binding.btnSubmit.text = if (verifyMode != NONE)
                        getString(R.string.verify_code)
                    else
                        getString(R.string.auth_submit)
                }
            } else
                selectedType = null
        }
        binding.tvUserDescription.isVisible = selectedType != null
        binding.btnSubmit.isEnabled = selectedType != null
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}
