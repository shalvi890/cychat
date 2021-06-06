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

package com.cioinfotech.cychat.core.platform

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import com.cioinfotech.cychat.R

private const val MIN_SNACKBAR_DURATION = 2000
private const val MAX_SNACKBAR_DURATION = 8000
private const val DURATION_PER_LETTER = 50

fun View.showOptimizedSnackbar(message: String) {
    Snackbar.make(this, HtmlCompat.fromHtml("<b>$message</b>", HtmlCompat.FROM_HTML_MODE_LEGACY), getDuration(message)).apply {
        setBackgroundTint(ContextCompat.getColor(this.context, R.color.primary_color_light))
        setTextColor(ContextCompat.getColor(this.context, R.color.white))
        show()
    }
}

private fun getDuration(message: String): Int {
    return (message.length * DURATION_PER_LETTER).coerceIn(MIN_SNACKBAR_DURATION, MAX_SNACKBAR_DURATION)
}
