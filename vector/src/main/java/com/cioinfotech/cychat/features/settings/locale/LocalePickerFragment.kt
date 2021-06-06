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

package com.cioinfotech.cychat.features.settings.locale

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.cleanup
import com.cioinfotech.cychat.core.extensions.configureWith
import com.cioinfotech.cychat.core.extensions.exhaustive
import com.cioinfotech.cychat.core.extensions.restart
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentLocalePickerBinding

import java.util.Locale
import javax.inject.Inject

class LocalePickerFragment @Inject constructor(
        private val viewModelFactory: LocalePickerViewModel.Factory,
        private val controller: LocalePickerController
) : VectorBaseFragment<FragmentLocalePickerBinding>(),
        LocalePickerViewModel.Factory by viewModelFactory,
        LocalePickerController.Listener {

    private val viewModel: LocalePickerViewModel by fragmentViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLocalePickerBinding {
        return FragmentLocalePickerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        views.localeRecyclerView.configureWith(controller)
        controller.listener = this

        viewModel.observeViewEvents {
            when (it) {
                LocalePickerViewEvents.RestartActivity -> {
                    activity?.restart()
                }
            }.exhaustive
        }
    }

    override fun onDestroyView() {
        views.localeRecyclerView.cleanup()
        controller.listener = null
        super.onDestroyView()
    }

    override fun invalidate() = withState(viewModel) { state ->
        controller.setData(state)
    }

    override fun onUseCurrentClicked() {
        // Just close the fragment
        parentFragmentManager.popBackStack()
    }

    override fun onLocaleClicked(locale: Locale) {
        viewModel.handle(LocalePickerAction.SelectLocale(locale))
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.setTitle(R.string.settings_select_language)
    }
}
