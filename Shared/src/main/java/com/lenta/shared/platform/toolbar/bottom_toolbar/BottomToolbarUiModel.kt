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

    fun cleanAll() {
        buttonsUiModels.forEach { it.clean() }
        visibility.value = false

    }
}

data class ButtonUiModel(
        val buttonDecorationInfo: MutableLiveData<ButtonDecorationInfo?> = MutableLiveData(),
        val visibility: MutableLiveData<Boolean> = MutableLiveData(),
        val enabled: MutableLiveData<Boolean> = MutableLiveData()
) {
    fun clean() {
        buttonDecorationInfo.value = ButtonDecorationInfo.clean
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
}

data class ButtonDecorationInfo(
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int
) {
    companion object {
        val enterToApp: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exit_to_app_white, R.string.enter)
        }
        val clean: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(0, 0)
        }

    }
}





