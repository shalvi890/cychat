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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.databinding.ItemShowWallpaperBinding

class RoomWallpaperAdapter : RecyclerView.Adapter<RoomWallpaperAdapter.RoomWallpaperVH>() {

    var itemClickListener: ClickListener? = null
    private val resourceList = mutableListOf(
            R.drawable.wall1, R.drawable.wall2, R.drawable.wall3,
            R.drawable.wall4, R.drawable.wall5, R.drawable.wall6,
            R.drawable.wall7, R.drawable.wall8, R.drawable.wall9,
            R.drawable.wall10, R.drawable.wall11, R.drawable.wall12,
            R.drawable.wall13, R.drawable.wall14)

    inner class RoomWallpaperVH(val itemBinding: ItemShowWallpaperBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RoomWallpaperVH(
            ItemShowWallpaperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: RoomWallpaperVH, position: Int) {
        holder.itemBinding.ivWallpaper.setImageResource(resourceList[position])
        holder.itemBinding.root.setOnClickListener {
            itemClickListener?.onItemClicked(resourceList[position])
        }
    }

    override fun getItemCount() = resourceList.size

    interface ClickListener {
        fun onItemClicked(resource: Int)
    }
}
