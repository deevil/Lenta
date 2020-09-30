package com.lenta.bp12.features.create_task.base.interfaces

/**
 * Базовый интерфейс для viewmodel карточки товара в разделе Создание задания
 * Имплементации:
 * @see BaseGoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.good_info.GoodInfoCreateViewModel
 * @see com.lenta.bp12.features.create_task.marked_good_info.MarkedGoodInfoCreateViewModel
 * */
interface IBaseGoodInfoCreateViewModel {

    fun updateData()
    fun loadBoxInfo(number: String)
}