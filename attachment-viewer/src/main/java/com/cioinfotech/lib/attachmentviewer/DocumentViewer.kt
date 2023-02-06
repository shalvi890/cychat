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

package com.cioinfotech.lib.attachmentviewer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URISyntaxException

class



DocumentViewer : AppCompatActivity(), View.OnClickListener {
    lateinit var ivBack: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.document_viewer)
        var webview = findViewById<
                WebView>(R.id.document_view)
        ivBack = findViewById(R.id.iv_back);

        webview.settings.javaScriptEnabled = true
        ivBack.setOnClickListener(this)

        var mediaURl = intent.getStringExtra("url")
        if (mediaURl.toString().contains(".pdf")) {
            webview.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$mediaURl")
        } else if (mediaURl.toString().contains(".png") || mediaURl.toString().contains(".jpg")) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show()
        } else {
            webview.loadUrl(mediaURl.toString())
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                this.finish()
            }

        }
    }
}
