package com.lenta.inventory.models

import com.google.gson.Gson
import com.lenta.inventory.models.memory.MemoryTaskExciseStampRepository
import com.lenta.inventory.models.memory.MemoryTaskProductRepository
import com.lenta.inventory.models.memory.MemoryTaskRepository
import com.lenta.inventory.models.memory.MemoryTaskStorePlaceRepository
import com.lenta.inventory.models.task.*
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PersistInventoryTask @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IPersistInventoryTask {


    private val keyForSave = "INVENTORY_TASK_SAVE_KEY_VALUE"


    override fun saveWriteOffTask(inventoryTask: InventoryTask?) {
        if (inventoryTask == null) {
            hyperHive.stateAPI.saveParamToDB(keyForSave, null)
            return
        }

        PersistInventoryTaskData(
                taskDescription = inventoryTask.taskDescription,
                productInfo = inventoryTask.taskRepository.getProducts().getProducts(),
                untiedProducts = inventoryTask.taskRepository.getProducts().getUntiedProducts(),
                stamps = inventoryTask.taskRepository.getExciseStamps().getExciseStamps(),
                storePlaceInfo = inventoryTask.taskRepository.getStorePlace().getStorePlaces()
        ).let {
            hyperHive.stateAPI.saveParamToDB(keyForSave, gson.toJson(it))
        }
    }

    override fun getSavedWriteOffTask(): InventoryTask? {
        hyperHive.stateAPI.getParamFromDB(keyForSave)?.let { json ->
            gson.fromJson(json, PersistInventoryTaskData::class.java)?.let { persistWriteOffTaskData ->
                return InventoryTask(
                        taskDescription = persistWriteOffTaskData.taskDescription,
                        taskRepository = MemoryTaskRepository(
                                taskProductRepository = MemoryTaskProductRepository(
                                        productInfo = ArrayList(persistWriteOffTaskData.productInfo),
                                        untiedProducts = ArrayList(persistWriteOffTaskData.untiedProducts)
                                ),
                                taskExciseStampRepository = MemoryTaskExciseStampRepository(
                                        stamps = ArrayList(persistWriteOffTaskData.stamps)
                                ),
                                taskStorePlaceRepository = MemoryTaskStorePlaceRepository(
                                        storePlaceInfo = ArrayList(persistWriteOffTaskData.storePlaceInfo)
                                )
                        ))
            }
        }
        return null
    }
}

interface IPersistInventoryTask {
    fun saveWriteOffTask(inventoryTask: InventoryTask?)
    fun getSavedWriteOffTask(): InventoryTask?
}

data class PersistInventoryTaskData(
        val taskDescription: TaskDescription,
        val productInfo: List<TaskProductInfo>,
        val untiedProducts: List<TaskProductInfo>,
        val stamps: List<TaskExciseStamp>,
        val storePlaceInfo: List<TaskStorePlaceInfo>
)