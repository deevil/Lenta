package com.lenta.bp12.managers.interfaces

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.Taskable
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult

/**
 * Имплементации
 * @see com.lenta.bp12.managers.base.BaseTaskManager
 * @see com.lenta.bp12.managers.CreateTaskManager
 * @see com.lenta.bp12.managers.OpenTaskManager
 *
 * Дети:
 * @see ICreateTaskManager
 * @see IOpenTaskManager
 * */

interface ITaskManager<T : Taskable> {

    var ean: String
    var isWholesaleTaskType: Boolean
    var isBasketsNeedsToBeClosed: Boolean

    val currentGood: MutableLiveData<Good>
    val currentBasket: MutableLiveData<Basket>
    val currentTask: MutableLiveData<T>

    fun addBasket(basket: Basket)
    suspend fun getBasket(providerCode: String, goodToAdd: Good, isSaveToTask: Boolean): Basket?

    suspend fun addOrDeleteGoodToBasket(good: Good, part: Part? = null, provider: ProviderInfo, count: Double)
    suspend fun addGoodToBasketWithMark(good: Good, mark: Mark, provider: ProviderInfo)
    suspend fun addGoodToBasketWithMarks(good: Good, marks: List<Mark>, provider: ProviderInfo)

    fun updateCurrentBasket(basket: Basket?)
    fun updateCurrentGood(good: Good?)

    fun saveGoodInTask(good: Good)
    fun findGoodByEan(ean: String): Good?
    fun findGoodByEanAndMRC(ean: String, mrc: String = ""): Good?

    fun findGoodByMaterial(material: String): Good?

    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean

    fun clearCurrentGood()
    fun removeBaskets(basketList: MutableList<Basket>)

    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)

    fun removeMarksFromGoods(mappedMarks: List<Mark>)

    fun clearEan()

    fun deleteGood(good: Good)
}