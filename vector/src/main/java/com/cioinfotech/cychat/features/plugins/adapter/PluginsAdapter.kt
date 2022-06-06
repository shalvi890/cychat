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
import com.cioinfotech.cychat.databinding.ItemPluginsBinding

class PluginsAdapter : RecyclerView.Adapter<PluginsAdapter.PluginsViewHolder>() {

    lateinit var itemClickListener: ItemClickListener
    private var listOfPlugins = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PluginsViewHolder(
            ItemPluginsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: PluginsViewHolder, position: Int) {
        val binding = holder.itemBinding
        binding.tvDescription.text = "adklfhsldhg ladli ghsghldh isdgh siogh oisfhoghsoidg os dugsogupspd pogud psogpsfgp osufpg"
        binding.tvTitle.text = listOfPlugins[position]
        binding.root.setOnClickListener {
            itemClickListener.onItemClicked(listOfPlugins[position])
        }
    }

    override fun getItemCount() = listOfPlugins.size

    inner class PluginsViewHolder(val itemBinding: ItemPluginsBinding) : RecyclerView.ViewHolder(itemBinding.root)

    fun setData(listOfPlugins: MutableList<String>) {
        this.listOfPlugins = listOfPlugins
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemClicked(model: String)
    }
}
