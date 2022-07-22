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
import com.cioinfotech.cychat.databinding.ItemSelectDocumentBinding

class SelectDocumentAdapter : RecyclerView.Adapter<SelectDocumentAdapter.SelectDocumentViewHolder>() {

    lateinit var itemClickListener: ItemClickListener
    val list = mutableListOf("Aadhar card", "Pan card", "License", "Bank Proof")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectDocumentViewHolder(
            ItemSelectDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SelectDocumentViewHolder, position: Int) {
        val binding = holder.itemBinding
        binding.root.setOnClickListener {
            itemClickListener.onItemClicked()
        }
    }

    override fun getItemCount() = list.size

    inner class SelectDocumentViewHolder(val itemBinding: ItemSelectDocumentBinding) : RecyclerView.ViewHolder(itemBinding.root)

    interface ItemClickListener {
        fun onItemClicked()
    }
}
