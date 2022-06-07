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

package com.cioinfotech.cychat.features.plugins.model

import android.os.Parcel
import android.os.Parcelable

data class UserPlugin(
        val name: String? = null,
        val plAPIKey: String? = null,
        val plAPIURL: String? = null,
        val plDesc: String? = null,
        val plID: String? = null,
        val plImgURL: String? = null,
        val plIntroURL: String? = null,
        val plStatus: String? = null,
        val regStatus: String? = null,
        val regId: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(plAPIKey)
        parcel.writeString(plAPIURL)
        parcel.writeString(plDesc)
        parcel.writeString(plID)
        parcel.writeString(plImgURL)
        parcel.writeString(plIntroURL)
        parcel.writeString(plStatus)
        parcel.writeString(regStatus)
        parcel.writeString(regId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserPlugin> {
        override fun createFromParcel(parcel: Parcel): UserPlugin {
            return UserPlugin(parcel)
        }

        override fun newArray(size: Int): Array<UserPlugin?> {
            return arrayOfNulls(size)
        }
    }
}
