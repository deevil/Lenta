package com.lenta.movement.models

import com.lenta.movement.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.orIfNull
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
    private val printerTable = ZmpUtz26V001(hyperHive).localHelper_ET_PRINTERS
    private val goodsTable = ZmpUtz30V001(hyperHive).localHelper_ET_MATERIALS
    private val taskTypeTable = ZmpUtz102V001(hyperHive).localHelper_ET_MVM_TXT

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
        return pickingStorageTable.all.asSequence()
                .filter {
                    it.taskType == taskType && it.mvmType == movementType
                }
                .map {
                    it.lgortSource
                }
                .toList()
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
        return excludeProductsTable.all.asSequence()
                .filter {
                    it.taskType == task?.taskType
                }
                .any {
                    val goodCodeForPictogram =
                            if (product.isAlco) ALCO_GOOD_CODE_FOR_PICTOGRAM
                            else USUAL_GOOD_CODE_FOR_PICTOGRAM

                    it.taskCntrl == goodCodeForPictogram &&
                            it.ekgrp == product.ekGroup &&
                            it.matkl == product.matkl &&
                            it.mtart == product.materialType
                }
    }

    override fun clear() {
        task = null
    }

    override fun getPrinterName(): String {
        return printerTable.all.first().printerName
    }

    override fun getGoodName(goodNumber: String?): String {
        return goodNumber?.let {
            goodsTable.getWhere(
                    GET_GOODNAME_BY_GOODNUMBER.format(goodNumber)
            )
                    .first()
                    .name
        }.orIfNull {
            Logg.e { "goodNumber null" }
            ""
        }
    }

    override fun getMovementType(movementType: MovementType): String {
        val propertyName = movementType.propertyName
        return taskSettingsTable.getWhere(
                GET_MVM_TYPE_BY_MVM_CODE.format(propertyName)
        )
                .first()
                .annotation
    }

    override fun getMovementTypeShort(movementType: MovementType): String {
        val propertyName = movementType.propertyName
        return taskTypeTable.getWhere(
                GET_MVM_SHORT_TYPE_BY_MVM_CODE.format(propertyName)
        )
                .first()
                .taskTypeTxt.orEmpty()
    }

    override fun getTaskSettings(taskType: TaskType, movementType: MovementType): TaskSettings {
        val gisControls = allowProductsTable.all.asSequence()
                .filter {
                    it.taskType == taskType
                }
                .map {
                    when (it.taskCntrl) {
                        ALCO_GOOD_CODE_FOR_PICTOGRAM -> GisControl.Alcohol
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

    companion object {
        private const val GET_MVM_TYPE_BY_MVM_CODE = "TYPE_MVM = \"%s\""
        private const val GET_MVM_SHORT_TYPE_BY_MVM_CODE = "MVM_TYPE = \"%s\""
        private const val GET_GOODNAME_BY_GOODNUMBER = "MATERIAL = \"%s\""
        private const val ALCO_GOOD_CODE_FOR_PICTOGRAM = "A"
        private const val USUAL_GOOD_CODE_FOR_PICTOGRAM = "N"
    }
}