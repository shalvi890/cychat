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

package com.cioinfotech.cychat.features.call.conference

import android.content.Context
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.di.DefaultSharedPreferences
import com.cioinfotech.cychat.core.network.await
import com.cioinfotech.cychat.core.resources.StringProvider
import com.cioinfotech.cychat.core.utils.ensureProtocol
import com.cioinfotech.cychat.core.utils.toBase32String
import com.cioinfotech.cychat.features.JitsiRequest
import com.cioinfotech.cychat.features.call.conference.api.JWTApi
import com.cioinfotech.cychat.features.call.conference.jwt.JitsiJWTFactory
import com.cioinfotech.cychat.features.home.room.detail.RoomDetailViewModel.Companion.TOKEN
import com.cioinfotech.cychat.features.plugins.model.JWTTokenModel
import com.cioinfotech.cychat.features.plugins.model.PluginListParentModel
import com.cioinfotech.cychat.features.raw.wellknown.ElementWellKnown
import com.cioinfotech.cychat.features.raw.wellknown.ElementWellKnownMapper
import com.cioinfotech.cychat.features.settings.VectorLocale
import com.cioinfotech.cychat.features.themes.ThemeProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import org.matrix.android.sdk.api.MatrixPatterns.getDomain
import org.matrix.android.sdk.api.auth.data.SessionParams
import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.api.raw.RawService
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.widgets.model.Widget
import org.matrix.android.sdk.api.session.widgets.model.WidgetType
import org.matrix.android.sdk.api.util.appendParamToUrl
import org.matrix.android.sdk.api.util.toMatrixItem
import org.matrix.android.sdk.internal.di.MoshiProvider
import org.matrix.android.sdk.internal.network.NetworkConstants
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL
import java.util.UUID
import javax.inject.Inject

