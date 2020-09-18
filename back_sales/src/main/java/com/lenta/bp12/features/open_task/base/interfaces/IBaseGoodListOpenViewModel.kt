package com.lenta.bp12.features.open_task.base.interfaces

import com.lenta.bp12.managers.interfaces.IMarkManager
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.shared.account.ISessionInfo

/**
 * Интерфейс вьюмодели списка товаров для Работы с заданиями
 * Имплементации:
 * @see com.lenta.bp12.features.open_task.base.BaseGoodListOpenViewModel
 * @see com.lenta.bp12.features.open_task.good_list.GoodListViewModel
 * @see com.lenta.bp12.features.basket.basket_good_list.BasketOpenGoodListViewModel
 * */
interface IBaseGoodListOpenViewModel {

    var navigator: IScreenNavigator
    var sessionInfo: ISessionInfo
    var manager: IOpenTaskManager
    var markManager: IMarkManager
    var database: IDatabaseRepository
    var resource: IResourceManager
    var goodInfoNetRequest: GoodInfoNetRequest

    fun checkSearchNumber(number: String)
    fun getGoodByEan(ean: String)
    fun setFoundGood(foundGood: Good)
}