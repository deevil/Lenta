package com.lenta.bp18.features.select_goods

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.map

class SelectGoodsViewModel : CoreViewModel() {

    lateinit var navigator: IScreenNavigator

    lateinit var sessionInfo: ISessionInfo

    lateinit var appSettings: IAppSettings

}