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

package com.cioinfotech.cychat.features.plugins.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cioinfotech.cychat.databinding.ItemPluginDocumentBinding

class PluginDocumentAdapter : RecyclerView.Adapter<PluginDocumentAdapter.DocumentViewHolder>() {

    lateinit var itemClickListener: ItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DocumentViewHolder(
            ItemPluginDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        val binding = holder.itemBinding
        binding.root.setOnClickListener {
            itemClickListener.onItemClicked()
        }
    }

    override fun getItemCount() = 5

    inner class DocumentViewHolder(val itemBinding: ItemPluginDocumentBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface ItemClickListener {
        fun onItemClicked()
    }
}
