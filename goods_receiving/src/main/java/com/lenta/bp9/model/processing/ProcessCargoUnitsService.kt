package com.lenta.bp9.model.processing

import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.shared.di.AppScope
import javax.inject.Inject

@AppScope
class ProcessCargoUnitsService
@Inject constructor() {
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    private var cargoUnitsInfo: ArrayList<TaskCargoUnitInfo> = ArrayList()

    fun newProcessCargoUnitsService(cargoUnitsInfo: List<TaskCargoUnitInfo>) : ProcessCargoUnitsService {
        this.cargoUnitsInfo.clear()
        cargoUnitsInfo.map {
            this.cargoUnitsInfo.add(it.copy())
        }
        return this
    }

    fun getCargoUnits() : List<TaskCargoUnitInfo> {
        return cargoUnitsInfo
    }

    fun findCargoUnit(cargoUnitNumber: String) : TaskCargoUnitInfo? {
        return cargoUnitsInfo.findLast {
            it.cargoUnitNumber == cargoUnitNumber
        }
    }

    fun apply(newCargoUnitInfo: TaskCargoUnitInfo, cargoUnitStatus: String, palletType: String) {
        var index = -1
        for (i in cargoUnitsInfo.indices) {
            if (newCargoUnitInfo.cargoUnitNumber == cargoUnitsInfo[i].cargoUnitNumber) {
                index = i
            }
        }

        if (index == -1) {
            cargoUnitsInfo.add(newCargoUnitInfo.copy(cargoUnitStatus = cargoUnitStatus, palletType = palletType))
        } else {
            val updatedCargoUnit = cargoUnitsInfo[index].copy(cargoUnitStatus = cargoUnitStatus, palletType = palletType)
            cargoUnitsInfo.removeAt(index)
            cargoUnitsInfo.add(updatedCargoUnit)
        }
    }

    fun delete(newCargoUnitInfo: TaskCargoUnitInfo) {
        var index = -1
        for (i in cargoUnitsInfo.indices) {
            if (newCargoUnitInfo.cargoUnitNumber == cargoUnitsInfo[i].cargoUnitNumber) {
                index = i
            }
        }

        if (index != -1) {
            cargoUnitsInfo.removeAt(index)
        }

    }

    fun save() {
        taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.updateCargoUnits(cargoUnitsInfo)
    }

}