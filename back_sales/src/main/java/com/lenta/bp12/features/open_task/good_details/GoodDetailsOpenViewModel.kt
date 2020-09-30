package com.lenta.bp12.features.open_task.good_details

import com.lenta.bp12.features.base.BaseGoodDetailsViewModel
import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import javax.inject.Inject

class GoodDetailsOpenViewModel : BaseGoodDetailsViewModel<IOpenTaskManager>() {

    @Inject
    override lateinit var manager: IOpenTaskManager
}