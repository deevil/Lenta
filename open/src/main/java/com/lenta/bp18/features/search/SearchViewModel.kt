package com.lenta.bp18.features.search

import androidx.lifecycle.ViewModel
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings

class SearchViewModel : CoreViewModel() {

    lateinit var navigator: IScreenNavigator

    lateinit var sessionInfo: ISessionInfo

    lateinit var appSettings: IAppSettings


}