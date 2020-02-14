package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskTransportMarriageInfo

interface ITaskTransportMarriageRepository {
    fun getTransportMarriage(): List<TaskTransportMarriageInfo>
    fun findTransportMarriage(transportMarriage: TaskTransportMarriageInfo): TaskTransportMarriageInfo?
    fun addTransportMarriage(transportMarriage: TaskTransportMarriageInfo): Boolean
    fun updateTransportMarriage(newTransportMarriage: List<TaskTransportMarriageInfo>)
    fun changeTransportMarriage(transportMarriage: TaskTransportMarriageInfo): Boolean
    fun deleteTransportMarriage(transportMarriage: TaskTransportMarriageInfo): Boolean
    fun clear()
}