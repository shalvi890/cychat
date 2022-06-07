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

package com.cioinfotech.cychat.features.plugins.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.features.cycore.data.ErrorModel
import com.cioinfotech.cychat.features.plugins.model.PluginListParentModel
import com.cioinfotech.cychat.features.plugins.service.PluginService
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.matrix.android.sdk.internal.network.NetworkConstants
import timber.log.Timber
import javax.inject.Inject

class PluginViewModel @Inject constructor(
        private val pluginService: PluginService,
        applicationContext: Context
) : ViewModel() {

    private var pref = DefaultSharedPreferences.getInstance(applicationContext)
    private var url = pref.getString(NetworkConstants.BASE_URL, "") ?: ""
    private val clid = pref.getString(NetworkConstants.CLID, "") ?: ""
    val errorData = MutableLiveData<ErrorModel?>()

    val pluginListLiveData = MutableLiveData<PluginListParentModel>()
    fun handleGetUserPlugins() {
        pluginService.getPlugins(
                hashMapOf(
                        NetworkConstants.CLIENT_NAME to NetworkConstants.CY_VERSE_ANDROID,
                        NetworkConstants.OP to NetworkConstants.GET_USER_PLUGINS,
                        NetworkConstants.SERVICE_NAME to NetworkConstants.PLUGINS,
                        NetworkConstants.USER_ID to (pref.getString(NetworkConstants.USER_ID, "") ?: "")
                ), url,clid).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserPlugins())
    }

    private fun getUserPlugins(): SingleObserver<PluginListParentModel> {
        return object : SingleObserver<PluginListParentModel> {

            override fun onSuccess(t: PluginListParentModel) {
                if (t.status == "ok")
                    pluginListLiveData.postValue(t)
                else
                    errorData.postValue(ErrorModel(NetworkConstants.GET_USER_PLUGINS, t.message))
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                Timber.log(1, e)
            }
        }
    }
}
