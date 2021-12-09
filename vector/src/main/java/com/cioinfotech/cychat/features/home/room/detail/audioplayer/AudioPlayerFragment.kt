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

package com.cioinfotech.cychat.features.home.room.detail.audioplayer

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.platform.VectorBaseBottomSheetDialogFragment
import com.cioinfotech.cychat.databinding.FragmentAudioPlayerBinding
import java.util.concurrent.TimeUnit

class AudioPlayerFragment(private val url: String? = null, private val fileName: String?, private val file: Uri?) : VectorBaseBottomSheetDialogFragment<FragmentAudioPlayerBinding>(), MediaPlayer.OnPreparedListener {

    private val mediaPlayer = MediaPlayer()

    private var startTime = 0.0
    private var finalTime = 0.0

    private var myHandler: Handler = Handler(Looper.myLooper()!!)
    private val forwardTime = 5000
    private val backwardTime = 5000
    var oneTimeOnly = 0

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentAudioPlayerBinding.inflate(inflater, container, false)

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        views.clWaitingView.waitingView.isVisible = true
        if (url.isNullOrEmpty() && file == null)
            dismiss()

//        fileName?.let {
//            views.tvFileName.text = it
//        }
        views.seekBar.setOnTouchListener { _, _ -> true }
        try {
            mediaPlayer.apply {
                setOnPreparedListener(this@AudioPlayerFragment)
                if (!url.isNullOrEmpty())
                    setDataSource(url)
                else {
                    file?.let {
                        setDataSource(requireContext(), it)
                    }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer.setOnCompletionListener {
            views.btnSend.setImageResource(R.drawable.ic_play_arrow)
        }
        views.btnSend.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                views.btnSend.setImageResource(R.drawable.ic_play_arrow)
                mediaPlayer.pause()
            } else
                startPlayer()
        }

        views.btnDelete.setOnClickListener {
            dismiss()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        finalTime = mediaPlayer.duration.toDouble()
        startTime = mediaPlayer.currentPosition.toDouble()
        views.btnSend.setImageResource(R.drawable.ic_pause)

        if (oneTimeOnly == 0) {
            views.seekBar.max = (finalTime.toInt())
            oneTimeOnly = 1
        }

        views.tvTime.text = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))

        views.tvTotal.text = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                (TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong())).toInt() % 60)

        views.seekBar.progress = startTime.toInt()
        myHandler.postDelayed(updateSongTime, 1)

        views.btnPlayAhead.setOnClickListener {
            val temp = startTime
            if ((temp + forwardTime) <= finalTime) {
                startTime += forwardTime
                mediaPlayer.seekTo(startTime.toInt())
            }
        }

        views.btnPlayBack.setOnClickListener {
            val temp = startTime
            if ((temp - backwardTime) > 0) {
                startTime -= backwardTime
                mediaPlayer.seekTo(startTime.toInt())
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        startPlayer()
    }

    override fun onDestroyView() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroyView()
    }

    private val updateSongTime = object : Runnable {
        override fun run() {
            try {
                startTime = mediaPlayer.currentPosition.toDouble()
                views.tvTime.text = String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
                views.seekBar.progress = startTime.toInt()
                myHandler.postDelayed(this, 100)
            } catch (ex: Exception) {
            }
        }
    }
}
