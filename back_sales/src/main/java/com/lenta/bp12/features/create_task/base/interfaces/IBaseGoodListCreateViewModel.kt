package com.lenta.bp12.features.create_task.base.interfaces

import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.request.pojo.good_info.GoodInfoResult

/**
 * Интерфейс вьюмодели ответственный за список товаров в Создании задания
 * Имплементации:
 * @see com.lenta.bp12.features.create_task.base.BaseGoodListCreateViewModel
 * @see com.lenta.bp12.features.create_task.task_content.TaskContentViewModel
 * @see com.lenta.bp12.features.basket.basket_good_list.BasketCreateGoodListViewModel
 * */
interface IBaseGoodListCreateViewModel {

    var manager: ICreateTaskManager
    fun getGoodByMaterial(material: String)
    fun checkMark(number: String)
    suspend fun loadGoodInfoByEan(ean: String)
    suspend fun loadGoodInfoByMaterial(material: String)
    fun handleLoadGoodInfoResult(result: GoodInfoResult)
    fun setGood(result: GoodInfoResult)
    fun onScanResult(data: String)
    fun onClickDelete()
}