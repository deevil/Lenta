package com.lenta.movement.models.memory


import com.lenta.movement.models.CargoUnit
import com.lenta.movement.models.ProcessingUnit
import com.lenta.movement.models.repositories.ICargoUnitRepository

/** Реализация интерфейса класса который хранит данные о ЕО и ГЕ задачи */
class MemoryCargoUnitRepository : ICargoUnitRepository {

    private val eoList = mutableListOf<ProcessingUnit>()
    private val geList = mutableListOf<CargoUnit>()
    private var taskNumber = -1

    override fun setEOAndGE(
            inputEoList: List<ProcessingUnit>,
            inputGeList: MutableList<CargoUnit>,
            inputTaskNumber: Int) {
        if (taskNumber != inputTaskNumber) {
            eoList.clear()
            eoList.addAll(inputEoList)

            geList.clear()
            geList.addAll(inputGeList)

            taskNumber = inputTaskNumber
        }
    }

    override fun setEO(eoListToAdd: List<ProcessingUnit>) {
        this.eoList.clear()
        this.eoList.addAll(eoListToAdd.toMutableList())
    }

    override fun setGE(geList: MutableList<CargoUnit>) {
        this.geList.clear()
        this.geList.addAll(geList)
    }

    override fun getGEList(): MutableList<CargoUnit> {
        return geList
    }

    override fun getEOList(): List<ProcessingUnit> {
        return eoList
    }

    override fun getSelectedGE(position: Int): CargoUnit {
        return geList[position]
    }

    override fun getGEbyItsNumber(geNumber: String): CargoUnit {
        val newGe = geList.first { it.number == geNumber }
        newGe.eoList.clear()
        newGe.eoList.addAll(
                eoList.filter { it.cargoUnitNumber == geNumber }
        )
        return newGe
    }

    override fun updateGE(geToUpdate: CargoUnit) {
            updateEoListInsideGeList(geToUpdate)
            excludeEoFromGeAndChangeItsState(geToUpdate)
    }

    private fun updateEoListInsideGeList(geToUpdate: CargoUnit){
        geList.find { ge ->
            ge.number == geToUpdate.number
        }?.let { ge ->
            val newGe = CargoUnit(
                    number = ge.number,
                    eoList = geToUpdate.eoList
            )
            val index = geList.indexOf(ge)
            geList[index] = newGe
        }
    }

    private fun excludeEoFromGeAndChangeItsState(geToUpdate: CargoUnit){
        val filteredEoList = eoList.filter { eo ->
            eo.cargoUnitNumber == geToUpdate.number && !geToUpdate.eoList.contains(eo)
        }
        val mappedFilteredEoList = filteredEoList.map {
            it.apply {
                cargoUnitNumber = null
                state = ProcessingUnit.State.NOT_PROCESSED
            }
        }
        eoList.removeAll(filteredEoList)
        eoList.addAll(mappedFilteredEoList)
    }

    override fun clear() {
        eoList.clear()
        geList.clear()
    }
}