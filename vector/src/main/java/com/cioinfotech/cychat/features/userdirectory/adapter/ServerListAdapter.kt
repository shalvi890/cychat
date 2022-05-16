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

package com.cioinfotech.cychat.features.userdirectory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cioinfotech.cychat.databinding.ItemServerListBinding
import org.matrix.android.sdk.internal.cy_auth.data.FederatedDomain

class ServerListAdapter : RecyclerView.Adapter<ServerListAdapter.ServerListViewHolder>() {
    private var serverList = mutableListOf<FederatedDomain>()

    var itemClickListener: ItemClickListener? = null

    inner class ServerListViewHolder(val itemBinding: ItemServerListBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ServerListViewHolder(
            ItemServerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ServerListViewHolder, position: Int) {
        holder.itemBinding.tvServerName.text = serverList[position].utypeName
        holder.itemBinding.root.setOnClickListener {
            itemClickListener?.onClick(serverList[position])
        }
    }

    fun updateData(list: MutableList<FederatedDomain>) {
        serverList = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = serverList.size

    interface ItemClickListener {
        fun onClick(item: FederatedDomain)
    }
}
