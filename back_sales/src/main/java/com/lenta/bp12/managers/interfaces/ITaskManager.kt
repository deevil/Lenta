package com.lenta.bp12.managers.interfaces

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult

/**
 * Имплементации
 * @see com.lenta.bp12.managers.CreateTaskManager
 * @see com.lenta.bp12.managers.OpenTaskManager
 *
 * Дети:
 * @see ICreateTaskManager
 * @see IOpenTaskManager
 * */

interface ITaskManager{
    var searchNumber: String
    var isSearchFromList: Boolean
    var isWholesaleTaskType: Boolean
    var isBasketsNeedsToBeClosed: Boolean

    val currentGood: MutableLiveData<Good>
    val currentBasket: MutableLiveData<Basket>

    fun addBasket(basket: Basket)
    fun getBasket(providerCode: String, goodToAdd: Good): Basket?

    fun updateCurrentBasket(basket: Basket?)
    fun updateCurrentGood(good: Good?)

    fun saveGoodInTask(good: Good)
    fun findGoodByEan(ean: String): Good?
    fun findGoodByEanAndMRC(ean:String, mrc: String = ""): Good?

    fun findGoodByMaterial(material: String): Good?

    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean

    fun clearCurrentGood()
    fun removeBaskets(basketList: MutableList<Basket>)

    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)

    fun clearSearchFromListParams()

    fun removeMarksFromGoods(mappedMarks: List<Mark>)
}