class JitsiService @Inject constructor(
        private val session: Session,
        private val rawService: RawService,
        stringProvider: StringProvider,
        private val themeProvider: ThemeProvider,
        private val jitsiJWTFactory: JitsiJWTFactory,
        context: Context) {

    companion object {
        const val JITSI_OPEN_ID_TOKEN_JWT_AUTH = "openidtoken-jwt"
    }

    private val pref = DefaultSharedPreferences.getInstance(context)
    private val defaultURL = stringProvider.getString(R.string.preferred_jitsi_domain)
    private val jitsiURL = pref.getString(NetworkConstants.JITSI, defaultURL) ?: defaultURL

    private val jitsiWidgetDataFactory by lazy {
        JitsiWidgetDataFactory(jitsiURL) { widget ->
            session.widgetService().getWidgetComputedUrl(widget, themeProvider.isLightTheme())
        }
    }

    fun getJWTToken(op:String, serviceName:String, clientName :String ,meetingName:String ,clied:String): Call<
            JsonObject> {
        return buildCyCoreAPI(NetworkConstants.JWT_TOKEN_JITSI).getJWTToken(op,serviceName,clientName, meetingName ,clied)
    }

    suspend fun createJitsiWidget(roomId: String, withVideo: Boolean): Widget {
        // Build data for a jitsi widget
        val widgetId: String = WidgetType.Jitsi.preferred + "_" + session.myUserId + "_" + System.currentTimeMillis()
        val preferredJitsiDomain = tryOrNull {
            rawService.getElementWellknown(session.sessionParams)
                    ?.jitsiServer
                    ?.preferredDomain
        }
        val jitsiDomain = preferredJitsiDomain ?: jitsiURL
        val jitsiAuth = getJitsiAuth(jitsiDomain)
        val confId = createConferenceId(roomId, jitsiAuth)

        // We use the default element wrapper for this widget
        // https://github.com/vector-im/element-web/blob/develop/docs/jitsi-dev.md
        // https://github.com/matrix-org/matrix-react-sdk/blob/develop/src/utils/WidgetUtils.ts#L469
        val url = buildString {
            append("https://app.element.io/jitsi.html")
            appendParamToUrl("confId", confId)
            append("#conferenceDomain=\$domain")
            append("&conferenceId=\$conferenceId")
            append("&isAudioOnly=\$isAudioOnly")
            append("&displayName=\$matrix_display_name")
            append("&avatarUrl=\$matrix_avatar_url")
            append("&userId=\$matrix_user_id")
            append("&roomId=\$matrix_room_id")
            append("&theme=\$theme")
            if (jitsiAuth != null) {
                append("&auth=$jitsiAuth")
            }
        }
        val widgetEventContent = mapOf(
                "url" to url,
                "type" to WidgetType.Jitsi.legacy,
                "data" to JitsiWidgetData(jitsiDomain, confId, !withVideo, jitsiAuth),
                "creatorUserId" to session.myUserId,
                "id" to widgetId,
                "name" to "jitsi"
        )
        return session.widgetService().createRoomWidget(roomId, widgetId, widgetEventContent)
    }

    suspend fun joinConference(roomId: String, jitsiWidget: Widget, enableVideo: Boolean): JitsiCallViewEvents.JoinConference {
        val me = session.getRoomMember(session.myUserId, roomId)?.toMatrixItem()
        val userDisplayName = me?.getBestName()
        val userAvatar = me?.avatarUrl?.let { session.contentUrlResolver().resolveFullSize(it)
        }
        val userInfo = JitsiMeetUserInfo().apply {
            this.displayName = userDisplayName
            this.avatar = userAvatar?.let { URL(it) }
        }
        val roomName = session.getRoomSummary(roomId)?.displayName
        val widgetData = jitsiWidgetDataFactory.create(jitsiWidget)
        return JitsiCallViewEvents.JoinConference(
                enableVideo = enableVideo,
                jitsiUrl = widgetData.domain.ensureProtocol(),
                subject = roomName ?: "",
                confId = roomName ?: "",
                userInfo = userInfo,
                token = TOKEN
        )
    }

    fun extractJitsiWidgetData(widget: Widget): JitsiWidgetData? {
        return tryOrNull {
            jitsiWidgetDataFactory.create(widget)
        }
    }

    private fun JitsiWidgetData.isOpenIdJWTAuthenticationRequired(): Boolean {
        return auth == JITSI_OPEN_ID_TOKEN_JWT_AUTH
    }

    private suspend fun getOpenIdJWTToken(roomId: String, domain: String, userDisplayName: String, userAvatar: String): String {
        val openIdToken = session.openIdService().getOpenIdToken()
        return jitsiJWTFactory.create(
                openIdToken = openIdToken,
                jitsiServerDomain = domain,
                roomId = roomId,
                userAvatarUrl = userAvatar,
                userDisplayName = userDisplayName
        )
    }

    private fun createConferenceId(roomId: String, jitsiAuth: String?): String {
        return if (jitsiAuth == JITSI_OPEN_ID_TOKEN_JWT_AUTH) {
            // Create conference ID from room ID
            // For compatibility with Jitsi, use base32 without padding.
            // More details here:
            // https://github.com/matrix-org/prosody-mod-auth-matrix-user-verification
            roomId.toBase32String(padding = false)
        } else {
            // Create a random enough jitsi conference id
            // Note: the jitsi server automatically creates conference when the conference
            // id does not exist yet
            var widgetSessionId = UUID.randomUUID().toString()
            if (widgetSessionId.length > 8) {
                widgetSessionId = widgetSessionId.substring(0, 7)
            }
            roomId.substring(1, roomId.indexOf(":") - 1) + widgetSessionId.lowercase(VectorLocale.applicationLocale)
        }
    }

    private suspend fun getJitsiAuth(jitsiDomain: String): String? {
        val request = Request.Builder().url("$jitsiDomain/.well-known/element/jitsi".ensureProtocol()).build()
        return tryOrNull {
            val response = session.getOkHttpClient().newCall(request).await()
            val json = response.body?.string() ?: return null
            MoshiProvider.providesMoshi().adapter(JitsiWellKnown::class.java).fromJson(json)?.auth
        }
    }

     //fun getPlugins(hashMap: HashMap<String, String>, url: String, clid: String) = buildCyCoreAPI(url).getUserPlugins(clid,hashMap)

    private fun buildCyCoreAPI(url: String): JWTApi {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.apply {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }
         val client = OkHttpClient.Builder()
                 .addNetworkInterceptor(httpLoggingInterceptor).build()

        return createWithBaseURL(client, url).create(JWTApi::class.java)
    }


}

fun createWithBaseURL(okHttpClient: OkHttpClient, baseUrl: String): Retrofit {
    val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
    return Retrofit.Builder()
            .baseUrl(baseUrl.ensureProtocol())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
}

suspend fun RawService.getElementWellknown(sessionParams: SessionParams): ElementWellKnown? {
    // By default we use the domain of the userId to retrieve the .well-known data
    val domain = sessionParams.userId.getDomain()
    return tryOrNull { getWellknown(domain) }
            ?.let { ElementWellKnownMapper.from(it) }
}


fun ElementWellKnown.isE2EByDefault() = elementE2E?.e2eDefault ?: riotE2E?.e2eDefault ?: true

