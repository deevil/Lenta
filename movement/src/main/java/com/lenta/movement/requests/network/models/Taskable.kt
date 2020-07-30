package com.lenta.movement.requests.network.models

import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType

/** Интерфейс объектов Task приходящих с сервера, для преобразования их в модель клиента
 * @see Taskable.toTask */
interface Taskable {
    val taskNumber: String?
    val description: String?
    val notFinish: String?
    val isCons : String?
    val currentStatusCode: String?
    val currentStatusText: String?
    val nextStatusText: String?
    val taskComment: String?
    val taskType: TaskType?
    val movementType: MovementType?
    val werksDstntnt: String?
    val lgortSrc: String?
    val lgortTarget: String?
    val dateShip: String?
    val blockType: String?
    val quantityPosition: String?
}