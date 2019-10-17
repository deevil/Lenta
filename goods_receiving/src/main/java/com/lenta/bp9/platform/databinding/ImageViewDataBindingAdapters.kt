package com.lenta.bp9.platform.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.lenta.bp9.R
import com.lenta.bp9.features.task_list.TaskPostponedStatus
import com.lenta.bp9.model.task.NotificationIndicatorType
import com.lenta.bp9.model.task.TaskLockStatus
import com.lenta.bp9.model.task.revise.ConditionViewType
import com.lenta.shared.utilities.extentions.setVisible
import com.lenta.shared.utilities.extentions.setVisibleGone
import com.lenta.bp9.model.task.revise.DocumentType
import com.lenta.bp9.model.task.revise.ProductDocumentType

@BindingAdapter("postponedTaskStatusIcon")
fun setPostponedStatusIcon(imageView: ImageView, statusType: TaskPostponedStatus?) {
    statusType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("lockTaskStatusIcon")
fun setLockStatusIcon(imageView: ImageView, statusType: TaskLockStatus?) {
    statusType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("notificationIndicatorIcon")
fun setNotificationIndicatorIcon(imageView: ImageView, indicatorType: NotificationIndicatorType?) {
    indicatorType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("documentTypeIcon")
fun setDocumentTypeIcon(imageView: ImageView, docType: DocumentType?) {
    docType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("productDocumentTypeIcon")
fun setProductDocumentTypeIcon(imageView: ImageView, docType: ProductDocumentType?) {
    docType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("conditionViewTypeIcon")
fun setProductDocumentTypeIcon(imageView: ImageView, condViewType: ConditionViewType?) {
    condViewType?.getIconRes()?.let {
        imageView.setVisible()
        imageView.setImageResource(it)
        return
    }
    imageView.setVisibleGone()
}

@BindingAdapter("visibleOrGone")
fun setVisibleOrGone(imageView: ImageView, isVisible: Boolean?) {
    if (isVisible == true) {
        imageView.setVisible()
    } else {
        imageView.setVisibleGone()
    }
}

fun TaskPostponedStatus.getIconRes(): Int? {
    return when (this) {
        TaskPostponedStatus.PauseSign -> R.drawable.ic_checkbox_green_32dp //TODO: Proper icon
        TaskPostponedStatus.PlaySign -> R.drawable.ic_play_arrow_white_24dp
        else -> null
    }
}

fun TaskLockStatus.getIconRes(): Int? {
    return when (this) {
        TaskLockStatus.LockedByMe -> R.drawable.ic_checkbox_green_32dp //TODO: Proper icon
        TaskLockStatus.LockedByOthers -> R.drawable.ic_lock_white_24dp
        else -> null
    }
}

fun NotificationIndicatorType.getIconRes(): Int? {
    return when (this) {
        NotificationIndicatorType.Yellow -> R.drawable.ic_indicator_orange
        NotificationIndicatorType.Red -> R.drawable.ic_indicator_red
        else -> null
    }
}

fun DocumentType.getIconRes(): Int? {
    return when (this) {
        DocumentType.Simple -> R.drawable.ic_simple_delivery_doc
        DocumentType.Invoice -> R.drawable.ic_invoice
        else -> null
    }
}

fun ProductDocumentType.getIconRes(): Int? {
    return when (this) {
        ProductDocumentType.Simple -> R.drawable.ic_simple_product_doc
        ProductDocumentType.AlcoRus, ProductDocumentType.AlcoImport -> R.drawable.ic_alco_task_icon
        else -> null
    }
}

fun ConditionViewType.getIconRes(): Int? {
    return when (this) {
        ConditionViewType.Temperature -> R.drawable.ic_temperature
        ConditionViewType.Simple, ConditionViewType.Seal -> R.drawable.ic_simple_delivery_doc
        else -> null
    }
}