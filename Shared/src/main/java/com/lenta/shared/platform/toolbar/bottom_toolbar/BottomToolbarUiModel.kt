package com.lenta.shared.platform.toolbar.bottom_toolbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.R

class BottomToolbarUiModel {
    val visibility: MutableLiveData<Boolean> = MutableLiveData()
    val uiModelButton1: ButtonUiModel = ButtonUiModel()
    val uiModelButton2: ButtonUiModel = ButtonUiModel()
    val uiModelButton3: ButtonUiModel = ButtonUiModel()
    val uiModelButton4: ButtonUiModel = ButtonUiModel()
    val uiModelButton5: ButtonUiModel = ButtonUiModel()
    val buttonsUiModels = listOf(uiModelButton1, uiModelButton2, uiModelButton3, uiModelButton4, uiModelButton5)

    fun cleanAll(visible: Boolean = true) {
        buttonsUiModels.forEach { it.clean() }
        visibility.value = visible

    }
}

data class ButtonUiModel(
        val buttonDecorationInfo: MutableLiveData<ButtonDecorationInfo?> = MutableLiveData(),
        val visibility: MutableLiveData<Boolean> = MutableLiveData(),
        val enabled: MutableLiveData<Boolean> = MutableLiveData(),
        val requestFocus: MutableLiveData<Any> = MutableLiveData()

) {
    fun clean() {
        buttonDecorationInfo.value = ButtonDecorationInfo.empty
        visibility.value = false
        enabled.value = true
    }

    fun show(buttonDecorationInfo: ButtonDecorationInfo? = null, visible: Boolean = true, enabled: Boolean = true) {
        this.visibility.value = visible
        this.enabled.value = enabled
        buttonDecorationInfo?.let {
            this.buttonDecorationInfo.value = it
        }

    }

    fun requestFocus() {
        requestFocus.postValue(true)
    }
}

data class ButtonDecorationInfo(
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int
) {
    companion object {
        val yes: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.yes)
        }
        val no: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_close_white_24dp, R.string.no)
        }
        val goOver: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.go_over)
        }
        val apply: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.apply)
        }
        val add: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_add_white_24dp, R.string.add)
        }
        val details: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_details_white_24dp, R.string.details)
        }

        val save: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.save)
        }
        val print: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_print_white_24dp, R.string.print)
        }
        val delete: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delete_white_24dp, R.string.delete)
        }
        val clean: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delete_white_24dp, R.string.clean)
        }
        val next: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_forward_white_24dp, R.string.to_next)
        }

        val back: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_back_white_24dp, R.string.to_back)
        }

        val enterToApp: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exit_to_app_white, R.string.enter)
        }

        val rollback: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_rollback_white_24dp, R.string.rollback)
        }

        val empty: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(0, 0)
        }


    }
}





