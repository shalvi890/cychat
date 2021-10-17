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

package com.cioinfotech.cychat.features.home.room.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.cioinfotech.cychat.R

class RoomHomeConcatAdapter(private val context: Context, private val adapter: EpoxyControllerAdapter) :
        RecyclerView.Adapter<BaseConcatHolder<*>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseConcatHolder<*> {
        val view = LayoutInflater.from(context).inflate(R.layout.item_concat_home_room, parent, false)
        view.findViewById<RecyclerView>(R.id.rv_concat).layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        return ConcatViewHolder(view)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: BaseConcatHolder<*>, position: Int) {
        (holder as ConcatViewHolder).bind(adapter)
    }

    inner class ConcatViewHolder(itemView: View) : BaseConcatHolder<EpoxyControllerAdapter>(itemView) {
        override fun bind(adapter: EpoxyControllerAdapter) {
            itemView.findViewById<RecyclerView>(R.id.rv_concat).adapter = adapter
        }
    }
}

abstract class BaseConcatHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(adapter: T)
}
