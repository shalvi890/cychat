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

package com.cioinfotech.cychat.features.roomdirectory.createroom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ScreenComponent
import com.cioinfotech.cychat.core.extensions.addFragment
import com.cioinfotech.cychat.core.platform.ToolbarConfigurable
import com.cioinfotech.cychat.core.platform.VectorBaseActivity
import com.cioinfotech.cychat.databinding.ActivitySimpleBinding
import com.cioinfotech.cychat.features.roomdirectory.RoomDirectorySharedAction
import com.cioinfotech.cychat.features.roomdirectory.RoomDirectorySharedActionViewModel

/**
 * Simple container for [CreateRoomFragment]
 */
class CreateRoomActivity : VectorBaseActivity<ActivitySimpleBinding>(), ToolbarConfigurable {

    private lateinit var sharedActionViewModel: RoomDirectorySharedActionViewModel

    override fun getBinding() = ActivitySimpleBinding.inflate(layoutInflater)

    override fun getCoordinatorLayout() = views.coordinatorLayout

    override fun configure(toolbar: Toolbar) {
        configureToolbar(toolbar)
    }

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun initUiAndData() {
        if (isFirstCreation()) {
            addFragment(
                    R.id.simpleFragmentContainer,
                    CreateRoomFragment::class.java,
                    CreateRoomArgs(intent?.getStringExtra(INITIAL_NAME) ?: "")
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedActionViewModel = viewModelProvider.get(RoomDirectorySharedActionViewModel::class.java)
        sharedActionViewModel
                .observe()
                .subscribe { sharedAction ->
                    when (sharedAction) {
                        is RoomDirectorySharedAction.Back,
                        is RoomDirectorySharedAction.Close -> finish()
                    }
                }
                .disposeOnDestroy()
    }

    companion object {
        private const val INITIAL_NAME = "INITIAL_NAME"

        fun getIntent(context: Context, initialName: String = ""): Intent {
            return Intent(context, CreateRoomActivity::class.java).apply {
                putExtra(INITIAL_NAME, initialName)
            }
        }
    }
}
