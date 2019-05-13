package com.lenta.bp10.account

import com.lenta.shared.account.ISessionInfo

data class SessionInfo(
        override var userName: String? = null,
        override var personnelNumber: String? = null,
        override var printer: String? = null,
        override var market: String? = null
) : ISessionInfo