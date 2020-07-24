package com.lenta.bp18.features.other

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp18.data.model.CheckData
import com.lenta.bp18.model.pojo.Good
import com.lenta.bp18.platform.navigation.IScreenNavigator
import com.lenta.bp18.repository.IDatabaseRepo
import com.lenta.bp18.request.model.GoodInfoParams
import com.lenta.bp18.request.model.GoodInfoResult
import com.lenta.bp18.request.network.GoodInfoNetRequest
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class SendDataViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var database: IDatabaseRepo
    @Inject
    lateinit var checkData: CheckData
    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    val good: MutableLiveData<Good> = MutableLiveData()
    val ean: MutableLiveData<String> = MutableLiveData()

/*    fun sendCheckResult() {
        viewModelScope.launch {
            val goodInfoParams = "0"
            val goodInfoParams = GoodInfoParams(
                    marketNumber = checkData.getFormattedMarketNumber(),
                    sapCode =
            )

            val saveRequest = goodInfoNetRequest

            saveRequest.let {request ->
                navigator.showProgress(request)
                request(goodInfoParams).either(::handleDataSendingError, ::handleDataSendingSuccess)
                navigator.hideProgress()
            }
        }

    }

    private fun handleDataSendingError(failure: Failure) {
        // Сообщение - Ошибка нет доступа к серверу
        navigator.showAlertServerNotAvailable() {
            navigator.openGoodsInfoScreen()
        }
    }

    private fun handleDataSendingSuccess(goodInfoResult: GoodInfoResult) {
        // Сообщение - Сохранение успешно
        navigator.showAlertSuccessfulOpeningPackage {
            //checkData.removeAllFinishedSegments()
            navigator.openSelectGoodScreen()
        }
    }*/

}