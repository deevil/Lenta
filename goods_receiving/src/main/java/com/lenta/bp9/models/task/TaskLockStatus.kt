package com.lenta.bp9.models.task

enum class TaskLockStatus(val taskLockStatusString: String) {
    None(""),
    LockedByMe("1"),
    LockedByOthers("2");

    companion object {
        fun from(taskLockStatusString: String): TaskLockStatus {
            return when(taskLockStatusString) {
                "1" -> LockedByMe
                "2" -> LockedByOthers
                else -> None
            }
        }
    }
}