package com.lenta.bp18.request.model.result

import com.lenta.bp18.request.pojo.RetCode
import com.lenta.shared.utilities.extentions.IResultWithRetCodes

data class GoodInfoResult(
        /**Таблица возврата*/
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes