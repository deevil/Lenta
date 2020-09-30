package com.lenta.bp12.features.open_task.base.interfaces

import com.lenta.bp12.model.pojo.Basket

/**
 * Базовый интерфейс для viewmodel карточки товара в разделе Работа с заданиями
 * Имплементации:
 * @see BaseGoodInfoOpenViewModel
 * @see com.lenta.bp12.features.open_task.marked_good_info.MarkedGoodInfoOpenViewModel
 * @see com.lenta.bp12.features.open_task.good_info.GoodInfoOpenViewModel
 * */
interface IBaseGoodInfoOpenViewModel {

    fun isFactQuantityMoreThanPlanned(): Boolean
    suspend fun getBasket(): Basket?
}