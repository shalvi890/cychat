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

package com.cioinfotech.cychat.features.home.room.filtered

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.ScreenComponent
import com.cioinfotech.cychat.core.extensions.replaceFragment
import com.cioinfotech.cychat.core.platform.VectorBaseActivity
import com.cioinfotech.cychat.databinding.ActivityFilteredRoomsBinding
import com.cioinfotech.cychat.features.home.RoomListDisplayMode
import com.cioinfotech.cychat.features.home.room.list.RoomListFragment
import com.cioinfotech.cychat.features.home.room.list.RoomListParams

class FilteredRoomsActivity : VectorBaseActivity<ActivityFilteredRoomsBinding>() {

    private val roomListFragment: RoomListFragment?
        get() {
            return supportFragmentManager.findFragmentByTag(FRAGMENT_TAG) as? RoomListFragment
        }

    override fun getBinding() = ActivityFilteredRoomsBinding.inflate(layoutInflater)

    override fun getCoordinatorLayout() = views.coordinatorLayout

    override fun injectWith(injector: ScreenComponent) {
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureToolbar(views.filteredRoomsToolbar)
        if (isFirstCreation()) {
            val params = RoomListParams(RoomListDisplayMode.FILTERED)
            replaceFragment(R.id.filteredRoomsFragmentContainer, RoomListFragment::class.java, params, FRAGMENT_TAG)
        }
        /** To Change Text Hint Color */
        views.filteredRoomsSearchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).setHintTextColor(ContextCompat.getColor(this, R.color.white))
        views.filteredRoomsSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                roomListFragment?.filterRoomsWith(newText)
                return true
            }
        })
        // Open the keyboard immediately
        views.filteredRoomsSearchView.requestFocus()
    }

    companion object {
        private const val FRAGMENT_TAG = "RoomListFragment"

        fun newIntent(context: Context): Intent {
            return Intent(context, FilteredRoomsActivity::class.java)
        }
    }
}
