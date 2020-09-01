package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult

interface ITaskManager{
    var searchNumber: String
    var isSearchFromList: Boolean
    var isWholesaleTaskType: Boolean
    var isBasketsNeedsToBeClosed: Boolean

    val currentBasket: MutableLiveData<Basket>

    fun addBasket(basket: Basket)
    fun getBasket(providerCode: String): Basket?

    fun updateCurrentBasket(basket: Basket?)

    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean

    fun clearCurrentGood()
    fun removeBaskets(basketList: MutableList<Basket>)

    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)

    fun clearSearchFromListParams()
}