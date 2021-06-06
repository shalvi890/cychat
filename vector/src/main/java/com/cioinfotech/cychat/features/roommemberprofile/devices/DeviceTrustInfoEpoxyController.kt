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
package com.cioinfotech.cychat.features.roommemberprofile.devices

import com.airbnb.epoxy.TypedEpoxyController
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.resources.ColorProvider
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.ui.list.GenericItem
import com.cioinfotech.cychat.core.ui.list.genericFooterItem
import com.cioinfotech.cychat.core.ui.list.genericItem
import com.cioinfotech.cychat.core.ui.list.genericItemWithValue
import com.cioinfotech.cychat.core.utils.DimensionConverter
import com.cioinfotech.cychat.features.crypto.verification.epoxy.bottomSheetVerificationActionItem
import com.cioinfotech.cychat.features.settings.VectorPreferences
import me.gujun.android.span.span
import org.matrix.android.sdk.internal.crypto.model.CryptoDeviceInfo
import javax.inject.Inject

class DeviceTrustInfoEpoxyController @Inject constructor(private val stringProvider: StringProvider,
                                                         private val colorProvider: ColorProvider,
                                                         private val dimensionConverter: DimensionConverter,
                                                         private val vectorPreferences: VectorPreferences)
    : TypedEpoxyController<DeviceListViewState>() {

    interface InteractionListener {
        fun onVerifyManually(device: CryptoDeviceInfo)
    }

    var interactionListener: InteractionListener? = null

    override fun buildModels(data: DeviceListViewState?) {
        data?.selectedDevice?.let {
            val isVerified = it.trustLevel?.isVerified() == true
            genericItem {
                id("title")
                style(GenericItem.STYLE.BIG_TEXT)
                titleIconResourceId(if (isVerified) R.drawable.ic_shield_trusted else R.drawable.ic_shield_warning)
                title(
                        stringProvider.getString(
                                if (isVerified) R.string.verification_profile_verified else R.string.verification_profile_warning
                        )
                )
            }
            genericFooterItem {
                id("desc")
                centered(false)
                textColor(colorProvider.getColorFromAttribute(R.attr.riotx_text_primary))
                apply {
                    if (isVerified) {
                        // TODO FORMAT
                        text(stringProvider.getString(R.string.verification_profile_device_verified_because,
                                data.userItem?.displayName ?: "",
                                data.userItem?.id ?: ""))
                    } else {
                        // TODO what if mine
                        text(stringProvider.getString(R.string.verification_profile_device_new_signing,
                                data.userItem?.displayName ?: "",
                                data.userItem?.id ?: ""))
                    }
                }
//                    text(stringProvider.getString(R.string.verification_profile_device_untrust_info))
            }

            genericItemWithValue {
                id(it.deviceId)
                titleIconResourceId(if (isVerified) R.drawable.ic_shield_trusted else R.drawable.ic_shield_warning)
                title(
                        span {
                            +(it.displayName() ?: "")
                            span {
                                text = " (${it.deviceId})"
                                textColor = colorProvider.getColorFromAttribute(R.attr.riotx_text_secondary)
                                textSize = dimensionConverter.spToPx(14)
                            }
                        }
                )
            }

            if (!isVerified) {
                genericFooterItem {
                    id("warn")
                    centered(false)
                    textColor(colorProvider.getColorFromAttribute(R.attr.riotx_text_primary))
                    text(stringProvider.getString(R.string.verification_profile_device_untrust_info))
                }

                bottomSheetVerificationActionItem {
                    id("verify")
                    title(stringProvider.getString(R.string.cross_signing_verify_by_emoji))
                    titleColor(colorProvider.getColor(R.color.riotx_accent))
                    iconRes(R.drawable.ic_arrow_right)
                    iconColor(colorProvider.getColor(R.color.riotx_accent))
                    listener {
                        interactionListener?.onVerifyManually(it)
                    }
                }
            }
        }
    }
}
