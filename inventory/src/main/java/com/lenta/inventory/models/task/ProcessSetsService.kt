package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository

class ProcessSetsService(private val taskDescription: TaskDescription,
                         private val taskRepository: ITaskRepository,
                         private val productInfo: TaskProductInfo) : IProcessProductService {

    override fun getFactCount(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setFactCount(count: Double){
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markMissing(){
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}