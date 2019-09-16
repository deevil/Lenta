package com.lenta.shared.platform.toolbar.top_toolbar

import androidx.annotation.DrawableRes
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.R


class TopToolbarUiModel(
        val title: MutableLiveData<String> = MutableLiveData(""),
        val description: MutableLiveData<String> = MutableLiveData(""),
        val uiModelButton1: ImageButtonUiModel = ImageButtonUiModel(),
        val uiModelButton2: ImageButtonUiModel = ImageButtonUiModel(),
        val uiModelButton3: ImageButtonUiModel = ImageButtonUiModel(),
        val visibility: MutableLiveData<Boolean> = MutableLiveData(true)
) {

    fun cleanAll(visibility: Boolean? = true) {
        description.value = ""
        uiModelButton1.clean()
        uiModelButton2.clean()
        uiModelButton3.clean()
        visibility?.let {
            this.visibility.value = it
        }
    }

}

data class ImageButtonUiModel(
        val buttonDecorationInfo: MutableLiveData<ImageButtonDecorationInfo?> = MutableLiveData(),
        val visibility: MutableLiveData<Boolean> = MutableLiveData(),
        val enabled: MutableLiveData<Boolean> = MutableLiveData()
) {

    fun clean() {
        buttonDecorationInfo.value = ImageButtonDecorationInfo.empty
        visibility.value = false
        enabled.value = true
    }

    fun show(buttonDecorationInfo: ImageButtonDecorationInfo? = null, visible: Boolean = true, enabled: Boolean = true) {
        this.visibility.value = visible
        this.enabled.value = enabled
        buttonDecorationInfo?.let {
            this.buttonDecorationInfo.value = it
        }
    }

}

data class ImageButtonDecorationInfo(
        @DrawableRes val iconRes: Int
) {

    companion object {
        val back: ImageButtonDecorationInfo? by lazy {
            ImageButtonDecorationInfo(R.drawable.ic_arrow_back_white_24dp)
        }

        val home: ImageButtonDecorationInfo? by lazy {
            ImageButtonDecorationInfo(R.drawable.ic_home_white_24dp)
        }

        val empty: ImageButtonDecorationInfo by lazy {
            ImageButtonDecorationInfo(0)
        }

        val exitFromApp: ImageButtonDecorationInfo by lazy {
            ImageButtonDecorationInfo(R.drawable.ic_exit_from_app_white)
        }

        val exitFromPleApp: ImageButtonDecorationInfo by lazy {
            ImageButtonDecorationInfo(R.drawable.ic_exit_from_app_white)
        }

        val settings: ImageButtonDecorationInfo by lazy {
            ImageButtonDecorationInfo(R.drawable.ic_settings_white)
        }

        val authorization: ImageButtonDecorationInfo by lazy {
            ImageButtonDecorationInfo(R.drawable.ic_authorization_24dp)
        }
    }

}