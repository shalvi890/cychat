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

package com.cioinfotech.cychat.features.settings.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cioinfotech.cychat.databinding.ItemServerListBinding
import com.cioinfotech.cychat.features.cycore.data.ActiveRole

class CyverseRoleAdapter : RecyclerView.Adapter<CyverseRoleAdapter.RoleViewHolder>() {
    private var list = mutableListOf<ActiveRole>()

    var itemClickListener: ItemClickListener? = null

    inner class RoleViewHolder(val itemBinding: ItemServerListBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RoleViewHolder(
            ItemServerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: RoleViewHolder, position: Int) {
        holder.itemBinding.tvServerName.text = list[position].utype_name
//        holder.itemBinding.root.setOnClickListener {
//            itemClickListener?.onClick(list[position])
//        }
    }

    fun updateData(list: MutableList<ActiveRole>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size

    interface ItemClickListener {
        fun onClick(item: String)
    }
}

