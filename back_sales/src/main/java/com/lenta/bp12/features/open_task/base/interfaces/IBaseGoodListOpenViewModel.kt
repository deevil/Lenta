package com.lenta.bp12.features.open_task.base.interfaces

import com.lenta.bp12.managers.interfaces.IOpenTaskManager
/**
 * Интерфейс вьюмодели списка товаров для Работы с заданиями
 * Имплементации:
 * @see com.lenta.bp12.features.open_task.base.BaseGoodListOpenViewModel
 * @see com.lenta.bp12.features.open_task.good_list.GoodListViewModel
 * @see com.lenta.bp12.features.basket.basket_good_list.BasketOpenGoodListViewModel
 * */
interface IBaseGoodListOpenViewModel {
    var manager: IOpenTaskManager
}