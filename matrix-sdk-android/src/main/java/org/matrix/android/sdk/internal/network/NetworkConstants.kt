/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.android.sdk.internal.network

import retrofit2.http.DELETE

object NetworkConstants {

    // Homeserver
    private const val URI_API_PREFIX_PATH = "_matrix/client"
    const val URI_API_PREFIX_PATH_ = "$URI_API_PREFIX_PATH/"
    const val URI_API_PREFIX_PATH_R0 = "$URI_API_PREFIX_PATH/r0/"
    const val URI_API_PREFIX_PATH_UNSTABLE = "$URI_API_PREFIX_PATH/unstable/"

    // Media
    private const val URI_API_MEDIA_PREFIX_PATH = "_matrix/media"
    const val URI_API_MEDIA_PREFIX_PATH_R0 = "$URI_API_MEDIA_PREFIX_PATH/r0/"

    // Identity server
    const val URI_IDENTITY_PREFIX_PATH = "_matrix/identity/v2"
    const val URI_IDENTITY_PATH_V2 = "$URI_IDENTITY_PREFIX_PATH/"

    // Push Gateway
    const val URI_PUSH_GATEWAY_PREFIX_PATH = "_matrix/push/v1/"

    // Integration
    const val URI_INTEGRATION_MANAGER_PATH = "_matrix/integrations/v1/"

    // Federation
    const val URI_FEDERATION_PATH = "_matrix/federation/v1/"

    const val DEV_URL = "https://cychat-dev.cioinfotech.com"
    const val QA_URL = "https://cyberiaqa-api.cioinfotech.com"
    const val UAT_URL = "https://cychat-ct.cioinfotech.com"
    const val PRODUCTION_URL = "https://cyverse-production1.cioinfotech.com"
    const val CENTRAL_SERVER_URL = "https://central.cyverse.co.za"
//    const val CENTRAL_SERVER_URL = "https://cyverse.api.cioinfotech.com"
    const val DEV = "Development"
    const val QA = "QA"
    const val UAT = "UAT"
    const val PRODUCTION = "PRODUCTION"
    // Cy Central Server APIs

    const val ROOT_API = "/api/root_api.php"
    const val GET_GROUPS_API = "getGroups"
    const val GET_USER_TYPE_API = "getOrgUserTypes"
    const val GET_IND_TYPE_API = "getIndType"
    const val GET_ORGANIZATION_API = "getOrgTypes"
    const val GET_SETTINGS_API = "getSettings"
    const val RE_CHECK_REF_CODE = "reCheckRefCode"
    const val VALIDATE_CODE = "validateCode"
    const val CHECK_CODE_API = "checkCode"
    const val CHECK_OTP_API = "checkOtp"
    const val USER_LOGIN_API = "userLogin"
    const val RESENT_OTP_API = "resendOtp"
    const val SET_SECRET_KEY_API = "setSecretKey"
    const val DELETE_SESSION_API ="deleteSessions"
    const val OP_SET_NAME = "setName"
    const val GET_COMPANY_DETAILS_API = "getCompanyNameAndLogo"
    const val GET_ADD_USER_TYPES = "getAddUserTypes"
    const val VERIFY_ADD_USER_TYPE = "verifyAddUserType"
    const val VERIFY_OTP = "verifyOTP"
    const val GET_USER_PROFILE = "getUserProfileDetails"
    const val SET_VISIBILITY = "setVisible"
    const val RESEND_VERIFICATION_CODE = "resendIndividualCode"
    const val DELETE_REQUEST = "deleteRequest"
    const val LIST_FEDERATED_API = "listFederated"
    const val SEARCH_USER_API = "searchUsers"
    const val GENERAL_DATA = "general_data"
    const val FEDERATION = "federation"
    const val CY_VERSE_ANDROID = "cyVerseAndroid"
    const val GET_USER_PLUGINS = "getUserPlugins"

    const val LIVE = "LIVE"
    const val OP = "op"
    const val SERVICE_NAME = "serviceName"
    const val CLIENT_NAME = "clientName"
    const val USER_TYPE = "userType"
    const val MATRIX_URL ="matrixURL"
    const val USER_TYPE_NAME = "userTypeName"
    const val EXCLUDE_USER_TYPE = "excludeUserType"
    const val REF_CODE = "refCode"
    const val CLID = "clid"
    const val SEARCH_TERM = "searchTerm"
    const val GROUP_VALUE = "groupValue"
    const val INDIVIDUAL = "Individual"
    const val NONE = "None"
    const val COMMON = "Common"
    const val USERTYPE_DATA = "usertype_data"
    const val PLUGINS = "plugins"
    const val MISC_FUNC = "misc_functions"
    const val SETUP_ID = "setupID"
    const val GET_SETTINGS = "get_settings"
    const val CY_VERSE_API_CLID = "96e9a8b6be83be41955ab39c8e738c2c"
    const val USER_CAT_ID = "userCatID"

