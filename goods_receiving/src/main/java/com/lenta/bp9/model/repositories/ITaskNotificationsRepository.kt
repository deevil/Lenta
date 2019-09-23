package com.lenta.bp9.model.repositories

import com.lenta.bp9.model.task.TaskNotification
import com.lenta.bp9.model.task.revise.CommentToVP

interface ITaskNotificationsRepository {
    fun updateWithNotifications(general: List<TaskNotification>?,
                                document: List<TaskNotification>?,
                                product: List<TaskNotification>?,
                                condition: List<TaskNotification>?)
    fun getGeneralNotifications(): List<TaskNotification>
    fun getReviseDocumentNotifications(): List<TaskNotification>
    fun getReviseProductNotifications(): List<TaskNotification>
    fun getReviseConditionsNotifications(): List<TaskNotification>
    fun getInvoiceNotes(): List<CommentToVP>
    fun updateWithInvoiceNotes(notes: List<CommentToVP>)
    fun clear()
}