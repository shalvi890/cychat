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

@file:Suppress("DEPRECATION")

package com.cioinfotech.cychat.features.home.room.detail.timeline.helper

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import android.view.ViewTreeObserver.OnGlobalLayoutListener as OnGlobalLayoutListener1

object ShowMoreText {

    fun makeTextViewResizable(tv: TextView?, expandText: String, viewMore: Boolean, maxLine: Int = 3) {
        if (tv?.tag == null) {
            tv?.tag = tv?.text
        }
        val vto = tv?.viewTreeObserver
        vto?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener1 {
            override fun onGlobalLayout() {
                val obs = tv.viewTreeObserver
                obs.removeGlobalOnLayoutListener(this)
                if (!tv.text.toString().contains(".. See More") && !tv.text.toString().contains("See Less") && maxLine < 3) {
                    tv.text = tv.text.toString()
                } else if (maxLine == 0) {
                    val lineEndIndex = tv.layout.getLineEnd(0)
                    val text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1).toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.text.toString()), tv, expandText,
                                    viewMore, maxLine), TextView.BufferType.SPANNABLE)
                } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                    val lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                    val text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1).toString() + " " + expandText
                    tv.text = text
                    tv.movementMethod = LinkMovementMethod.getInstance()
                    tv.setText(
                            addClickablePartTextViewResizable(Html.fromHtml(tv.text.toString()), tv, expandText,
                                    viewMore, maxLine), TextView.BufferType.SPANNABLE)
                } else {
                    tv.layout?.let {
                        val lineEndIndex: Int = it.getLineEnd(it.lineCount - 1)
                        val text = tv.text.subSequence(0, lineEndIndex).toString() + " " + expandText
                        tv.text = text
                        tv.movementMethod = LinkMovementMethod.getInstance()
                        tv.setText(
                                addClickablePartTextViewResizable(Html.fromHtml(tv.text.toString()), tv, expandText,
                                        viewMore, maxLine), TextView.BufferType.SPANNABLE)
                    }
                }
            }
        })
    }

    fun addClickablePartTextViewResizable(strSpanned: Spanned, tv: TextView, spanableText: String, viewMore: Boolean, maxLine: Int): SpannableStringBuilder {
        val str = strSpanned.toString()
        val ssb = SpannableStringBuilder(strSpanned)
        if (str.contains(spanableText)) {
            ssb.setSpan(object : MySpannable(false) {
                override fun onClick(widget: View) {
                    if (viewMore) {
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        tv.invalidate()
                        makeTextViewResizable(tv, "See Less", false, -1)
                    } else {
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        tv.invalidate()
                        makeTextViewResizable(tv, ".. See More", true, maxLine)
                    }
                }
            }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length, 0)
        }
        return ssb
    }
}
