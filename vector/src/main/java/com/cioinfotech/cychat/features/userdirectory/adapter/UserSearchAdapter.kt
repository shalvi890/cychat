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

package com.cioinfotech.cychat.features.userdirectory.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.databinding.ItemKnownUserBinding
import com.cioinfotech.cychat.features.cycore.data.SearchedUser
import com.cioinfotech.cychat.features.home.AvatarRenderer
import org.matrix.android.sdk.api.session.identity.ThreePid
import org.matrix.android.sdk.api.session.user.model.User
import org.matrix.android.sdk.api.util.MatrixItem
import org.matrix.android.sdk.api.util.toNormalEmail

class UserSearchAdapter(private val avatarRenderer: AvatarRenderer,
                        private val isOneToOneChat: Boolean) : RecyclerView.Adapter<UserSearchAdapter.UserSearchViewHolder>() {
    var callback: Callback? = null

    private var userList = mutableListOf<SearchedUser>()
    var selectedUsers = mutableListOf<SearchedUser>()
    private var excludedUserIds = mutableListOf<String>()

    class UserSearchViewHolder(val itemBinding: ItemKnownUserBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UserSearchViewHolder(
            ItemKnownUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: UserSearchViewHolder, position: Int) {
        val itemBinding = holder.itemBinding
        val user = userList[position]
        val matrixItem = MatrixItem.UserItem(user.matrixUserID,
                user.firstName + user.lastName,
                null)

        avatarRenderer.render(matrixItem, itemBinding.knownUserAvatar)
        itemBinding.root.setOnClickListener {
            if (!isOneToOneChat) {
                if (selectedUsers.contains(user))
                    selectedUsers.remove(user)
                else
                    selectedUsers.add(user)
                notifyDataSetChanged()
            }
            callback?.onItemClick(User(user.matrixUserID, user.firstName + user.lastName, null))
        }
        if (user.firstName.isNullOrEmpty() && user.lastName.isNullOrEmpty()) {
            itemBinding.knownUserID.visibility = View.GONE
            itemBinding.knownUserName.text = user.matrixUserID.toNormalEmail()
        } else {
            itemBinding.knownUserID.visibility = View.VISIBLE
            itemBinding.knownUserName.text = user.displayName
            itemBinding.knownUserID.text = user.email.toNormalEmail()
        }

        if (!isOneToOneChat) {
            if (selectedUsers.contains(user)) {
                itemBinding.knownUserAvatarChecked.visibility = View.VISIBLE
                val backgroundColor = ContextCompat.getColor(itemBinding.root.context, R.color.riotx_accent)
                val backgroundDrawable = TextDrawable.builder().buildRound("", backgroundColor)
                itemBinding.knownUserAvatar.setImageDrawable(backgroundDrawable)
            } else {
                itemBinding.knownUserAvatarChecked.visibility = View.GONE
                avatarRenderer.render(matrixItem, itemBinding.knownUserAvatar)
            }
        } else {
            itemBinding.knownUserAvatarChecked.visibility = View.GONE
            avatarRenderer.render(matrixItem, itemBinding.knownUserAvatar)
        }
    }

    override fun getItemCount() = userList.size

    fun setUserList(list: MutableList<SearchedUser>) {
        userList.clear()
        list.forEach {
            if (!excludedUserIds.contains(it.matrixUserID))
                userList.add(it)
        }
        callback?.showToastOnEmptyList(userList.isNullOrEmpty())
        notifyDataSetChanged()
    }

    fun updateExcludeList(excludedUserIds: Set<String>) {
        this.excludedUserIds = excludedUserIds.toMutableList()
    }

    interface Callback {
        fun onInviteFriendClick()
        fun onContactBookClick()
        fun onUseQRCode()
        fun onItemClick(user: User)
        fun onMatrixIdClick(matrixId: String)
        fun onThreePidClick(threePid: ThreePid)
        fun showToastOnEmptyList(isListEmpty: Boolean)
    }
}
