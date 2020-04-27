package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskExciseStampBad

interface ITaskExciseStampBadRepository {
    fun getExciseStampsBad(): List<TaskExciseStampBad>
    fun findExciseStampBad(findExciesStampBad: TaskExciseStampBad): TaskExciseStampBad?
    fun addExciseStampBad(addExciesStampBad: TaskExciseStampBad): Boolean
    fun updateExciseStampBad(newExciesStampBad: List<TaskExciseStampBad>)
    fun changeExciseStampBad(chExciesStampBad: TaskExciseStampBad): Boolean
    fun deleteExciseStampBad(delExciesStampBad: TaskExciseStampBad): Boolean
    fun clear()
}