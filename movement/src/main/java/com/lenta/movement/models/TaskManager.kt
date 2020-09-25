package com.lenta.movement.models

import android.util.Log
import com.lenta.movement.fmp.resources.fast.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz26V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz30V001
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.utilities.extentions.isSapTrue
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    override suspend fun getAvailableReceivers(): List<String> {
        return withContext(Dispatchers.IO) {
            receiversTable.all
                    .map { table ->
                        table.plant
                    }
                    .distinct()
        }
    }

    override suspend fun getAvailablePikingStorageList(
            taskType: TaskType,
            movementType: MovementType
    ): List<String> {
        return withContext(Dispatchers.IO) {
            pickingStorageTable.all.asSequence()
                    .filter {
                        it.taskType == taskType && it.mvmType == movementType
                    }
                    .map {
                        it.lgortSource
                    }
                    .distinct()
                    .toList()
        }
    }

    override suspend fun isAllowProduct(product: ProductInfo): Boolean {
        return withContext(Dispatchers.IO) {
            allowProductsTable.all.any {
                it.taskType == task?.taskType &&
                        it.taskCntrl == product.type.code &&
                        (it.ekgrp.isEmpty() || it.ekgrp == product.ekGroup) &&
                        (it.matkl.isEmpty() || it.matkl == product.matkl) &&
                        (it.mtart.isEmpty() || it.mtart == product.materialType)
            }
        }
    }

    override suspend fun isDisallowProduct(product: ProductInfo): Boolean {
        val productEkGroup = product.ekGroup
        val productMatkl = product.matkl
        val productMaterialType = product.materialType
        val goodCodeForPictogram =
                if (product.isAlco) ALCO_GOOD_CODE_FOR_PICTOGRAM
                else USUAL_GOOD_CODE_FOR_PICTOGRAM

        return withContext(Dispatchers.IO) {
            excludeProductsTable.all.asSequence()
                    .filter {
                        it.taskType == task?.taskType
                    }
                    .any { inputProduct ->
                        with(inputProduct) {
                            taskCntrl == goodCodeForPictogram &&
                                    ekgrp == productEkGroup &&
                                    matkl == productMatkl &&
                                    mtart == productMaterialType
                        }
                    }
        }
    }

    override fun clear() {
        task = null
    }

    override suspend fun getPrinterName(): String {
        return withContext(Dispatchers.IO) {
            printerTable.all.firstOrNull()?.printerName.orEmpty()
        }
    }

    override suspend fun getGoodName(goodNumber: String?): String {
        return withContext(Dispatchers.IO) {
            goodNumber?.let {
                goodsTable.getWhere(
                        GET_GOODNAME_BY_GOODNUMBER.format(goodNumber)
                )
                        .firstOrNull()
                        ?.name
            }.orEmpty()
        }
    }

    override suspend fun getMovementType(movementType: MovementType): String {
        val propertyName = movementType.propertyName
        return withContext(Dispatchers.IO) {
            taskSettingsTable.getWhere(
                    GET_MVM_TYPE_BY_MVM_CODE.format(propertyName)
            )
                    .firstOrNull()
                    ?.annotation
                    .orEmpty()
        }
    }

    override suspend fun getMovementTypeShort(movementType: MovementType): String {
        val propertyName = movementType.propertyName
        return withContext(Dispatchers.IO) {
            taskTypeTable.getWhere(
                    GET_MVM_SHORT_TYPE_BY_MVM_CODE.format(propertyName)
            )
                    .firstOrNull()
                    ?.taskTypeTxt
                    .orEmpty()
        }
    }

    override suspend fun getTaskSettings(taskType: TaskType, movementType: MovementType): TaskSettings {
        return withContext(Dispatchers.IO) {
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

            if (results.firstOrNull()?.divMarkParts.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.MARK_PARTS)
            }

            if (results.firstOrNull()?.divAlco.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.ALCO)
            }

            if (results.firstOrNull()?.divUsual.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.USUAL)
            }

            if (results.firstOrNull()?.divVet.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.VET)
            }

            if (results.firstOrNull()?.divParts.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.PARTS)
            }

            if (results.firstOrNull()?.divMtart.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.MTART)
            }

            if (results.firstOrNull()?.divFood.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.FOOD)
            }

            if (results.firstOrNull()?.divLifnr.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.LIF_NUMBER)
            }

            if (results.firstOrNull()?.divMatnr.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.MATERIAL_NUMBER)
            }

            if (results.firstOrNull()?.divAbtnr.isSapTrue()) {
                signOfDivision.add(GoodsSignOfDivision.SECTION)
            }

            TaskSettings(
                    description = results.firstOrNull()?.annotation.orEmpty(),
                    shipmentStorageList = results.map { it.lgortTarget }.distinct(),
                    signsOfDiv = signOfDivision,
                    gisControls = gisControls
            )

        }
    }

    companion object {
        private const val GET_MVM_TYPE_BY_MVM_CODE = "TYPE_MVM = \"%s\""
        private const val GET_MVM_SHORT_TYPE_BY_MVM_CODE = "MVM_TYPE = \"%s\""
        private const val GET_GOODNAME_BY_GOODNUMBER = "MATERIAL = \"%s\""
        private const val ALCO_GOOD_CODE_FOR_PICTOGRAM = "A"
        private const val USUAL_GOOD_CODE_FOR_PICTOGRAM = "N"
    }
}