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

package com.cioinfotech.cychat.features.home.room.detail.audiorecorder

import android.annotation.SuppressLint
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.glide.GlideApp
import com.cioinfotech.cychat.core.platform.VectorBaseBottomSheetDialogFragment
import com.cioinfotech.cychat.databinding.FragmentAudioRecorderBinding
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorderFragment : VectorBaseBottomSheetDialogFragment<FragmentAudioRecorderBinding>() {

    var recordAudio: RecordAudio? = null

    interface RecordAudio {
        fun sendRecordedAudioFile(file: File)
    }

    private var mRecorder: MediaRecorder? = null
    private var mFileName: String? = null
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentAudioRecorderBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmSS", Locale.getDefault()).format(
                Date()
        )
        mFileName = requireContext().getExternalFilesDir(null)!!.absolutePath + "/recording_$timeStamp.mp3"
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(mFileName)
            try {
                prepare()
            } catch (e: IOException) {
                Timber.e(e.localizedMessage)
            }
            start()
        }
        GlideApp.with(requireContext()).asGif()
                .load(R.drawable.voice_recorder)
                .into(views.ivGif)

        var counter = 0
        val temp = object : CountDownTimer(100000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                try {
                    view.findViewById<TextView>(R.id.tv_timer).text =
                            "${counter / 60}:" + if (counter % 60 < 10) "0${counter % 60}" else "${counter % 60}"
                    counter++
                } catch (ex: Exception) {
                }
            }

            override fun onFinish() {
                try {
                    stopRecorderAndDeleteFile()
                    dismiss()
                } catch (ex: Exception) {
                }
            }
        }.start()

        views.btnDelete.setOnClickListener {
            stopRecorderAndDeleteFile()
            temp.cancel()
        }
        views.btnSend.setOnClickListener {
            stopRecorderAndSendAudio()
            temp.cancel()
            dismiss()
        }
    }

    fun stopRecorderAndDeleteFile() {
        stopRecorder()
        deleteFile()
        dismiss()
    }

    fun deleteFile() {
        mFileName?.let {
            val fdelete = File(it)
            if (fdelete.exists()) {
                fdelete.delete()
            }
        }
    }

    fun stopRecorderAndSendAudio() {
        stopRecorder()
        sendFile()
        dismiss()
    }

    fun sendFile() {
        mFileName?.let {
            recordAudio?.sendRecordedAudioFile(File(it))
        }
    }

    fun stopRecorder() {
        mRecorder?.stop()
        mRecorder?.release()
        mRecorder = null
    }
}
