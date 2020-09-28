package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName

data class UserPermissionsParams(
        @SerializedName("IV_UNAME")
        val userName: String
)