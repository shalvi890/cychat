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

package com.cioinfotech.cychat.features.roomdirectory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.airbnb.mvrx.viewModel
import com.airbnb.mvrx.withState
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ScreenComponent
import com.cioinfotech.cychat.core.extensions.addFragment
import com.cioinfotech.cychat.core.extensions.addFragmentToBackstack
import com.cioinfotech.cychat.core.platform.VectorBaseActivity
import com.cioinfotech.cychat.databinding.ActivitySimpleBinding
import com.cioinfotech.cychat.features.roomdirectory.createroom.CreateRoomFragment
import com.cioinfotech.cychat.features.roomdirectory.createroom.CreateRoomArgs
import com.cioinfotech.cychat.features.roomdirectory.picker.RoomDirectoryPickerFragment
import javax.inject.Inject

class RoomDirectoryActivity : VectorBaseActivity<ActivitySimpleBinding>() {

    @Inject lateinit var roomDirectoryViewModelFactory: RoomDirectoryViewModel.Factory
    private val roomDirectoryViewModel: RoomDirectoryViewModel by viewModel()
    private lateinit var sharedActionViewModel: RoomDirectorySharedActionViewModel

    override fun getBinding() = ActivitySimpleBinding.inflate(layoutInflater)

    override fun getCoordinatorLayout() = views.coordinatorLayout

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedActionViewModel = viewModelProvider.get(RoomDirectorySharedActionViewModel::class.java)

        if (isFirstCreation()) {
            roomDirectoryViewModel.handle(RoomDirectoryAction.FilterWith(intent?.getStringExtra(INITIAL_FILTER) ?: ""))
        }

        sharedActionViewModel
                .observe()
                .subscribe { sharedAction ->
                    when (sharedAction) {
                        is RoomDirectorySharedAction.Back           -> onBackPressed()
                        is RoomDirectorySharedAction.CreateRoom     -> {
                            // Transmit the filter to the CreateRoomFragment
                            withState(roomDirectoryViewModel) {
                                addFragmentToBackstack(
                                        R.id.simpleFragmentContainer,
                                        CreateRoomFragment::class.java,
                                        CreateRoomArgs(it.currentFilter)
                                )
                            }
                        }
                        is RoomDirectorySharedAction.ChangeProtocol ->
                            addFragmentToBackstack(R.id.simpleFragmentContainer, RoomDirectoryPickerFragment::class.java)
                        is RoomDirectorySharedAction.Close          -> finish()
                    }
                }
                .disposeOnDestroy()
    }

    override fun initUiAndData() {
        if (isFirstCreation()) {
            addFragment(R.id.simpleFragmentContainer, PublicRoomsFragment::class.java)
        }
    }

    companion object {
        private const val INITIAL_FILTER = "INITIAL_FILTER"

        fun getIntent(context: Context, initialFilter: String = ""): Intent {
            val intent = Intent(context, RoomDirectoryActivity::class.java)
            intent.putExtra(INITIAL_FILTER, initialFilter)
            return intent
        }
    }
}
