package com.lenta.bp7.features.option

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.viewModelScope
import com.lenta.bp7.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class OptionViewModel : CoreViewModel() {

    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo

    private val zmpUtz23V001: ZmpUtz23V001 by lazy { ZmpUtz23V001(hyperHive) }
    private val zmpUtz14V001: ZmpUtz14V001 by lazy { ZmpUtz14V001(hyperHive) }

    var isFacings = ObservableBoolean()
    var isPlaces = ObservableBoolean()

    companion object {
        const val HYPER = "H"
        const val SUPER = "S"
        const val TURNED_OFF = "NO"
    }

    init {
        viewModelScope.launch {
            when (sessionInfo.market?.let { zmpUtz23V001.getRetailType(it) }) {
                HYPER -> {
                    isFacings.set(zmpUtz14V001.getFacingsHyperParam() != TURNED_OFF)
                    isPlaces.set(zmpUtz14V001.getPlacesHyperParam() != TURNED_OFF)
                }
                SUPER -> {
                    isFacings.set(zmpUtz14V001.getFacingsSuperParam() != TURNED_OFF)
                    isPlaces.set(zmpUtz14V001.getPlacesSuperParam() != TURNED_OFF)
                }
                else -> {
                    Logg.d { "Тип магазина неизвестен." }
                }
            }
        }
    }

    fun onClickNext() {
        // Перейти к следующему экрану
        //navigator.openSomething()
    }
}
