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
package com.cioinfotech.cychat.features.reactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.core.extensions.cleanup
import com.cioinfotech.cychat.core.extensions.configureWith
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.core.utils.LiveEvent
import com.cioinfotech.cychat.databinding.FragmentGenericRecyclerBinding

import javax.inject.Inject

class EmojiSearchResultFragment @Inject constructor(
        private val epoxyController: EmojiSearchResultController
) : VectorBaseFragment<FragmentGenericRecyclerBinding>(), ReactionClickListener {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGenericRecyclerBinding {
        return FragmentGenericRecyclerBinding.inflate(inflater, container, false)
    }

    private val viewModel: EmojiSearchResultViewModel by activityViewModel()

    private lateinit var sharedViewModel: EmojiChooserViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = activityViewModelProvider.get(EmojiChooserViewModel::class.java)
        epoxyController.listener = this
        views.genericRecyclerView.configureWith(epoxyController, showDivider = true)
    }

    override fun onDestroyView() {
        epoxyController.listener = null
        views.genericRecyclerView.cleanup()
        super.onDestroyView()
    }

    override fun onReactionSelected(reaction: String) {
        sharedViewModel.selectedReaction = reaction
        sharedViewModel.navigateEvent.value = LiveEvent(EmojiChooserViewModel.NAVIGATE_FINISH)
    }

    override fun invalidate() = withState(viewModel) { state ->
        epoxyController.setData(state)
    }
}