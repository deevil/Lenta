package com.lenta.bp9.model.memory

import com.lenta.bp9.model.repositories.ITaskNotificationsRepository
import com.lenta.bp9.model.task.TaskNotification

class MemoryTaskNotificationsRepository : ITaskNotificationsRepository {

    private val generalNotifications: ArrayList<TaskNotification> = ArrayList()
    private val documentNotifications: ArrayList<TaskNotification> = ArrayList()
    private val productNotifications: ArrayList<TaskNotification> = ArrayList()
    private val conditionsNotifications: ArrayList<TaskNotification> = ArrayList()

    override fun getGeneralNotifications(): List<TaskNotification> {
        return generalNotifications.toList()
    }

    override fun getReviseDocumentNotifications(): List<TaskNotification> {
        return documentNotifications.toList()
    }

    override fun getReviseProductNotifications(): List<TaskNotification> {
        return productNotifications.toList()
    }

    override fun getReviseConditionsNotifications(): List<TaskNotification> {
        return conditionsNotifications.toList()
    }

    override fun updateWithNotifications(general: List<TaskNotification>?,
                                document: List<TaskNotification>?,
                                product: List<TaskNotification>?,
                                condition: List<TaskNotification>?) {
        general?.let {
            generalNotifications.clear()
            generalNotifications.addAll(it)
        }
        document?.let {
            documentNotifications.clear()
            documentNotifications.addAll(it)
        }
        product?.let {
            productNotifications.clear()
            productNotifications.addAll(it)
        }
        condition?.let {
            conditionsNotifications.clear()
            conditionsNotifications.addAll(it)
        }
    }

    override fun clear() {
        generalNotifications.clear()
        documentNotifications.clear()
        productNotifications.clear()
        conditionsNotifications.clear()
    }
}