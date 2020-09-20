package com.lenta.bp12.features.create_task.base.interfaces

import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult
import com.lenta.shared.account.ISessionInfo

/**
 * Интерфейс вьюмодели ответственный за список товаров в Создании задания
 * Имплементации:
 * @see com.lenta.bp12.features.create_task.base.BaseGoodListCreateViewModel
 * @see com.lenta.bp12.features.create_task.task_content.TaskContentViewModel
 * @see com.lenta.bp12.features.basket.basket_good_list.BasketCreateGoodListViewModel
 * */
interface IBaseGoodListCreateViewModel {

    var navigator: IScreenNavigator
    var manager: ICreateTaskManager
    var markManager: IMarkManager
    var sessionInfo: ISessionInfo
    var resource: IResourceManager
    var goodInfoNetRequest: GoodInfoNetRequest
    var database: IDatabaseRepository

    fun getGoodByMaterial(material: String)
    fun checkSearchNumber(number: String)
    fun checkMark(number: String)
    fun getGoodByEan(ean: String)
    fun setFoundGood(foundGood: Good)
    suspend fun loadGoodInfoByEan(ean: String)
    suspend fun loadGoodInfoByMaterial(material: String)
    fun handleLoadGoodInfoResult(result: GoodInfoResult)
    fun setGood(result: GoodInfoResult)
    fun onScanResult(data: String)
    fun onClickDelete()
}