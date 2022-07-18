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

package com.cioinfotech.cychat.features.home.notice

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.addFragmentToBackstack
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentManageNoticesBinding
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.home.notice.adapters.NoticeBoardAdapter
import com.cioinfotech.cychat.features.home.notice.model.Notice
import com.cioinfotech.cychat.features.home.notice.model.NoticeListParent
import com.cioinfotech.cychat.features.home.notice.pagination.PaginationScrollListener
import com.cioinfotech.cychat.features.home.room.list.ProfileFullScreenFragment
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.matrix.android.sdk.internal.network.NetworkConstants
import timber.log.Timber

class ManageNoticesFragment : VectorBaseFragment<FragmentManageNoticesBinding>(), NoticeBoardAdapter.ClickListener {

    private lateinit var cyCoreViewModel: CyCoreViewModel
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private lateinit var noticesAdapter: NoticeBoardAdapter
    private var lastPost = -1

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentManageNoticesBinding {
        return FragmentManageNoticesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as NoticeBoardActivity).setToolbarTitle(getString(R.string.manage_notices))
        cyCoreViewModel = (requireActivity() as NoticeBoardActivity).cyCoreViewModel
        setupPagination()
    }

    private fun setupPagination() {
        loadFirstPage()
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        noticesAdapter = NoticeBoardAdapter(true).apply {
            clickListener = this@ManageNoticesFragment
        }
        views.rvNoticeBoard.adapter = noticesAdapter
        views.rvNoticeBoard.layoutManager = linearLayoutManager
        views.rvNoticeBoard.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage += 1
                loadNextPage()
            }

            override fun isLastPage() = this@ManageNoticesFragment.isLastPage
            override fun isLoading() = this@ManageNoticesFragment.isLoading
        })
    }

    private fun loadFirstPage() {
        isLoading = true
        cyCoreViewModel.getPostList(postType = NetworkConstants.POST_GET_MINE).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleFirstPostList())
    }

    private fun handleFirstPostList(): SingleObserver<NoticeListParent> {
        return object : SingleObserver<NoticeListParent> {

            override fun onSuccess(t: NoticeListParent) {
                noticesAdapter.removeLoadingFooter()
                t.data.data?.let { it1 -> noticesAdapter.addAll(it1) }
                lastPost = t.data.lastPost ?: -1
                if (!t.data.data.isNullOrEmpty()) noticesAdapter.addLoadingFooter() else isLastPage = true
                isLoading = false
                if (isLastPage)
                    noticesAdapter.removeLoadingFooter()
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                noticesAdapter.removeLoadingFooter()
                Timber.log(1, e)
            }
        }
    }

    private fun loadNextPage() {
        isLoading = true
        cyCoreViewModel.getPostList(lastPost, NetworkConstants.POST_GET_MINE).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleNextPostList())
    }

    private fun handleNextPostList(): SingleObserver<NoticeListParent> {
        return object : SingleObserver<NoticeListParent> {

            override fun onSuccess(t: NoticeListParent) {
                noticesAdapter.removeLoadingFooter()
                t.data.data?.let { noticesAdapter.addAll(it) }
                lastPost = t.data.lastPost ?: -1
                if (!t.data.data.isNullOrEmpty()) noticesAdapter.addLoadingFooter() else isLastPage = true
                isLoading = false
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onError(e: Throwable) {
                noticesAdapter.removeLoadingFooter()
                Timber.log(1, e)
            }
        }
    }

    override fun onClickListener(notice: Notice) {
        cyCoreViewModel.selectedNotice.postValue(notice)
        addFragmentToBackstack(R.id.container, CreateNoticeFragment::class.java)
    }

    override fun onAttachmentClicked(url: String) {
        (requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).apply {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substring(url.lastIndexOf("/", url.length - 1)))
            this.enqueue(request)
        }
    }

    override fun onAddToCalendarClicked(notice: Notice) {
    }

    override fun onPhotoClicked(url: String) {
        ProfileFullScreenFragment(null, null, url).show(childFragmentManager, "")
    }
}
