package com.lenta.movement.models

import com.lenta.movement.fmp.resources.fast.*
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.utilities.extentions.isSapTrue
import com.mobrun.plugin.api.HyperHive

@Suppress("INACCESSIBLE_TYPE")
class TaskManager(
    hyperHive: HyperHive
) : ITaskManager {

    private val taskSettingsTable = ZmpUtz47V001(hyperHive).localHelper_ET_TASK_TPS
    private val pickingStorageTable = ZmpUtz48V001(hyperHive).localHelper_ET_LGORT_SRC
    private val allowProductsTable = ZmpUtz49V001(hyperHive).localHelper_ET_ALLOW_MATNR
    private val excludeProductsTable = ZmpUtz50V001(hyperHive).localHelper_ET_EXCLUDE_MATNR
    private val receiversTable = ZmpUtz79V001(hyperHive).localHelper_ET_PLANTS

    private var task: Task? = null

    override fun getTask(): Task {
        return task ?: error("Task not defined!")
    }

    override fun getTaskOrNull(): Task? {
        return task
    }

    override fun setTask(task: Task) {
        this.task = task
    }

    override fun getAvailableReceivers(): List<String> {
        return receiversTable.all
            .map { table ->
                table.plant
            }
            .distinct()
    }

    override fun getAvailablePikingStorageList(
        taskType: TaskType,
        movementType: MovementType
    ): List<String> {
        return pickingStorageTable.all
            .filter {
                it.taskType == taskType && it.mvmType == movementType
            }
            .map {
                it.lgortSource
            }
            .distinct()
    }

    override fun isAllowProduct(product: ProductInfo): Boolean {
        return allowProductsTable.all.any {
            it.taskType == task?.taskType &&
                    it.taskCntrl == task?.movementType?.propertyName &&
                    it.ekgrp == product.ekGroup &&
                    it.matkl == product.matkl &&
                    it.mtart == product.materialType
        }
    }

    override fun isDisallowProduct(product: ProductInfo): Boolean {
        return excludeProductsTable.all
            .filter {
                it.taskType == task?.taskType
            }
            .any {
                it.taskCntrl == (if (product.isAlco) "A" else "N") &&
                        it.ekgrp == product.ekGroup &&
                        it.matkl == product.matkl &&
                        it.mtart == product.materialType
            }
    }

    override fun clear() {
        task = null
    }

    override fun getTaskSettings(taskType: TaskType, movementType: MovementType): TaskSettings {
        val gisControls = allowProductsTable.all
            .filter {
                it.taskType == taskType
            }
            .map {
                when (it.taskCntrl) {
                    "A" -> GisControl.Alcohol
                    else -> GisControl.GeneralProduct
                }
            }.toSet()

        val results = taskSettingsTable.all.filter {
            it.taskType == taskType && it.mvmType == movementType
        }

        val signOfDivision = mutableSetOf<GoodsSignOfDivision>()

        if (results.first().divMarkParts.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.MARK_PARTS)
        }

        if (results.first().divAlco.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.ALCO)
        }

        if (results.first().divUsual.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.USUAL)
        }

        if (results.first().divVet.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.VET)
        }

        if (results.first().divParts.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.PARTS)
        }

        if (results.first().divMtart.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.MTART)
        }

        if (results.first().divFood.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.FOOD)
        }

        if (results.first().divLifnr.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.LIF_NUMBER)
        }

        if (results.first().divMatnr.isSapTrue()) {
            signOfDivision.add(GoodsSignOfDivision.MATERIAL_NUMBER)
        }

        return TaskSettings(
            description = results.first().annotation,
            shipmentStorageList = results.map { it.lgortTarget }.distinct(),
            //signsOfDiv = signOfDivision.toSet()
            signsOfDiv = setOf(
                GoodsSignOfDivision.ALCO,
                GoodsSignOfDivision.VET,
                GoodsSignOfDivision.FOOD
            ),
            gisControls = gisControls
        )
    }
}