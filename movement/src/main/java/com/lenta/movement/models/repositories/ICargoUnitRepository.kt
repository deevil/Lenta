package com.lenta.movement.models.repositories

import com.lenta.movement.models.CargoUnit
import com.lenta.movement.models.ProcessingUnit


/** Интерфейс класса который хранит данные о ЕО и ГЕ задачи */
interface ICargoUnitRepository {

    fun setEOAndGE(
            inputEoList: List<ProcessingUnit>,
            inputGeList: MutableList<CargoUnit>,
            inputTaskNumber: Int)
    fun setEO(eoListToAdd: List<ProcessingUnit>)
    fun setGE(geList: MutableList<CargoUnit>)
    fun getGEList() : MutableList<CargoUnit>
    fun getEOList() : List<ProcessingUnit>
    fun getSelectedGE(position : Int) : CargoUnit
    fun getGEbyItsNumber(geNumber: String) : CargoUnit
    fun updateGE(geToUpdate : CargoUnit)
    fun clear()
}