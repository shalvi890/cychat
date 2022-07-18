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
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME
import android.provider.CalendarContract.EXTRA_EVENT_END_TIME
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentNoticeBoardBinding
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.home.notice.adapters.NoticeBoardAdapter
import com.cioinfotech.cychat.features.home.notice.model.Notice
import com.cioinfotech.cychat.features.home.notice.model.NoticeListParent
import com.cioinfotech.cychat.features.home.notice.pagination.PaginationScrollListener
import com.cioinfotech.cychat.features.home.notice.widget.NoticeFabMenuView
import com.cioinfotech.cychat.features.home.room.list.ProfileFullScreenFragment
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.Calendar

class NoticeBoardFragment : VectorBaseFragment<FragmentNoticeBoardBinding>(), NoticeFabMenuView.Listener, NoticeBoardAdapter.ClickListener {

    private lateinit var cyCoreViewModel: CyCoreViewModel
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private lateinit var noticesAdapter: NoticeBoardAdapter
    private var lastPost = -1

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNoticeBoardBinding {
        return FragmentNoticeBoardBinding.inflate(inflater, container, false)
    }

    override fun onResume() {
        super.onResume()

        cyCoreViewModel = fragmentViewModelProvider.get(CyCoreViewModel::class.java)
        setupPagination()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupPagination() {
        loadFirstPage()
        noticesAdapter = NoticeBoardAdapter()
        noticesAdapter.clickListener = this
        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        views.rvNoticeBoard.adapter = noticesAdapter
        views.rvNoticeBoard.layoutManager = linearLayoutManager
//        views.createChatFabMenu.listener = this
        views.btnAdd.setOnClickListener {
            fabOpenRoomDirectory()
        }
        views.rvNoticeBoard.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage += 1
                loadNextPage()
            }

            override fun isLastPage() = this@NoticeBoardFragment.isLastPage
            override fun isLoading() = this@NoticeBoardFragment.isLoading
        })
    }

    private fun loadFirstPage() {
        isLoading = true
        cyCoreViewModel.getPostList().subscribeOn(Schedulers.io())
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
        cyCoreViewModel.getPostList(lastPost).subscribeOn(Schedulers.io())
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

    private fun setupToolbar() {
//        val parentActivit/ = vectorBaseActivity
//        if (parentActivity is ToolbarConfigurable)
//            parentActivity.configure(views.groupToolbar)

//        views.groupToolbar.title = ""
//        views.groupToolbarAvatarImageView.debouncedClicks {
//            sharedActionViewModel.post(HomeActivitySharedAction.OpenDrawer)
//        }
    }

    override fun fabCreateDirectChat() {
        navigator.openNoticeBoardActivity(requireActivity(), false)
    }

    override fun fabOpenRoomDirectory() {
        navigator.openNoticeBoardActivity(requireActivity(), true)
    }

    override fun onClickListener(notice: Notice) {
    }

    override fun onPhotoClicked(url: String) {
        ProfileFullScreenFragment(null, null, url).show(childFragmentManager, "")
    }

    private var enqueue: Long = 0L
    override fun onAttachmentClicked(url: String) {
        (requireActivity().getSystemService(DOWNLOAD_SERVICE) as DownloadManager).apply {
            val request = DownloadManager.Request(Uri.parse(url))
            val fileName = if (url.contains("/")) url.substring(url.lastIndexOf("/") + 1, url.length) else url
            request.setTitle(fileName)
            request.setDescription("Downloading $fileName")
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            enqueue = this.enqueue(request)
        }
        requireContext().registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onAddToCalendarClicked(notice: Notice) {
        try {
            val calendarEvent = Calendar.getInstance().apply {
                val startDate = notice.event?.eventStart ?: ""
                val year = Integer.valueOf(startDate.substring(0, 4))
                val month = Integer.valueOf(startDate.substring(5, 7))-1
                val day = Integer.valueOf(startDate.substring(8, 10))
                val hourOfDay = Integer.valueOf(startDate.substring(11, 13))
                val minute = Integer.valueOf(startDate.substring(14, 16))
                set(year, month, day, hourOfDay, minute)
            }
            val intent = Intent(Intent.ACTION_EDIT)
            intent.type = "vnd.android.cursor.item/event"
            intent.putExtra(EXTRA_EVENT_BEGIN_TIME, calendarEvent.timeInMillis)
//            intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, notice.event?.eventTzName)
            val calendarEventEnd = Calendar.getInstance().apply {
                val startDate = notice.event?.eventEnd ?: ""
                val year = Integer.valueOf(startDate.substring(0, 4))
                val month = Integer.valueOf(startDate.substring(5, 7))-1
                val day = Integer.valueOf(startDate.substring(8, 10))
                val hourOfDay = Integer.valueOf(startDate.substring(11, 13))
                val minute = Integer.valueOf(startDate.substring(14, 16))
                set(year, month, day, hourOfDay, minute)
            }
            intent.putExtra(EXTRA_EVENT_END_TIME, calendarEventEnd.timeInMillis)
            intent.putExtra("title", notice.title)
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No Calendar App Found!", Toast.LENGTH_LONG).show()
        }
    }

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                val query = DownloadManager.Query()
                query.setFilterById(enqueue)
                val downloadManager = (requireActivity().getSystemService(DOWNLOAD_SERVICE) as DownloadManager)
                val c = downloadManager.query(query)
                if (c.moveToFirst()) {
                    val columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        try {
//                            val uriString: String = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
                        } catch (ex: Exception) {
                            Toast.makeText(requireContext(), "No Calendar App Found!", Toast.LENGTH_LONG).show()
                        } finally {
                            Toast.makeText(context, "File downloaded in downloads folder", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
