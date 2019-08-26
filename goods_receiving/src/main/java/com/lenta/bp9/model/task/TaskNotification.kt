package com.lenta.bp9.model.task

import com.google.gson.annotations.SerializedName

enum class NotificationIndicatorType(val code: String) {
    None(""),
    Yellow("1"),
    Red("2");

    companion object {
        fun from(code: String): NotificationIndicatorType {
            return when (code) {
                "1" -> Yellow
                "2" -> Red
                else -> None
            }
        }
    }
}

data class TaskNotification (val text: String, val indicator: NotificationIndicatorType) {
    companion object {
        fun from(restInfo: TaskNotificationRestInfo): TaskNotification {
            return TaskNotification(text = restInfo.text, indicator = NotificationIndicatorType.from(restInfo.indicator))
        }
    }
}

data class TaskNotificationRestInfo (
        @SerializedName("NOTIF_TEXT")
        val text: String,
        @SerializedName("LIGHT")
        val indicator: String) {
}