    const val GET_NOTICE_BOARDS = "fetchPostDetails"
    const val UPDATE_POST_DETAILS = "updatePostDetails"
    const val GET_NOTICES = "getPostsList"
    const val GET_TIMEZONES = "getTimeZones"
    const val POST_DATA = "post_data"
    const val EDIT_POSTS = "edit_posts"
    const val UPLOAD_MEDIA = "uploadMedia"

    // Cy Chat APIs
    const val LOGIN = "login"
    const val USER_LOGIN = "/user-login"

    const val VALIDATE_SECURITY_CODE = "/val-secr-code"
    const val GET_DOMAIN_DETAILS = "/get-company-name-and-logo"
    const val CY_CHAT_ENV = "CY_CHAT_ENV"
    const val BASE_URL = "BASE_URL"
    const val U_TYPE_NAME = "U_TYPE_NAME"
    const val U_TYPE_MODE = "U_TYPE_MODE"
    const val U_REG_TITLE = "U_REG_TITLE"
    const val U_TYPE_MODE_INDIVIDUAL = "Common"
    const val USER_ID = "userID"
    const val POST_ID = "postID"
    const val EMAIL = "EMAIL"
    const val ACCESS_TOKEN_SMALL = "access_token"
    const val REQ_ID = "reqID"
    const val JITSI = "JITSI"
    const val SYGNAL = "SYGNAL"

    const val DOMAIN_NAME = "DOMAIN_NAME"
    const val DOMAIN_IMAGE = "DOMAIN_IMAGE"
    const val APP_NAME = "APP_NAME"
    const val SECRET_KEY = "SECRET_KEY"
    const val API_SERVER = "API_SERVER"
    const val SECRET_CODE_SMALL = "secretCode"
    const val SIGN_UP_SMALL = "sign-up"
    const val SIGN_IN_SMALL = "sign-in"
    const val SIGNING_MODE = "SIGNING_MODE" // true - Sign Up & false - Sign In
    const val FULL_NAME = "FULL_NAME"
    const val DEVICE_ID = "dev_id"
    const val CURRENT_DOMAIN = "curr_domain"
    const val SESSION_UPDATED = "SESSION_UPDATED"
    const val MOBILE = "mobile"
    const val EMAIL_SMALL = "email"
    const val IMEI = "imei"
    const val COUNTRY_CODE = "countryCode"
    const val CODE = "code"
    const val F_NAME = "firstName"
    const val L_NAME = "lastName"
    const val TYPE = "type"
    const val EMAIL_OTP = "emailOtp"
    const val MOBILE_OTP = "mobileOtp"
    const val OTP = "otp"
    const val USER_ROLE_ID = "userRoleID"
    const val EMAIL_VAL = "emailVal"

    const val ACTIVE = "active"
    const val FILTERS = "filters"
    const val FETCH_COUNT = "fetchCount"
    const val LAST_POST = "lastPost"
    const val TEXT_BEFORE = "textBefore"
    const val TEXT_AFTER = "textAfter"
    const val POST_TITLE = "postTitle"
    const val BOARD_ID = "boardID"
    const val POST_STATUS = "postStatus"
    const val POST_GET_ALL = "All"
    const val POST_GET_MINE = "Mine"
    const val POST_STATUS_TYPE_DRAFT = "Draft"
    const val POST_STATUS_TYPE_PUBLISH = "Published"
    const val EDIT_POST_YES = "Yes"
    const val EDIT_POST_NO = "No"
    const val EVENT_VENUE = "eventVenue"
    const val EVENT_START = "eventStart"
    const val EVENT_END = "eventEnd"
    const val END_DATE = "endDate"
    const val POST_TYPE = "postType"
    const val TIMEZONE = "timeZone"

    const val MEDIA_IMAGE = "image"
    const val MEDIA_ATTACHMENT = "attachment"
    const val TIME_ZONES = "time_zones"
    const val EVENT_ONLINE = "event-online"
    const val EVENT_LIVE = "event-live"
    const val POST = "post"
    const val JWT_TOKEN_JITSI ="https://instance01.cyverse.co.za/api/root_api.php/"

}
