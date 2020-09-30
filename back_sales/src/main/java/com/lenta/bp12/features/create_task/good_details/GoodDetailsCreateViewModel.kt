package com.lenta.bp12.features.create_task.good_details

import com.lenta.bp12.features.base.BaseGoodDetailsViewModel
import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import javax.inject.Inject

class GoodDetailsCreateViewModel : BaseGoodDetailsViewModel<ICreateTaskManager>() {

    @Inject
    override lateinit var manager: ICreateTaskManager
}